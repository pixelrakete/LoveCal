package com.pixelrakete.lovecal.data.repository

import com.pixelrakete.lovecal.data.model.DatePlan
import kotlinx.coroutines.flow.Flow

interface DatePlanRepository {
    suspend fun createDatePlan(
        title: String,
        description: String?,
        location: String?,
        budget: Double?,
        dateTimeStr: String?,
        isSurprise: Boolean
    ): DatePlan

    suspend fun updateDatePlan(
        id: String,
        title: String,
        description: String?,
        location: String?,
        budget: Double?,
        dateTimeStr: String?,
        isSurprise: Boolean
    ): DatePlan

    fun getDatePlans(): Flow<List<DatePlan>>

    suspend fun getDatePlan(id: String): DatePlan?

    suspend fun deleteDatePlan(id: String)

    suspend fun markDatePlanAsCompleted(id: String)

    suspend fun updateDatePlanCompleted(id: String, completed: Boolean)
} 