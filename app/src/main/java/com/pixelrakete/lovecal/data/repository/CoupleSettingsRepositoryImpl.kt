package com.pixelrakete.lovecal.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.pixelrakete.lovecal.data.manager.UserManager
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CoupleSettingsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userManager: UserManager
) : CoupleSettingsRepository {

    companion object {
        private const val TAG = "CoupleSettingsRepo"
    }

    private fun getStringList(value: Any?): List<String> {
        return when (value) {
            is List<*> -> value.filterIsInstance<String>()
            else -> emptyList()
        }
    }

    override suspend fun getSettings(): Map<String, Any> {
        val userId = userManager.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        val userDoc = firestore.collection("users").document(userId).get().await()
        val coupleId = userDoc.getString("coupleId") ?: throw IllegalStateException("User not in couple")
        val isPartner1 = userDoc.getBoolean("isPartner1") ?: false

        val coupleDoc = firestore.collection("couples").document(coupleId).get().await()
        
        val settings = mutableMapOf<String, Any>()
        
        val name = if (isPartner1) {
            coupleDoc.getString("partner1Name") ?: ""
        } else {
            coupleDoc.getString("partner2Name") ?: ""
        }
        settings["name"] = name

        val color = if (isPartner1) {
            coupleDoc.getString("partner1Color") ?: "#2196F3"
        } else {
            coupleDoc.getString("partner2Color") ?: "#2196F3"
        }
        settings["color"] = color

        settings["interests"] = getStringList(if (isPartner1) {
            coupleDoc.get("partner1Interests")
        } else {
            coupleDoc.get("partner2Interests")
        })

        val partnerName = if (isPartner1) {
            coupleDoc.getString("partner2Name") ?: ""
        } else {
            coupleDoc.getString("partner1Name") ?: ""
        }
        settings["partnerName"] = partnerName

        val partnerColor = if (isPartner1) {
            coupleDoc.getString("partner2Color") ?: "#FF4081"
        } else {
            coupleDoc.getString("partner1Color") ?: "#FF4081"
        }
        settings["partnerColor"] = partnerColor

        settings["partnerInterests"] = getStringList(if (isPartner1) {
            coupleDoc.get("partner2Interests")
        } else {
            coupleDoc.get("partner1Interests")
        })

        settings["monthlyBudget"] = coupleDoc.getDouble("monthlyBudget") ?: 500.0
        settings["dateFrequencyWeeks"] = coupleDoc.getLong("dateFrequencyWeeks")?.toInt() ?: 2
        
        return settings
    }

    override suspend fun updateSettings(updates: Map<String, Any>) {
        val userId = userManager.getCurrentUserId() ?: throw IllegalStateException("User not authenticated")
        
        try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val coupleId = userDoc.getString("coupleId")
            
            if (coupleId == null) {
                // Create new couple
                Log.d(TAG, "Creating new couple settings")
                val couple = mapOf(
                    "partner1Id" to userId,
                    "partner1Name" to (updates["name"] as? String ?: ""),
                    "partner1Color" to (updates["color"] as? String ?: "#2196F3"),
                    "partner1Interests" to getStringList(updates["interests"]),
                    "partner2Name" to (updates["partnerName"] as? String ?: ""),
                    "partner2Color" to (updates["partnerColor"] as? String ?: "#FF4081"),
                    "partner2Interests" to getStringList(updates["partnerInterests"]),
                    "monthlyBudget" to (updates["monthlyBudget"] as? Double ?: 500.0),
                    "dateFrequencyWeeks" to (updates["dateFrequencyWeeks"] as? Int ?: 2),
                    "createdAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                )
                
                val coupleRef = firestore.collection("couples").add(couple).await()
                
                // Update user document with couple ID
                firestore.collection("users").document(userId)
                    .update(mapOf(
                        "coupleId" to coupleRef.id,
                        "isPartner1" to true
                    ))
                    .await()
            } else {
                // Update existing couple
                Log.d(TAG, "Updating existing couple settings")
                val isPartner1 = userDoc.getBoolean("isPartner1") ?: false
                
                // Map the updates to the correct partner fields
                val mappedUpdates = mutableMapOf<String, Any>()
                updates.forEach { (key, value) ->
                    when (key) {
                        "name" -> mappedUpdates[if (isPartner1) "partner1Name" else "partner2Name"] = value
                        "color" -> mappedUpdates[if (isPartner1) "partner1Color" else "partner2Color"] = value
                        "interests" -> mappedUpdates[if (isPartner1) "partner1Interests" else "partner2Interests"] = getStringList(value)
                        "partnerName" -> mappedUpdates[if (isPartner1) "partner2Name" else "partner1Name"] = value
                        "partnerColor" -> mappedUpdates[if (isPartner1) "partner2Color" else "partner1Color"] = value
                        "partnerInterests" -> mappedUpdates[if (isPartner1) "partner2Interests" else "partner1Interests"] = getStringList(value)
                        else -> mappedUpdates[key] = value
                    }
                }
                mappedUpdates["updatedAt"] = System.currentTimeMillis()
                
                firestore.collection("couples")
                    .document(coupleId)
                    .update(mappedUpdates)
                    .await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating settings", e)
            throw handleError(e)
        }
    }

    private fun handleError(e: Exception): Exception = when (e) {
        is FirebaseFirestoreException -> IllegalStateException("Failed to access Firestore: ${e.message}")
        is IllegalStateException -> e
        else -> IllegalStateException("Settings error: ${e.message}")
    }
} 