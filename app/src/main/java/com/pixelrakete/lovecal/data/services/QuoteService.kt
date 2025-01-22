package com.pixelrakete.lovecal.data.services

import android.util.Log
import com.pixelrakete.lovecal.data.model.Quote
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteService @Inject constructor() {
    companion object {
        private const val TAG = "QuoteService"
        private val QUOTES = listOf(
            Quote(
                text = "Liebe ist das brennende Feuer der Sehnsucht, die unermüdlich verzehrt, bis sie vereint wird.",
                author = "Khalil Gibran"
            ),
            Quote(
                text = "Die Liebe ist die stärkste Macht der Welt, und doch ist sie die demütigste, die man sich vorstellen kann.",
                author = "Mahatma Gandhi"
            ),
            Quote(
                text = "Wo die Liebe wohnt, ist der Himmel.",
                author = "Johann Wolfgang von Goethe"
            ),
            Quote(
                text = "Die Liebe ist wie der Wind, du kannst sie nicht sehen, aber du kannst sie fühlen.",
                author = "Nicholas Sparks"
            ),
            Quote(
                text = "Die Liebe besteht nicht darin, dass man einander ansieht, sondern dass man gemeinsam in die gleiche Richtung blickt.",
                author = "Antoine de Saint-Exupéry"
            )
        )
    }

    private var cachedQuote: Quote? = null
    private var lastGeneratedDate: String? = null

    suspend fun getRandomQuote(): Quote? {
        val today = java.time.LocalDate.now().toString()
        
        // Return cached quote if available for today
        if (today == lastGeneratedDate && cachedQuote != null) {
            Log.d(TAG, "Returning cached quote for $today: ${cachedQuote?.text} by ${cachedQuote?.author}")
            return cachedQuote
        }

        // Select a new quote for today
        val todaysSeed = today.hashCode()
        val random = java.util.Random(todaysSeed.toLong())
        val selectedQuote = QUOTES[random.nextInt(QUOTES.size)]
        
        // Cache the quote
        cachedQuote = selectedQuote
        lastGeneratedDate = today
        
        Log.d(TAG, "Selected new quote for $today: ${selectedQuote.text} by ${selectedQuote.author}")
        return selectedQuote
    }
} 