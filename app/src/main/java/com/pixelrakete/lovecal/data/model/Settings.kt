package com.pixelrakete.lovecal.data.model

import com.pixelrakete.lovecal.data.model.validation.ModelValidatable
import com.pixelrakete.lovecal.data.model.validation.ModelValidationResult

data class Settings(
    val id: String = "",
    val monthlyBudget: Double = 500.0,
    val notificationsEnabled: Boolean = true,
    val reminderDays: Int = 7,
    val userId: String = "",
    val invitationCode: String = "U1CVJU",
    val city: String = "Berlin"
) : ModelValidatable {
    companion object {
        const val FIELD_ID = "id"
        const val FIELD_MONTHLY_BUDGET = "monthlyBudget"
        const val FIELD_NOTIFICATIONS_ENABLED = "notificationsEnabled"
        const val FIELD_REMINDER_DAYS = "reminderDays"
        const val FIELD_USER_ID = "userId"
        const val FIELD_CITY = "city"

        private const val MIN_REMINDER_DAYS = 1
        private const val MAX_REMINDER_DAYS = 30
        private const val MIN_MONTHLY_BUDGET = 0.0
        private const val MAX_MONTHLY_BUDGET = 100000.0
    }

    override fun validate(): ModelValidationResult {
        val errors = mutableMapOf<String, String>()

        // Validate monthly budget
        when {
            monthlyBudget < MIN_MONTHLY_BUDGET -> errors[FIELD_MONTHLY_BUDGET] = "Monthly budget cannot be negative"
            monthlyBudget > MAX_MONTHLY_BUDGET -> errors[FIELD_MONTHLY_BUDGET] = "Monthly budget cannot exceed $MAX_MONTHLY_BUDGET"
            monthlyBudget.isNaN() -> errors[FIELD_MONTHLY_BUDGET] = "Monthly budget must be a valid number"
            monthlyBudget.isInfinite() -> errors[FIELD_MONTHLY_BUDGET] = "Monthly budget must be a finite number"
        }

        // Validate reminder days
        when {
            reminderDays < MIN_REMINDER_DAYS -> errors[FIELD_REMINDER_DAYS] = "Reminder days must be at least $MIN_REMINDER_DAYS"
            reminderDays > MAX_REMINDER_DAYS -> errors[FIELD_REMINDER_DAYS] = "Reminder days cannot exceed $MAX_REMINDER_DAYS"
        }

        // Validate userId
        if (userId.isBlank()) {
            errors[FIELD_USER_ID] = "User ID is required"
        }

        // Validate city
        if (city.isBlank()) {
            errors[FIELD_CITY] = "City is required"
        }

        return if (errors.isEmpty()) ModelValidationResult.valid() else ModelValidationResult.invalid(errors)
    }
} 