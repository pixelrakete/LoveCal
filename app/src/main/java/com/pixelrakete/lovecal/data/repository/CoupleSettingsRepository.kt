package com.pixelrakete.lovecal.data.repository

interface CoupleSettingsRepository {
    suspend fun getSettings(): Map<String, Any>
    suspend fun updateSettings(settings: Map<String, Any>)
} 