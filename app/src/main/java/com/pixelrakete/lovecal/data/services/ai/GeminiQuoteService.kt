package com.pixelrakete.lovecal.data.services.ai

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.BlockThreshold
import com.pixelrakete.lovecal.data.model.Quote
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class GeminiQuoteService @Inject constructor(
    @Named("QuoteModel") private val generativeModel: GenerativeModel
) {
    companion object {
        private const val TAG = "GeminiQuoteService"
    }

    private var cachedQuote: Quote? = null
    private var lastGeneratedDate: String? = null

    suspend fun generateLoveQuote(): Quote {
        // Check if we already have a quote for today
        val today = java.time.LocalDate.now().toString()
        if (today == lastGeneratedDate && cachedQuote != null) {
            Log.d(TAG, "Returning cached quote from $lastGeneratedDate: ${cachedQuote?.text}")
            return cachedQuote!!
        }

        Log.d(TAG, "Generating new quote for $today (last generated: $lastGeneratedDate)")
        
        try {
            val prompt = """
                Generate a romantic love quote that is:
                1. Short and sweet (max 50 characters)
                2. Deep and meaningful
                3. Original (not from any existing source)
                4. Personal and intimate
                5. Focused on love, relationships, and connection
                6. Written in first or second person
                
                The quote should feel like a genuine expression of love between partners.
                Do not include any prefixes like "quote:" or "author:" in the response.
                Just return the quote text directly.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt).text?.trim() ?: 
                throw IllegalStateException("Failed to generate quote")
            
            Log.d(TAG, "Generated quote: $response")
            
            // Clean up the response
            val cleanQuote = response
                .replace("quote:", "")
                .replace("author:", "")
                .replace("\"", "")
                .trim()
            
            // Cache the quote
            cachedQuote = Quote(
                text = cleanQuote,
                author = "Love AI"
            )
            lastGeneratedDate = today
            
            Log.d(TAG, "Cached new quote for $today: ${cachedQuote?.text}")
            
            return cachedQuote!!
        } catch (e: Exception) {
            Log.e(TAG, "Error generating quote", e)
            return Quote(
                text = "Every moment with you is a gift",
                author = "Love AI"
            )
        }
    }
} 