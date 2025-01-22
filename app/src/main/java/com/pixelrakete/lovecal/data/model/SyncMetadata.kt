package com.pixelrakete.lovecal.data.model

import java.util.Date

data class SyncMetadata(
    val version: Long = 0,
    val lastModified: Date = Date(),
    val deviceId: String = "",
    val modifiedBy: String = "",
    val isDeleted: Boolean = false
) {
    fun incrementVersion(): SyncMetadata {
        return copy(
            version = version + 1,
            lastModified = Date()
        )
    }

    fun markDeleted(userId: String): SyncMetadata {
        return copy(
            version = version + 1,
            lastModified = Date(),
            modifiedBy = userId,
            isDeleted = true
        )
    }
} 