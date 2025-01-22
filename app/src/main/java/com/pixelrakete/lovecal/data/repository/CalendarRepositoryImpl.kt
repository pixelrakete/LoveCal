package com.pixelrakete.lovecal.data.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.calendar.CalendarScopes
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import javax.inject.Inject

class CalendarRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    private val context: Context,
    private val auth: FirebaseAuth
) : CalendarRepository {

    private fun checkAuthentication(): GoogleSignInAccount {
        val firebaseUser = auth.currentUser ?: throw IllegalStateException("Firebase user not logged in")
        Log.d("CalendarRepository", "Firebase user authenticated: ${firebaseUser.email}")
        
        val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
            ?: throw IllegalStateException("Google account not found")
            
        Log.d("CalendarRepository", "Google account found: ${googleAccount.email}")
        
        if (!googleAccount.grantedScopes.any { it.scopeUri == CalendarScopes.CALENDAR }) {
            throw IllegalStateException("Calendar permission not granted")
        }
        
        return googleAccount
    }

    override suspend fun createCalendar(name: String): Long {
        val googleAccount = checkAuthentication()
        Log.d("CalendarRepository", "Creating calendar with name: $name for account: ${googleAccount.email}")
        
        val values = ContentValues().apply {
            put(CalendarContract.Calendars.ACCOUNT_NAME, googleAccount.email)
            put(CalendarContract.Calendars.ACCOUNT_TYPE, "com.google")
            put(CalendarContract.Calendars.NAME, name)
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, name)
            put(CalendarContract.Calendars.CALENDAR_COLOR, 0xFF4081) // Pink color
            put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER)
            put(CalendarContract.Calendars.OWNER_ACCOUNT, googleAccount.email)
            put(CalendarContract.Calendars.VISIBLE, 1)
            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
        }

        try {
            val uri = contentResolver.insert(
                CalendarContract.Calendars.CONTENT_URI.buildUpon()
                    .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                    .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, googleAccount.email)
                    .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, "com.google")
                    .build(),
                values
            )
            
            if (uri == null) {
                Log.e("CalendarRepository", "Failed to create calendar: URI is null")
                return -1L
            }

            val calendarId = uri.lastPathSegment?.toLong() ?: -1L
            Log.d("CalendarRepository", "Calendar created with ID: $calendarId")
            return calendarId
        } catch (e: SecurityException) {
            Log.e("CalendarRepository", "Security exception while creating calendar", e)
            throw IllegalStateException("Calendar permission denied", e)
        } catch (e: Exception) {
            Log.e("CalendarRepository", "Error creating calendar", e)
            throw e
        }
    }

    override suspend fun getCalendars(): List<Calendar> {
        val googleAccount = checkAuthentication()
        Log.d("CalendarRepository", "Getting calendars for account: ${googleAccount.email}")
        
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.OWNER_ACCOUNT,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
        )

        val selection = "(${CalendarContract.Calendars.ACCOUNT_TYPE} = ?) AND " +
                       "(${CalendarContract.Calendars.OWNER_ACCOUNT} = ? OR " +
                       "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ${CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR})"
        val selectionArgs = arrayOf("com.google", googleAccount.email)

        val calendars = mutableListOf<Calendar>()
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )

            if (cursor == null) {
                Log.e("CalendarRepository", "Query returned null cursor")
                return emptyList()
            }

            Log.d("CalendarRepository", "Found ${cursor.count} calendars in total")
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
                val displayName = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME))
                val ownerAccount = cursor.getString(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.OWNER_ACCOUNT))
                val accessLevel = cursor.getInt(cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL))
                
                val isOwner = ownerAccount == googleAccount.email
                val isShared = accessLevel >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR
                
                Log.d("CalendarRepository", "Calendar: $displayName (ID: $id, Owner: $ownerAccount, Access: $accessLevel, IsOwner: $isOwner, IsShared: $isShared)")
                calendars.add(Calendar(id, displayName))
            }
            
            Log.d("CalendarRepository", "Returning ${calendars.size} calendars")
            return calendars
        } catch (e: SecurityException) {
            Log.e("CalendarRepository", "Security exception while getting calendars", e)
            throw IllegalStateException("Calendar permission denied", e)
        } catch (e: Exception) {
            Log.e("CalendarRepository", "Error getting calendars", e)
            throw e
        } finally {
            cursor?.close()
        }
    }

    override suspend fun addDateToCalendar(
        title: String,
        description: String,
        location: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        calendarId: Long
    ): Long {
        checkAuthentication() // Verify authentication before proceeding
        Log.d("CalendarRepository", "Adding date to calendar $calendarId")
        
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.EVENT_LOCATION, location)
            put(CalendarContract.Events.DTSTART, startDateTime.toEpochSecond(java.time.ZoneOffset.UTC) * 1000)
            put(CalendarContract.Events.DTEND, endDateTime.toEpochSecond(java.time.ZoneOffset.UTC) * 1000)
            put(CalendarContract.Events.EVENT_TIMEZONE, "UTC")
        }

        try {
            val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            if (uri == null) {
                Log.e("CalendarRepository", "Failed to add date: URI is null")
                return -1L
            }
            
            val eventId = uri.lastPathSegment?.toLong() ?: -1L
            Log.d("CalendarRepository", "Date added with ID: $eventId")
            return eventId
        } catch (e: SecurityException) {
            Log.e("CalendarRepository", "Security exception while adding date", e)
            throw IllegalStateException("Calendar permission denied", e)
        } catch (e: Exception) {
            Log.e("CalendarRepository", "Error adding date to calendar", e)
            throw e
        }
    }
} 