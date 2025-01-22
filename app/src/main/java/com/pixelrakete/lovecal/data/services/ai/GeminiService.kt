package com.pixelrakete.lovecal.data.services.ai

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pixelrakete.lovecal.data.manager.UserManager
import com.pixelrakete.lovecal.data.model.DateSuggestion
import com.pixelrakete.lovecal.data.model.DateWish
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class GeminiService @Inject constructor(
    private val gson: Gson,
    private val userManager: UserManager,
    private val geminiWrapper: GeminiWrapper
) {
    companion object {
        private const val TAG = "GeminiService"
        private const val MAX_RETRIES = 3
    }

    suspend fun generateDateSuggestions(randomWish: DateWish? = null): List<DateSuggestion> {
        val settings = userManager.getCoupleSettings() ?: return emptyList()
        val city = settings.city ?: "München"
        
        repeat(MAX_RETRIES) { attempt ->
            try {
                val prompt = """
                    Generiere genau 5 einzigartige und kreative Date-Ideen für ein Paar in $city.
                    ${if (randomWish != null) "Eine der Vorschläge MUSS auf diesem Date-Wunsch basieren: ${randomWish.title} - ${randomWish.description}" else ""}
                    
                    Anforderungen:
                    - Schreibe GENAU 5 Vorschläge
                    - Die meisten Vorschläge sollten in $city oder der näheren Umgebung sein
                    - Ein Vorschlag kann auch ein Wochenendtrip oder Kurzurlaub sein
                    - Jeder Vorschlag MUSS exakt diesem Format folgen:
                      TITLE: [kurzer, prägnanter Titel für das Date]
                      LOCATION: [konkrete Location für das Date]
                      BUDGET: [geschätztes Budget in €]
                      DESCRIPTION: [Beschreibung in "Wir"-Form]
                    - Die DESCRIPTION soll:
                      - "Wir"-Form verwenden (z.B. "Wir werden...", "Lass uns...", "Gemeinsam...")
                      - Mindestens 3 Vorschläge sollen sehr leidenschaftlich und intim sein
                      - Spezifische gemeinsame Aktivitäten enthalten
                      - Wenn möglich nur zur Jahreszeit passende Aktivitäten vorschlagen
                      - 3-4 Sätze lang sein
                      - Sehr romantische, intime und sinnliche Elemente enthalten
                      - Kreativ und spezifisch sein, keine generischen Vorschläge
                      - Erotische und leidenschaftliche Untertöne sind erwünscht
                      - Ein Vorschlag soll sehr direkt und explizit intim sein
                      - Ein Vorschlag darf auch deutlich verrückter sein (z.B. "Picknick im Garten bei Kerzenschein")
                    
                    Gib GENAU 5 Vorschläge zurück, getrennt durch '|||'.
                    Jeder Vorschlag MUSS alle 4 Felder enthalten (TITLE, LOCATION, BUDGET, DESCRIPTION).
                    Jeder Vorschlag MUSS mit 'TITLE:' beginnen.
                    Budget darf nur einen Betrag mit Währung enthalten: z.B. € 50,-
                """.trimIndent()
                
                Log.d(TAG, "Attempt ${attempt + 1} to generate suggestions")
                val response = geminiWrapper.generateContent(prompt) 
                    ?: throw IllegalStateException("No response from Gemini")
                
                Log.d(TAG, "Received response from Gemini")
                
                val suggestions = response.split("|||")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                
                if (suggestions.size != 5) {
                    if (attempt < MAX_RETRIES - 1) {
                        Log.w(TAG, "Wrong number of suggestions (${suggestions.size}), retrying...")
                        throw IllegalStateException("Expected 5 suggestions, got ${suggestions.size}")
                    }
                    Log.w(TAG, "Wrong number of suggestions (${suggestions.size}) on final attempt, proceeding anyway")
                }
                
                return suggestions.map { suggestion ->
                    try {
                        parseDateSuggestion(suggestion)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse suggestion: $suggestion", e)
                        throw IllegalStateException("Failed to parse suggestion: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error on attempt ${attempt + 1}", e)
                if (attempt == MAX_RETRIES - 1) {
                    throw IllegalStateException("Failed to generate valid suggestions after $MAX_RETRIES attempts: ${e.message}")
                }
            }
        }
        
        throw IllegalStateException("Failed to generate valid suggestions after $MAX_RETRIES attempts")
    }

    private fun parseDateSuggestion(text: String): DateSuggestion {
        Log.d(TAG, "Parsing suggestion: $text")
        
        val lines = text.split("\n").filter { it.isNotBlank() }
        
        if (lines.size < 4) {
            val error = "Invalid suggestion format: Not enough lines"
            Log.e(TAG, "$error\nReceived text: $text")
            throw IllegalStateException(error)
        }
        
        if (!lines[0].startsWith("TITLE:")) {
            val error = "First line must start with TITLE:"
            Log.e(TAG, "$error\nReceived text: $text")
            throw IllegalStateException(error)
        }
        
        try {
            val title = lines.find { it.startsWith("TITLE:") }?.substringAfter("TITLE:")?.trim()
                ?: throw IllegalStateException("Missing TITLE field")
            Log.d(TAG, "Parsed title: $title")
            
            val location = lines.find { it.startsWith("LOCATION:") }?.substringAfter("LOCATION:")?.trim()
                ?: throw IllegalStateException("Missing LOCATION field")
            Log.d(TAG, "Parsed location: $location")
            
            val budgetStr = lines.find { it.startsWith("BUDGET:") }?.substringAfter("BUDGET:")?.trim()
                ?: throw IllegalStateException("Missing BUDGET field")
            Log.d(TAG, "Parsing budget string: $budgetStr")
            
            val budget = parseBudget(budgetStr)
            Log.d(TAG, "Parsed budget: $budget")
            
            val description = lines.find { it.startsWith("DESCRIPTION:") }?.substringAfter("DESCRIPTION:")?.trim()
                ?: throw IllegalStateException("Missing DESCRIPTION field")
            Log.d(TAG, "Parsed description: $description")
            
            return DateSuggestion(
                title = title,
                location = location,
                budget = budget,
                description = description
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing suggestion", e)
            throw e
        }
    }

    private fun parseBudget(budgetStr: String): Double {
        Log.d(TAG, "Parsing budget string: $budgetStr")
        return when {
            budgetStr.lowercase() in listOf("kostenlos", "gratis", "free", "0", "0.0", "0€") -> {
                Log.d(TAG, "Parsed as free (0.0)")
                0.0
            }
            else -> try {
                // Extract the first number from the string
                val numberRegex = """(\d+(?:[,.]\d+)?)""".toRegex()
                val match = numberRegex.find(budgetStr)?.value
                    ?.replace(",", ".")
                    ?.toDouble()
                    ?: throw IllegalStateException("No number found in budget string")
                
                Log.d(TAG, "Parsed budget number: $match")
                match
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse budget", e)
                throw IllegalStateException("Failed to parse budget: $budgetStr")
            }
        }
    }

    fun <T> parseJsonResponse(jsonString: String): T {
        return gson.fromJson(jsonString, object : TypeToken<T>() {}.type)
    }
} 