package com.pixelrakete.lovecal.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.pixelrakete.lovecal.data.manager.UserManager
import com.pixelrakete.lovecal.data.model.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate
import java.util.Random

@Singleton
class QuoteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userManager: UserManager
) : QuoteRepository {

    override suspend fun getRandomQuote(): Quote? {
        try {
            val quotes = getAllQuotes()
            return quotes.randomOrNull()
        } catch (e: Exception) {
            throw handleError(e)
        }
    }

    override suspend fun getQuoteOfTheDay(): Quote? {
        try {
            val quotes = getAllQuotes()
            val today = LocalDate.now()
            val seed = today.toEpochDay()
            val random = Random(seed)
            return quotes.getOrNull(random.nextInt(quotes.size))
        } catch (e: Exception) {
            throw handleError(e)
        }
    }

    override suspend fun getAllQuotes(): List<Quote> = withContext(Dispatchers.IO) {
        try {
            val userId = userManager.getCurrentUserId() ?: throw IllegalStateException("No user ID available")
            val snapshot = firestore.collection("quotes")
                .whereEqualTo("createdBy", userId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Quote::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            throw handleError(e)
        }
    }

    override suspend fun getQuoteById(quoteId: String): Quote? = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection("quotes")
                .document(quoteId)
                .get()
                .await()
            
            doc.toObject(Quote::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            throw handleError(e)
        }
    }

    override suspend fun saveQuote(quote: Quote): Boolean = withContext(Dispatchers.IO) {
        try {
            val userId = userManager.getCurrentUserId() ?: throw IllegalStateException("No user ID available")
            val quoteWithUser = quote.copy(createdBy = userId)
            
            if (quote.id.isBlank()) {
                firestore.collection("quotes")
                    .add(quoteWithUser)
                    .await()
            } else {
                firestore.collection("quotes")
                    .document(quote.id)
                    .set(quoteWithUser)
                    .await()
            }
            true
        } catch (e: Exception) {
            throw handleError(e)
        }
    }

    private fun handleError(e: Exception): Exception = when (e) {
        is FirebaseFirestoreException -> IllegalStateException("Failed to access Firestore: ${e.message}")
        is IllegalStateException -> e
        else -> IllegalStateException("Quote error: ${e.message}")
    }
} 