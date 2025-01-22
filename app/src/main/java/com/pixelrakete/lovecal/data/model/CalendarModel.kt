package com.pixelrakete.lovecal.data.model

import com.pixelrakete.lovecal.data.model.validation.ModelValidatable
import com.pixelrakete.lovecal.data.model.validation.ModelValidationResult

data class CalendarModel(
    val id: String,
    val accountName: String,
    val displayName: String,
    val ownerAccount: String,
    val description: String,
    val timeZone: String,
    val isReadOnly: Boolean,
    val isPrimary: Boolean,
    val canModifyEvents: Boolean
) : ModelValidatable {
    companion object {
        const val FIELD_ID = "id"
        const val FIELD_ACCOUNT_NAME = "accountName"
        const val FIELD_DISPLAY_NAME = "displayName"
        const val FIELD_OWNER_ACCOUNT = "ownerAccount"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_TIME_ZONE = "timeZone"
        const val FIELD_IS_READ_ONLY = "isReadOnly"
        const val FIELD_IS_PRIMARY = "isPrimary"
        const val FIELD_CAN_MODIFY_EVENTS = "canModifyEvents"
    }

    override fun validate(): ModelValidationResult {
        val errors = mutableMapOf<String, String>()

        if (id.isBlank()) {
            errors[FIELD_ID] = "Calendar ID is required"
        }

        if (displayName.isBlank()) {
            errors[FIELD_DISPLAY_NAME] = "Display name is required"
        }

        if (timeZone.isBlank()) {
            errors[FIELD_TIME_ZONE] = "Time zone is required"
        }

        return if (errors.isEmpty()) ModelValidationResult.valid() else ModelValidationResult.invalid(errors)
    }
} 