package com.pixelrakete.lovecal.data.services.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.BlockThreshold
import com.pixelrakete.lovecal.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class GeminiWrapper @Inject constructor() {
    companion object {
        private const val TAG = "GeminiWrapper"
    }

    private val model = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.GEMINI_API_KEY,
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
        )
    )

    suspend fun generateContent(prompt: String): String? {
        return try {
            val response = model.generateContent(prompt)
            if (response.text == null) {
                throw IllegalStateException("Gemini returned null response")
            }
            response.text
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate content", e)
            throw IllegalStateException("Failed to generate content: ${e.message}")
        }
    }
} 