package com.pixelrakete.lovecal.data.repository

import android.content.ContentResolver
import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.pixelrakete.lovecal.data.manager.UserManager
import com.pixelrakete.lovecal.data.services.GoogleCalendarService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

data class CalendarListEntry(
    val id: String = "",
    val title: String = "",
    val color: String = "",
    val accessRole: String = "",
    val name: String = "",
    val timeZone: String = "",
    val isReadOnly: Boolean = false,
    val isPrimary: Boolean = false,
    val canModifyEvents: Boolean = true,
    val userId: String = "",
    val coupleId: String = ""
)

class CalendarRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userManager: UserManager,
    private val googleCalendarService: GoogleCalendarService,
    @ApplicationContext private val context: Context
) : CalendarRepository {
    companion object {
        private const val TAG = "CalendarRepository"
        private const val SHARED_CALENDAR_COLLECTION = "shared_calendars"
    }

    private val calendarCollection = firestore.collection(SHARED_CALENDAR_COLLECTION)
    private val contentResolver: ContentResolver = context.contentResolver

    override suspend fun getCalendars(): List<CalendarListEntry> = withContext(Dispatchers.IO) {
        try {
            val projection = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.OWNER_ACCOUNT,
                CalendarContract.Calendars.CALENDAR_COLOR,
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                CalendarContract.Calendars.CALENDAR_TIME_ZONE,
                CalendarContract.Calendars.IS_PRIMARY
            )

            val calendars = mutableListOf<CalendarListEntry>()
            contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
                    val displayName = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME))
                    val ownerAccount = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.OWNER_ACCOUNT))
                    val color = cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_COLOR))
                    val accessLevel = cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL))
                    val timeZone = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_TIME_ZONE))
                    val isPrimary = cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.IS_PRIMARY)) == 1

                    val isReadOnly = accessLevel <= CalendarContract.Calendars.CAL_ACCESS_READ
                    val canModifyEvents = accessLevel >= CalendarContract.Calendars.CAL_ACCESS_READ

                    calendars.add(
                        CalendarListEntry(
                            id = id,
                            title = displayName ?: "Unknown Calendar",
                            color = String.format("#%06X", 0xFFFFFF and color),
                            accessRole = if (isReadOnly) "reader" else "owner",
                            name = ownerAccount ?: "",
                            timeZone = timeZone ?: "",
                            isReadOnly = isReadOnly,
                            isPrimary = isPrimary,
                            canModifyEvents = canModifyEvents
                        )
                    )
                }
            }
            calendars
        } catch (e: Exception) {
            Log.e(TAG, "Error getting calendars", e)
            throw e
        }
    }

    override suspend fun addCalendar(calendar: CalendarListEntry) {
        val userId = userManager.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val coupleId = userManager.getCurrentCoupleId() ?: throw IllegalStateException("User not in a couple")
        
        try {
            // Store calendar info in Firestore
            val sharedCalendarDoc = calendarCollection.document(coupleId)
            
            // First check if document already exists
            val existingDoc = sharedCalendarDoc.get().await()
            if (existingDoc.exists()) {
                throw IllegalStateException("Calendar already shared for this couple")
            }

            val sharedCalendarData = hashMapOf(
                "calendarId" to calendar.id,
                "title" to calendar.title,
                "ownerId" to userId,
                "ownerEmail" to calendar.name,
                "timeZone" to calendar.timeZone,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
                "status" to "active"
            )

            // Use a transaction to ensure atomic write
            firestore.runTransaction { transaction ->
                transaction.set(sharedCalendarDoc, sharedCalendarData)
            }.await()

            // Set up Google Calendar ACL rule for future partner
            setupCalendarSharing(calendar.id)

            Log.d(TAG, "Successfully added shared calendar: ${calendar.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding calendar", e)
            throw e
        }
    }

    private suspend fun setupCalendarSharing(calendarId: String) {
        try {
            val calendarService = googleCalendarService.getCalendarService()
            
            // Create a default ACL rule that will be used when partner2 joins
            val aclRule = com.google.api.services.calendar.model.AclRule().apply {
                scope = com.google.api.services.calendar.model.AclRule.Scope().apply {
                    type = "default"
                }
                role = "writer"  // Allows viewing and editing events
            }

            calendarService.acl().insert(calendarId, aclRule).execute()
            Log.d(TAG, "Successfully set up calendar sharing for calendar: $calendarId")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up calendar sharing", e)
            throw e
        }
    }

    override suspend fun getSharedCalendar(coupleId: String): CalendarListEntry? {
        return try {
            val doc = calendarCollection.document(coupleId).get().await()
            if (doc.exists()) {
                val data = doc.data ?: return null
                
                // Validate required fields
                val calendarId = data["calendarId"] as? String ?: return null
                val status = data["status"] as? String
                
                // Only return active calendars
                if (status != "active") {
                    Log.d(TAG, "Calendar found but status is $status")
                    return null
                }

                CalendarListEntry(
                    id = calendarId,
                    title = (data["title"] as? String) ?: "Shared Calendar",
                    color = "#4285F4", // Google Calendar blue
                    accessRole = "writer",
                    name = "Shared Couple Calendar",
                    timeZone = (data["timeZone"] as? String) ?: TimeZone.getDefault().id,
                    isReadOnly = false,
                    isPrimary = false,
                    canModifyEvents = true,
                    userId = (data["ownerId"] as? String) ?: "",
                    coupleId = coupleId
                )
            } else {
                Log.d(TAG, "No shared calendar found for couple: $coupleId")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting shared calendar", e)
            throw e
        }
    }
} 