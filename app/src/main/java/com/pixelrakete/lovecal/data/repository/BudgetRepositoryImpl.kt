package com.pixelrakete.lovecal.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : BudgetRepository {

    private val budgetCollection = firestore.collection("budgets")
    private val couplesCollection = firestore.collection("couples")

    override suspend fun getMonthlyBudget(): Double {
        val userId = auth.currentUser?.uid ?: return 0.0
        return try {
            val couple = couplesCollection
                .whereEqualTo("partner1Id", userId)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.getDouble("monthlyBudget")
                ?: couplesCollection
                    .whereEqualTo("partner2Id", userId)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()
                    ?.getDouble("monthlyBudget")
                ?: 0.0
            couple
        } catch (e: Exception) {
            0.0
        }
    }

    override suspend fun updateMonthlyBudget(amount: Double) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        budgetCollection.document(userId).set(mapOf("monthlyBudget" to amount)).await()
    }

    override suspend fun getSpentBudget(): Double {
        val userId = auth.currentUser?.uid ?: return 0.0
        return try {
            val document = budgetCollection.document(userId).get().await()
            document.getDouble("spentBudget") ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    override suspend fun getRemainingBudget(): Double {
        val monthlyBudget = getMonthlyBudget()
        val spentBudget = getSpentBudget()
        return monthlyBudget - spentBudget
    }
} 