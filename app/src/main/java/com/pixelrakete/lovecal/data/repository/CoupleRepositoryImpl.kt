package com.pixelrakete.lovecal.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pixelrakete.lovecal.data.model.Couple
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*
import javax.inject.Inject
import android.content.SharedPreferences

class CoupleRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val prefs: SharedPreferences
) : CoupleRepository {

    companion object {
        private const val COUPLES_COLLECTION = "couples"
        private const val TEMP_CALENDAR_ID_KEY = "temp_calendar_id"
    }

    private val couplesCollection = firestore.collection("couples")

    private fun generateInvitationCode(): String {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        return (1..6).map { allowedChars.random() }.joinToString("")
    }

    override fun observeCurrentCouple(): Flow<Couple?> = callbackFlow {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        
        val listener = couplesCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val couple = snapshot?.documents
                    ?.mapNotNull { it.toObject(Couple::class.java)?.copy(id = it.id) }
                    ?.firstOrNull { it.partner1Id == userId || it.partner2Id == userId }
                
                trySend(couple)
            }

        awaitClose {
            listener.remove()
        }
    }

    override suspend fun getCurrentCouple(): Couple? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val snapshot = couplesCollection
                .whereEqualTo("partner1Id", userId)
                .get()
                .await()
            
            var couple = snapshot.documents.firstOrNull()?.toObject(Couple::class.java)
            if (couple != null) {
                couple = couple.copy(id = snapshot.documents.first().id)
                return couple
            }

            val snapshot2 = couplesCollection
                .whereEqualTo("partner2Id", userId)
                .get()
                .await()
            
            couple = snapshot2.documents.firstOrNull()?.toObject(Couple::class.java)
            if (couple != null) {
                couple = couple.copy(id = snapshot2.documents.first().id)
            }
            couple
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createCouple(
        partner1Name: String,
        partner1Color: String,
        monthlyBudget: Double,
        city: String
    ): Couple {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val invitationCode = generateInvitationCode()
        
        val couple = Couple(
            partner1Id = userId,
            partner1Name = partner1Name,
            partner1Color = partner1Color,
            monthlyBudget = monthlyBudget,
            city = city,
            invitationCode = invitationCode,
            setupComplete = false
        )
        
        val docRef = couplesCollection.add(couple).await()
        return couple.copy(id = docRef.id)
    }

    override suspend fun updateCouple(couple: Couple) {
        couplesCollection.document(couple.id).set(couple).await()
    }

    override suspend fun updateCouple(
        partner1Name: String,
        partner2Name: String,
        partner1Color: String,
        partner2Color: String,
        partner1Interests: List<String>,
        partner2Interests: List<String>,
        monthlyBudget: Double,
        city: String
    ) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val currentCouple = getCurrentCouple() ?: throw IllegalStateException("No couple found")

        val updatedCouple = currentCouple.copy(
            partner1Name = partner1Name,
            partner2Name = partner2Name,
            partner1Color = partner1Color,
            partner2Color = partner2Color,
            partner1Interests = partner1Interests,
            partner2Interests = partner2Interests,
            monthlyBudget = monthlyBudget,
            city = city,
            setupComplete = currentCouple.setupComplete
        )

        couplesCollection.document(currentCouple.id).set(updatedCouple).await()
    }

    override suspend fun acceptInvitation(invitationCode: String) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        
        val snapshot = couplesCollection
            .whereEqualTo("invitationCode", invitationCode)
            .get()
            .await()
        
        val couple = snapshot.documents.firstOrNull()?.toObject(Couple::class.java)
            ?: throw IllegalStateException("Invalid invitation code")
        
        if (couple.partner2Id != null) {
            throw IllegalStateException("This couple already has a partner")
        }
        
        couplesCollection.document(snapshot.documents.first().id)
            .update("partner2Id", userId)
            .await()
    }

    override suspend fun validateInvitationCode(invitationCode: String): Boolean {
        val snapshot = couplesCollection
            .whereEqualTo("invitationCode", invitationCode)
            .get()
            .await()
        
        val couple = snapshot.documents.firstOrNull()?.toObject(Couple::class.java)
        
        return couple != null && couple.partner2Id == null
    }

    override suspend fun completePartner2Setup(
        invitationCode: String,
        partner2Name: String,
        partner2Color: String,
        partner2Interests: List<String>
    ) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        
        val snapshot = couplesCollection
            .whereEqualTo("invitationCode", invitationCode)
            .get()
            .await()
        
        val couple = snapshot.documents.firstOrNull()?.toObject(Couple::class.java)
            ?: throw IllegalStateException("Invalid invitation code")
        
        if (couple.partner2Id != null) {
            throw IllegalStateException("This couple already has a partner")
        }
        
        couplesCollection.document(snapshot.documents.first().id)
            .update(mapOf(
                "partner2Id" to userId,
                "partner2Name" to partner2Name,
                "partner2Color" to partner2Color,
                "partner2Interests" to partner2Interests
            ))
            .await()
    }
    
    override suspend fun setupCalendar(calendarId: String, isPartner1: Boolean) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val couple = getCurrentCouple() ?: throw IllegalStateException("No couple found")
        
        val field = if (isPartner1) "partner1CalendarId" else "partner2CalendarId"
        
        couplesCollection.document(couple.id)
            .update(field, calendarId)
            .await()
    }
    
    override suspend fun markSetupComplete() {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val couple = getCurrentCouple() ?: throw IllegalStateException("No couple found")
        
        couplesCollection.document(couple.id)
            .update("setupComplete", true)
            .await()
    }

    override suspend fun storeTemporaryCalendarId(calendarId: String) {
        prefs.edit().putString(TEMP_CALENDAR_ID_KEY, calendarId).apply()
    }

    override suspend fun getTemporaryCalendarId(): String? {
        return prefs.getString(TEMP_CALENDAR_ID_KEY, null)
    }
} 