package com.pixelrakete.lovecal.data.model

import com.pixelrakete.lovecal.data.model.validation.ModelValidatable
import com.pixelrakete.lovecal.data.model.validation.ModelValidationResult
import java.util.Date

data class Quote(
    val id: String = "",
    val text: String = "",
    val author: String = "",
    val createdBy: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isArchived: Boolean = false,
    val isQuoteOfTheDay: Boolean = false,
    val syncMetadata: SyncMetadata = SyncMetadata()
) : ModelValidatable {
    companion object {
        private const val MAX_TEXT_LENGTH = 1000
        private const val MAX_AUTHOR_LENGTH = 100
    }

    override fun validate(): ModelValidationResult {
        val errors = mutableMapOf<String, String>()

        // Validate text
        when {
            text.isBlank() -> errors["text"] = "Quote text is required"
            text.length > MAX_TEXT_LENGTH -> errors["text"] = "Quote text cannot exceed $MAX_TEXT_LENGTH characters"
        }

        // Validate author
        if (author.length > MAX_AUTHOR_LENGTH) {
            errors["author"] = "Author name cannot exceed $MAX_AUTHOR_LENGTH characters"
        }

        // Validate createdBy
        if (createdBy.isBlank()) {
            errors["createdBy"] = "Creator ID is required"
        }

        return if (errors.isEmpty()) ModelValidationResult.valid() else ModelValidationResult.invalid(errors)
    }

    fun incrementVersion(userId: String, deviceId: String): Quote {
        return copy(
            updatedAt = Date(),
            syncMetadata = syncMetadata.copy(
                version = syncMetadata.version + 1,
                lastModified = Date(),
                modifiedBy = userId,
                deviceId = deviceId
            )
        )
    }

    fun markDeleted(userId: String, deviceId: String): Quote {
        return copy(
            isArchived = true,
            updatedAt = Date(),
            syncMetadata = syncMetadata.copy(
                version = syncMetadata.version + 1,
                lastModified = Date(),
                modifiedBy = userId,
                deviceId = deviceId,
                isDeleted = true
            )
        )
    }
} 