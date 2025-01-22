package com.pixelrakete.lovecal.data.cache

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetCache @Inject constructor() {
    private var monthlyBudget: Double? = null
    private var spentBudget: Double? = null
    private var plannedBudget: Double? = null
    private var lastCacheUpdate: Long = 0L

    companion object {
        private const val CACHE_VALIDITY_DURATION = 5 * 60 * 1000L // 5 minutes
    }

    fun getMonthlyBudget(): Double? = monthlyBudget
    fun getSpentBudget(): Double? = spentBudget
    fun getPlannedBudget(): Double? = plannedBudget

    fun saveMonthlyBudget(amount: Double) {
        monthlyBudget = amount
        updateCacheTimestamp()
    }

    fun saveSpentBudget(amount: Double) {
        spentBudget = amount
        updateCacheTimestamp()
    }

    fun savePlannedBudget(amount: Double) {
        plannedBudget = amount
        updateCacheTimestamp()
    }

    fun invalidateCache() {
        monthlyBudget = null
        spentBudget = null
        plannedBudget = null
        lastCacheUpdate = 0L
    }

    fun isCacheValid(): Boolean {
        return System.currentTimeMillis() - lastCacheUpdate < CACHE_VALIDITY_DURATION
    }

    private fun updateCacheTimestamp() {
        lastCacheUpdate = System.currentTimeMillis()
    }
} 