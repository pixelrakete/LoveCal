package com.pixelrakete.lovecal.data.services

import com.google.ai.client.generativeai.GenerativeModel
import com.pixelrakete.lovecal.data.model.Quote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteService @Inject constructor(
    private val generativeModel: GenerativeModel?
) {
    private val defaultQuotes = listOf(
        Quote(
            text = "In deinen Armen vergesse ich die Welt und fühle nur noch das Feuer unserer Leidenschaft."
        ),
        Quote(
            text = "Wahre Liebe ist wie eine Flamme, die uns verzehrt und neu erschafft."
        ),
        Quote(
            text = "Deine Berührung lässt mein Herz schneller schlagen und meine Seele brennen."
        ),
        Quote(
            text = "Die Leidenschaft ist der Schlüssel zu den tiefsten Geheimnissen des Lebens."
        ),
        Quote(
            text = "In der Hitze der Nacht verschmelzen unsere Seelen zu einer einzigen Flamme."
        )
    )

    private var lastQuoteIndex = -1

    suspend fun generateLoveQuote(): Quote {
        if (generativeModel == null) {
            android.util.Log.w("QuoteService", "GenerativeModel is null, using default quote")
            return getNextDefaultQuote()
        }

        val prompt = buildString {
            appendLine("Generiere ein kurzes, leidenschaftliches Zitat über Liebe und Begierde.")
            appendLine("Das Zitat sollte:")
            appendLine("- Maximal 2 Sätze lang sein")
            appendLine("- Gefühlvoll und verführerisch sein")
            appendLine("- Auf Deutsch sein")
            appendLine("\nFormat:")
            appendLine("Zitat: [Das Zitat]")
            appendLine("\nWichtig: Das Zitat sollte die Intensität der Leidenschaft und das Verlangen zwischen zwei Menschen ausdrücken.")
            appendLine("\nBeispiele für Themen:")
            appendLine("- Die Hitze der Leidenschaft")
            appendLine("- Das Feuer der Begierde")
            appendLine("- Die Kraft der Anziehung")
            appendLine("- Die Intensität der Liebe")
            appendLine("- Die Sehnsucht nacheinander")
            appendLine("\nHinweis: Das Zitat darf verführerisch sein und sexy Andeutungen haben.")
        }

        try {
            android.util.Log.d("QuoteService", "Attempting to generate quote with Gemini")
            val contentResponse = try {
                generativeModel.generateContent(prompt)
            } catch (e: Exception) {
                android.util.Log.e("QuoteService", "Error calling Gemini API", e)
                return getNextDefaultQuote()
            }

            val response = contentResponse.text
            android.util.Log.d("QuoteService", "Raw Gemini response: $response")
            
            if (response == null) {
                android.util.Log.w("QuoteService", "Gemini returned null response")
                return getNextDefaultQuote()
            }
            
            val quote = parseQuote(response)
            android.util.Log.d("QuoteService", "Parsed quote - text: '${quote.text}'")

            return if (quote.text.isNotBlank()) {
                android.util.Log.d("QuoteService", "Successfully generated quote: ${quote.text}")
                quote
            } else {
                android.util.Log.w("QuoteService", "Generated quote was invalid (text: ${quote.text.isNotBlank()}), using default")
                getNextDefaultQuote()
            }
        } catch (e: Exception) {
            android.util.Log.e("QuoteService", "Unexpected error generating quote", e)
            return getNextDefaultQuote()
        }
    }

    private fun getNextDefaultQuote(): Quote {
        lastQuoteIndex = (lastQuoteIndex + 1) % defaultQuotes.size
        val quote = defaultQuotes[lastQuoteIndex]
        android.util.Log.d("QuoteService", "Using default quote: ${quote.text}")
        return quote
    }

    private fun parseQuote(response: String): Quote {
        var text = ""

        android.util.Log.d("QuoteService", "Parsing response lines:")
        response.lines().forEach { line ->
            android.util.Log.d("QuoteService", "Processing line: $line")
            if (line.startsWith("Zitat:")) {
                text = line.substringAfter(":").trim()
                android.util.Log.d("QuoteService", "Found quote text: $text")
            }
        }

        return Quote(text = text)
    }
} 