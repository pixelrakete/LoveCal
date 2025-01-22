package com.pixelrakete.lovecal.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.pixelrakete.lovecal.data.model.Couple
import com.pixelrakete.lovecal.util.ValidationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Date

@Singleton
class CoupleRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CoupleRepository {

    override suspend fun getCouple(coupleId: String): Couple? = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection("couples")
                .document(coupleId)
                .get()
                .await()
            
            if (doc.exists()) {
                doc.toObject(Couple::class.java)?.copy(id = doc.id)
            } else {
                null
            }
        } catch (e: Exception) {
            throw handleError(e)
        }
    }

    override suspend fun updateCouple(couple: Couple): Unit = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("No user ID available")
            
            // Verify user is part of the couple
            val existingCouple = getCouple(couple.id) ?: throw IllegalStateException("Couple not found")
            if (existingCouple.partner1Id != userId && existingCouple.partner2Id != userId) {
                throw IllegalStateException("User not authorized to update this couple")
            }
            
            val updates = mapOf(
                "partner1Name" to couple.partner1Name,
                "partner1Color" to couple.partner1Color,
                "partner1Interests" to couple.partner1Interests,
                "partner2Name" to couple.partner2Name,
                "partner2Color" to couple.partner2Color,
                "partner2Interests" to couple.partner2Interests,
                "monthlyBudget" to couple.monthlyBudget,
                "dateFrequencyWeeks" to couple.dateFrequencyWeeks,
                "city" to couple.city,
                "updatedAt" to Date()
            )
            
            firestore.collection("couples")
                .document(couple.id)
                .update(updates)
                .await()
        } catch (e: Exception) {
            throw handleError(e)
        }
    }

    override suspend fun createCouple(couple: Couple): String = withContext(Dispatchers.IO) {
        try {
            // Generate a unique invitation code
            var invitationCode: String
            var isCodeUnique = false
            do {
                invitationCode = ValidationUtils.generateInvitationCode()
                val existingCouple = firestore.collection("couples")
                    .whereEqualTo("invitationCode", invitationCode)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()
                isCodeUnique = existingCouple == null
            } while (!isCodeUnique)
            
            val coupleWithCode = couple.copy(invitationCode = invitationCode)
            val docRef = firestore.collection("couples")
                .add(coupleWithCode)
                .await()
            
            docRef.id
        } catch (e: Exception) {
            throw handleError(e)
        }
    }

    override fun observeCouple(coupleId: String): Flow<Couple?> = flow {
        try {
            val snapshot = firestore.collection("couples")
                .document(coupleId)
                .get()
                .await()
            
            emit(
                if (snapshot.exists()) {
                    snapshot.toObject(Couple::class.java)?.copy(id = snapshot.id)
                } else {
                    null
                }
            )
        } catch (e: Exception) {
            throw handleError(e)
        }
    }.flowOn(Dispatchers.IO)

    private fun handleError(e: Exception): Exception = when (e) {
        is FirebaseFirestoreException -> IllegalStateException("Failed to access Firestore: ${e.message}")
        is IllegalStateException -> e
        else -> IllegalStateException("Couple error: ${e.message}")
    }
} 