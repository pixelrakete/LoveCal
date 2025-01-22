package com.pixelrakete.lovecal.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pixelrakete.lovecal.data.model.Quote
import com.pixelrakete.lovecal.data.services.QuoteService
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val quoteService: QuoteService
) : QuoteRepository {
    private val quotesCollection = firestore.collection("quotes")

    override suspend fun getRandomQuote(): Quote? {
        if (auth.currentUser == null) {
            Log.e("QuoteRepository", "User not authenticated")
            return null
        }

        return try {
            // Try to get a random quote from Firestore first
            val randomQuote = try {
                quotesCollection
                    .limit(1)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()
                    ?.toObject(Quote::class.java)
                    ?.let { quote ->
                        if (quote.author.isBlank()) quote.copy(author = "Unbekannt") else quote
                    }
            } catch (e: Exception) {
                Log.e("QuoteRepository", "Error getting random quote from Firestore", e)
                null
            }

            // If no quote is found or there's an error, generate a new one
            if (randomQuote == null) {
                val newQuote = quoteService.generateLoveQuote().let { quote ->
                    quote.copy(author = if (quote.author.isBlank()) "Unbekannt" else quote.author)
                }
                if (newQuote.text.isNotBlank()) {
                    val saved = saveQuote(newQuote)
                    if (saved) {
                        Log.d("QuoteRepository", "Successfully saved new quote to Firebase")
                    } else {
                        Log.w("QuoteRepository", "Failed to save quote to Firebase")
                    }
                    newQuote
                } else {
                    null
                }
            } else {
                randomQuote
            }
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error in getRandomQuote", e)
            // If all else fails, generate a quote without saving
            quoteService.generateLoveQuote().let { quote ->
                quote.copy(author = if (quote.author.isBlank()) "Unbekannt" else quote.author)
            }
        }
    }

    override suspend fun saveQuote(quote: Quote): Boolean {
        if (auth.currentUser == null) {
            Log.e("QuoteRepository", "User not authenticated")
            return false
        }

        return try {
            val quoteId = quote.id.ifBlank { UUID.randomUUID().toString() }
            val quoteWithId = quote.copy(
                id = quoteId,
                author = if (quote.author.isBlank()) "Unbekannt" else quote.author
            )
            quotesCollection.document(quoteId).set(quoteWithId).await()
            true
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error saving quote", e)
            false
        }
    }

    override suspend fun getQuoteById(id: String): Quote? {
        if (auth.currentUser == null) {
            Log.e("QuoteRepository", "User not authenticated")
            return null
        }

        return try {
            val document = quotesCollection.document(id).get().await()
            document.toObject(Quote::class.java)?.let { quote ->
                if (quote.author.isBlank()) quote.copy(author = "Unbekannt") else quote
            }
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error getting quote by id", e)
            null
        }
    }

    override suspend fun getAllQuotes(): List<Quote> {
        if (auth.currentUser == null) {
            Log.e("QuoteRepository", "User not authenticated")
            return emptyList()
        }

        return try {
            quotesCollection.get().await().toObjects(Quote::class.java)
                .map { quote ->
                    if (quote.author.isBlank()) quote.copy(author = "Unbekannt") else quote
                }
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error getting all quotes", e)
            emptyList()
        }
    }

    override suspend fun getQuoteOfTheDay(): Quote? {
        if (auth.currentUser == null) {
            Log.e("QuoteRepository", "User not authenticated")
            return null
        }

        return try {
            getRandomQuote()
        } catch (e: Exception) {
            Log.e("QuoteRepository", "Error getting quote of the day", e)
            null
        }
    }
} 