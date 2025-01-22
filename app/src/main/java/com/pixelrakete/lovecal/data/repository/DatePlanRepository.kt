package com.pixelrakete.lovecal.data.repository

import com.pixelrakete.lovecal.data.model.DatePlan
import java.time.LocalDateTime

interface DatePlanRepository {
    suspend fun createDatePlan(
        title: String,
        description: String,
        location: String,
        startDateTime: LocalDateTime,
        budget: Double,
        isSurprise: Boolean
    ): String
    suspend fun getDatePlan(id: String): DatePlan?
    suspend fun getDatePlans(): List<DatePlan>
    suspend fun updateDatePlan(datePlan: DatePlan)
    suspend fun deleteDatePlan(id: String)
} 