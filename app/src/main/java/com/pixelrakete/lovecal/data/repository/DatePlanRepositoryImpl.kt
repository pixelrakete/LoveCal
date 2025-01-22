package com.pixelrakete.lovecal.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pixelrakete.lovecal.data.manager.UserManager
import com.pixelrakete.lovecal.data.model.DatePlan
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatePlanRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userManager: UserManager
) : DatePlanRepository {

    override suspend fun createDatePlan(
        title: String,
        description: String?,
        location: String?,
        budget: Double?,
        dateTimeStr: String?,
        isSurprise: Boolean
    ): DatePlan {
        val userId = userManager.getCurrentUserId() ?: throw IllegalStateException("User not logged in")
        val coupleId = userManager.getCoupleId() ?: throw IllegalStateException("User not in a couple")
        
        val datePlan = DatePlan(
            title = title,
            description = description,
            location = location,
            budget = budget,
            dateTimeStr = dateTimeStr,
            createdBy = userId,
            coupleId = coupleId,
            isSurprise = isSurprise
        )
        
        val doc = firestore.collection("date_plans").document()
        val datePlanWithId = datePlan.copy(id = doc.id)
        doc.set(datePlanWithId).await()
        
        return datePlanWithId
    }

    override fun getDatePlans(): Flow<List<DatePlan>> = callbackFlow {
        val coupleId = userManager.getCoupleId() ?: throw IllegalStateException("User not in a couple")
        
        val registration = firestore.collection("date_plans")
            .whereEqualTo("coupleId", coupleId)
            .orderBy("dateTimeStr", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val datePlans = snapshot?.documents?.map { doc ->
                    DatePlan.fromFirestore(doc.data?.plus(mapOf("id" to doc.id)) ?: emptyMap())
                } ?: emptyList()
                
                trySend(datePlans)
            }
        
        awaitClose { registration.remove() }
    }

    override suspend fun getDatePlan(id: String): DatePlan? {
        val doc = firestore.collection("date_plans").document(id).get().await()
        return if (doc.exists()) {
            DatePlan.fromFirestore(doc.data?.plus(mapOf("id" to doc.id)) ?: emptyMap())
        } else {
            null
        }
    }

    override suspend fun deleteDatePlan(id: String) {
        firestore.collection("date_plans").document(id).delete().await()
    }

    override suspend fun markDatePlanAsCompleted(id: String) {
        firestore.collection("date_plans").document(id)
            .update("completed", true)
            .await()
    }

    override suspend fun updateDatePlan(
        id: String,
        title: String,
        description: String?,
        location: String?,
        budget: Double?,
        dateTimeStr: String?,
        isSurprise: Boolean
    ): DatePlan {
        val userId = userManager.getCurrentUserId() ?: throw IllegalStateException("User not logged in")
        val coupleId = userManager.getCoupleId() ?: throw IllegalStateException("User not in a couple")
        
        val datePlan = DatePlan(
            id = id,
            title = title,
            description = description,
            location = location,
            budget = budget,
            dateTimeStr = dateTimeStr,
            createdBy = userId,
            coupleId = coupleId,
            isSurprise = isSurprise
        )
        
        firestore.collection("date_plans").document(id).set(datePlan).await()
        return datePlan
    }

    override suspend fun updateDatePlanCompleted(id: String, completed: Boolean) {
        firestore.collection("date_plans")
            .document(id)
            .update("completed", completed)
            .await()
    }
} 