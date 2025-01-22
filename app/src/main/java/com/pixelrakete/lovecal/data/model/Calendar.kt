package com.pixelrakete.lovecal.data.model

import com.pixelrakete.lovecal.data.model.validation.ModelValidatable
import com.pixelrakete.lovecal.data.model.validation.ModelValidationResult

data class Calendar(
    val id: String,
    val name: String,
    val description: String,
    val timeZone: String,
    val isReadOnly: Boolean,
    val isPrimary: Boolean,
    val canModifyEvents: Boolean,
    val accountName: String = "",
    val accountType: String = "",
    val ownerName: String = "",
    val displayName: String = "",
    val hasWriteAccess: Boolean = false
) : ModelValidatable {
    companion object {
        const val FIELD_ID = "id"
        const val FIELD_NAME = "name"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_TIME_ZONE = "timeZone"
        const val FIELD_IS_READ_ONLY = "isReadOnly"
        const val FIELD_IS_PRIMARY = "isPrimary"
        const val FIELD_CAN_MODIFY_EVENTS = "canModifyEvents"
        const val FIELD_ACCOUNT_NAME = "accountName"
        const val FIELD_ACCOUNT_TYPE = "accountType"
        const val FIELD_OWNER_NAME = "ownerName"
        const val FIELD_DISPLAY_NAME = "displayName"
        const val FIELD_HAS_WRITE_ACCESS = "hasWriteAccess"

        private const val MAX_NAME_LENGTH = 100
        private const val MAX_DESCRIPTION_LENGTH = 500
    }

    override fun validate(): ModelValidationResult {
        val errors = mutableMapOf<String, String>()

        // Validate id
        if (id.isBlank()) {
            errors[FIELD_ID] = "Calendar ID is required"
        }

        // Validate name
        when {
            name.isBlank() -> errors[FIELD_NAME] = "Calendar name is required"
            name.length > MAX_NAME_LENGTH -> errors[FIELD_NAME] = "Calendar name cannot exceed $MAX_NAME_LENGTH characters"
        }

        // Validate description
        if (description.length > MAX_DESCRIPTION_LENGTH) {
            errors[FIELD_DESCRIPTION] = "Calendar description cannot exceed $MAX_DESCRIPTION_LENGTH characters"
        }

        // Validate timeZone
        if (timeZone.isBlank()) {
            errors[FIELD_TIME_ZONE] = "Time zone is required"
        }

        // Validate logical consistency
        if (isReadOnly && canModifyEvents) {
            errors[FIELD_CAN_MODIFY_EVENTS] = "Cannot modify events in a read-only calendar"
        }

        return if (errors.isEmpty()) ModelValidationResult.valid() else ModelValidationResult.invalid(errors)
    }
} 