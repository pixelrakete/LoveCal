package com.pixelrakete.lovecal.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CoupleSettingsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CoupleSettingsRepository {

    override suspend fun getSettings(): Map<String, Any> {
        return try {
            val document = firestore.collection("couple_settings")
                .document("current")
                .get()
                .await()
            document.data ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    override suspend fun updateSettings(settings: Map<String, Any>) {
        try {
            firestore.collection("couple_settings")
                .document("current")
                .set(settings)
                .await()
        } catch (e: Exception) {
            throw e
        }
    }
} 