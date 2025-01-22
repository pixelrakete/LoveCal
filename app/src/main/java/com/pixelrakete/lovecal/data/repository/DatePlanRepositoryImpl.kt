package com.pixelrakete.lovecal.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pixelrakete.lovecal.data.model.DatePlan
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

class DatePlanRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val coupleRepository: CoupleRepository
) : DatePlanRepository {

    override suspend fun getDatePlans(): List<DatePlan> {
        val couple = coupleRepository.getCurrentCouple() ?: return emptyList()
        
        return firestore.collection("dates")
            .whereEqualTo("coupleId", couple.id)
            .orderBy("startDateTime", Query.Direction.ASCENDING)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(DatePlan::class.java)?.copy(id = doc.id)
            }
    }

    override suspend fun getDatePlan(id: String): DatePlan? {
        return firestore.collection("dates")
            .document(id)
            .get()
            .await()
            .toObject(DatePlan::class.java)
            ?.copy(id = id)
    }

    override suspend fun createDatePlan(
        title: String,
        description: String,
        location: String,
        startDateTime: LocalDateTime,
        budget: Double,
        isSurprise: Boolean
    ): String {
        val couple = coupleRepository.getCurrentCouple() ?: throw IllegalStateException("No couple found")
        val currentUserId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        
        val datePlan = DatePlan(
            title = title,
            description = description,
            location = location,
            startDateTime = startDateTime,
            budget = budget,
            isSurprise = isSurprise,
            coupleId = couple.id,
            plannerId = currentUserId,
            createdAt = LocalDateTime.now()
        )
        
        return firestore.collection("dates")
            .add(datePlan)
            .await()
            .id
    }

    override suspend fun updateDatePlan(datePlan: DatePlan) {
        firestore.collection("dates")
            .document(datePlan.id!!)
            .set(datePlan)
            .await()
    }

    override suspend fun deleteDatePlan(id: String) {
        firestore.collection("dates")
            .document(id)
            .delete()
            .await()
    }
} 