package com.pixelrakete.lovecal.data.repository

import com.pixelrakete.lovecal.data.model.BudgetValidationResult

interface BudgetRepository {
    suspend fun getMonthlyBudget(): Double
    suspend fun updateMonthlyBudget(amount: Double)
    suspend fun getSpentBudget(): Double
    suspend fun getPlannedBudget(): Double
    suspend fun getRemainingBudget(): Double
    suspend fun validateDateBudget(amount: Double): BudgetValidationResult
} 