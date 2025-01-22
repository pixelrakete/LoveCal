package com.pixelrakete.lovecal.data.repository

interface BudgetRepository {
    suspend fun getMonthlyBudget(): Double
    suspend fun updateMonthlyBudget(amount: Double)
    suspend fun getSpentBudget(): Double
    suspend fun getRemainingBudget(): Double
} 