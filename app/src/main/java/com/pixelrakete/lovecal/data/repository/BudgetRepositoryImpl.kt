package com.pixelrakete.lovecal.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pixelrakete.lovecal.data.cache.BudgetCache
import com.pixelrakete.lovecal.data.model.BudgetValidation
import com.pixelrakete.lovecal.data.model.BudgetValidationResult
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val budgetCache: BudgetCache
) : BudgetRepository {

    companion object {
        private const val COUPLES_COLLECTION = "couples"
        private const val DATES_COLLECTION = "dates"
        private const val FIELD_MONTHLY_BUDGET = "monthlyBudget"
        private const val FIELD_BUDGET = "budget"
    }

    override suspend fun getMonthlyBudget(): Double {
        // Check cache first
        budgetCache.getMonthlyBudget()?.let { cachedBudget ->
            if (budgetCache.isCacheValid()) {
                return cachedBudget
            }
        }

        // Cache miss or invalid, fetch from Firestore
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        
        // Get user's couple ID first
        val userDoc = firestore.collection("users")
            .document(userId)
            .get()
            .await()
        
        val coupleId = userDoc.getString("coupleId") ?: throw IllegalStateException("User not in a couple")
        
        // Get couple document using the coupleId
        val coupleDoc = firestore.collection(COUPLES_COLLECTION)
            .document(coupleId)
            .get()
            .await()

        val monthlyBudget = coupleDoc.getDouble(FIELD_MONTHLY_BUDGET) ?: 500.0
        
        // Cache the result
        budgetCache.saveMonthlyBudget(monthlyBudget)
        
        return monthlyBudget
    }

    override suspend fun updateMonthlyBudget(amount: Double) {
        // Validate budget amount
        when (BudgetValidation.validateMonthlyBudget(amount)) {
            is BudgetValidationResult.Valid -> {
                val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
                
                // Get user's couple ID
                val userDoc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                
                val coupleId = userDoc.getString("coupleId") ?: throw IllegalStateException("User not in a couple")

                // Update Firestore
                firestore.collection(COUPLES_COLLECTION)
                    .document(coupleId)
                    .update(FIELD_MONTHLY_BUDGET, amount)
                    .await()

                // Update cache
                budgetCache.saveMonthlyBudget(amount)
                // Invalidate spent and planned budget cache as they might need recalculation
                budgetCache.invalidateCache()
            }
            is BudgetValidationResult.Invalid -> throw IllegalArgumentException("Invalid budget amount: ${amount}")
        }
    }

    override suspend fun getSpentBudget(): Double {
        // Check cache first
        budgetCache.getSpentBudget()?.let { cachedSpent ->
            if (budgetCache.isCacheValid()) {
                return cachedSpent
            }
        }

        // Cache miss or invalid, calculate from Firestore
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        
        // Get user's couple ID
        val userDoc = firestore.collection("users")
            .document(userId)
            .get()
            .await()
        
        val coupleId = userDoc.getString("coupleId") ?: throw IllegalStateException("User not in a couple")

        val spentBudget = firestore.collection(DATES_COLLECTION)
            .whereEqualTo("coupleId", coupleId)
            .whereEqualTo("completed", true)  // Only count completed dates
            .get()
            .await()
            .documents
            .mapNotNull { it.getDouble(FIELD_BUDGET) }
            .sum()

        // Cache the result
        budgetCache.saveSpentBudget(spentBudget)

        return spentBudget
    }

    override suspend fun getPlannedBudget(): Double {
        // Check cache first
        budgetCache.getPlannedBudget()?.let { cachedPlanned ->
            if (budgetCache.isCacheValid()) {
                return cachedPlanned
            }
        }

        // Cache miss or invalid, calculate from Firestore
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        
        // Get user's couple ID
        val userDoc = firestore.collection("users")
            .document(userId)
            .get()
            .await()
        
        val coupleId = userDoc.getString("coupleId") ?: throw IllegalStateException("User not in a couple")

        val plannedBudget = firestore.collection(DATES_COLLECTION)
            .whereEqualTo("coupleId", coupleId)
            .whereEqualTo("completed", false)  // Only count uncompleted dates
            .get()
            .await()
            .documents
            .mapNotNull { it.getDouble(FIELD_BUDGET) }
            .sum()

        // Cache the result
        budgetCache.savePlannedBudget(plannedBudget)

        return plannedBudget
    }

    override suspend fun validateDateBudget(amount: Double): BudgetValidationResult {
        val monthlyBudget = getMonthlyBudget()
        val spentBudget = getSpentBudget()
        val remainingBudget = monthlyBudget - spentBudget
        
        return BudgetValidation.validateDateBudget(amount, monthlyBudget, remainingBudget)
    }

    override suspend fun getRemainingBudget(): Double {
        val monthlyBudget = getMonthlyBudget()
        val spentBudget = getSpentBudget()
        val plannedBudget = getPlannedBudget()
        return monthlyBudget - spentBudget - plannedBudget
    }
} 