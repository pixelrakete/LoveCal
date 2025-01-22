package com.pixelrakete.lovecal.data.manager

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.pixelrakete.lovecal.data.model.DatePlan
import com.pixelrakete.lovecal.data.model.Quote
import com.pixelrakete.lovecal.data.model.DateWish
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import android.content.SharedPreferences
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Singleton
class DataSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val sharedPreferences: SharedPreferences,
    private val userManager: UserManager
) {
    companion object {
        private const val TAG = "DataSyncManager"
        private const val LAST_SYNC_DATES = "last_sync_dates"
        private const val LAST_SYNC_WISHES = "last_sync_wishes"
        private const val LAST_SYNC_QUOTES = "last_sync_quotes"
        private const val SYNC_INTERVAL = 5 * 60 * 1000L // 5 minutes
        private const val CALENDAR_ID_PREFIX = "calendar_id_"
        private const val CALENDAR_ID_TIMESTAMP_PREFIX = "calendar_id_timestamp_"
        private const val CALENDAR_ID_CACHE_DURATION = 24 * 60 * 60 * 1000L // 24 hours
        private const val BATCH_SIZE = 500
    }

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState

    sealed class SyncState {
        object Idle : SyncState()
        object Syncing : SyncState()
        data class Error(val message: String) : SyncState()
        object Success : SyncState()
    }

    // Synchronize all data for a couple
    suspend fun syncAllData(coupleId: String) = coroutineScope {
        try {
            _syncState.value = SyncState.Syncing
            Log.d(TAG, "Starting full data sync for couple $coupleId")

            val datesDeferred = async { syncDates(coupleId) }
            val wishesDeferred = async { 
                val userId = userManager.getCurrentUserId() ?: return@async emptyList()
                syncWishes(userId)
            }
            val quotesDeferred = async { syncQuotes(coupleId) }

            // Wait for all syncs to complete
            val dates = datesDeferred.await()
            val wishes = wishesDeferred.await()
            val quotes = quotesDeferred.await()

            Log.d(TAG, "Full sync completed. Synced ${dates.size} dates, ${wishes.size} wishes, ${quotes.size} quotes")
            _syncState.value = SyncState.Success
        } catch (e: Exception) {
            Log.e(TAG, "Error during full sync", e)
            _syncState.value = SyncState.Error("Failed to sync all data: ${e.message}")
        }
    }

    suspend fun syncDates(coupleId: String): List<DatePlan> {
        if (!shouldSync(LAST_SYNC_DATES)) {
            Log.d(TAG, "Using cached dates, sync not needed")
            return getCachedDates(coupleId)
        }

        return try {
            Log.d(TAG, "Starting date sync for couple $coupleId")
            
            val serverDates = mutableListOf<DatePlan>()
            var lastDoc: com.google.firebase.firestore.DocumentSnapshot? = null
            
            // Fetch dates in batches
            do {
                val query = firestore.collection("dates")
                    .whereEqualTo("coupleId", coupleId)
                    .limit(BATCH_SIZE.toLong())
                
                if (lastDoc != null) {
                    query.startAfter(lastDoc)
                }
                
                val snapshot = query.get(Source.SERVER).await()
                val batchDates = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(DatePlan::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing date document ${doc.id}", e)
                        null
                    }
                }
                
                serverDates.addAll(batchDates)
                lastDoc = snapshot.documents.lastOrNull()
            } while (lastDoc != null && serverDates.size % BATCH_SIZE == 0)

            // Cache the dates
            cacheDates(serverDates)
            updateLastSyncTime(LAST_SYNC_DATES)
            
            Log.d(TAG, "Successfully synced ${serverDates.size} dates")
            serverDates
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing dates", e)
            getCachedDates(coupleId)
        }
    }

    private suspend fun getCachedDates(coupleId: String): List<DatePlan> {
        return try {
            firestore.collection("dates")
                .whereEqualTo("coupleId", coupleId)
                .get(Source.CACHE)
                .await()
                .documents
                .mapNotNull { doc ->
                    try {
                        doc.toObject(DatePlan::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing cached date document ${doc.id}", e)
                        null
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cached dates", e)
            emptyList()
        }
    }

    private fun cacheDates(dates: List<DatePlan>) {
        // Store in Firestore cache
        dates.forEach { date ->
            firestore.collection("dates")
                .document(date.id)
                .set(date)
        }
    }

    private fun shouldSync(key: String): Boolean {
        val lastSync = sharedPreferences.getLong(key, 0)
        val currentTime = System.currentTimeMillis()
        val shouldSync = currentTime - lastSync > SYNC_INTERVAL
        Log.d(TAG, "Should sync $key: $shouldSync (last sync: ${LocalDateTime.ofInstant(Instant.ofEpochMilli(lastSync), ZoneId.systemDefault())})")
        return shouldSync
    }

    private fun updateLastSyncTime(key: String) {
        val currentTime = System.currentTimeMillis()
        sharedPreferences.edit()
            .putLong(key, currentTime)
            .apply()
        Log.d(TAG, "Updated last sync time for $key: ${LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTime), ZoneId.systemDefault())}")
    }

    fun clearCache() {
        sharedPreferences.edit().apply {
            remove(LAST_SYNC_DATES)
            remove(LAST_SYNC_WISHES)
            remove(LAST_SYNC_QUOTES)
            apply()
        }
        firestore.clearPersistence()
        // Clear all calendar ID caches
        sharedPreferences.all.keys
            .filter { it.startsWith(CALENDAR_ID_PREFIX) || it.startsWith(CALENDAR_ID_TIMESTAMP_PREFIX) }
            .forEach { key ->
                sharedPreferences.edit().remove(key).apply()
            }
        Log.d(TAG, "Cleared all calendar ID caches")
    }

    suspend fun syncWishes(userId: String): List<DateWish> {
        if (!shouldSync(LAST_SYNC_WISHES)) {
            Log.d(TAG, "Using cached wishes, sync not needed")
            return getCachedWishes(userId)
        }

        return try {
            _syncState.value = SyncState.Syncing
            Log.d(TAG, "Starting wishes sync for user $userId")

            val serverWishes = firestore.collection("wishes")
                .whereEqualTo("creatorId", userId)
                .get(Source.SERVER)
                .await()
                .documents
                .mapNotNull { doc ->
                    try {
                        doc.toObject(DateWish::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing wish document ${doc.id}", e)
                        null
                    }
                }

            // Cache the wishes
            cacheWishes(serverWishes)
            updateLastSyncTime(LAST_SYNC_WISHES)
            _syncState.value = SyncState.Success
            
            serverWishes
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing wishes", e)
            _syncState.value = SyncState.Error("Failed to sync wishes: ${e.message}")
            getCachedWishes(userId)
        }
    }

    private suspend fun getCachedWishes(userId: String): List<DateWish> {
        return try {
            firestore.collection("wishes")
                .whereEqualTo("creatorId", userId)
                .get(Source.CACHE)
                .await()
                .documents
                .mapNotNull { doc ->
                    try {
                        doc.toObject(DateWish::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing cached wish document ${doc.id}", e)
                        null
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cached wishes", e)
            emptyList()
        }
    }

    private fun cacheWishes(wishes: List<DateWish>) {
        wishes.forEach { wish ->
            firestore.collection("wishes")
                .document(wish.id)
                .set(wish)
        }
    }

    suspend fun syncQuotes(coupleId: String): List<Quote> {
        if (!shouldSync(LAST_SYNC_QUOTES)) {
            Log.d(TAG, "Using cached quotes, sync not needed")
            return getCachedQuotes(coupleId)
        }

        return try {
            _syncState.value = SyncState.Syncing
            Log.d(TAG, "Starting quotes sync for couple $coupleId")

            val serverQuotes = firestore.collection("quotes")
                .whereEqualTo("coupleId", coupleId)
                .get(Source.SERVER)
                .await()
                .documents
                .mapNotNull { doc ->
                    try {
                        doc.toObject(Quote::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing quote document ${doc.id}", e)
                        null
                    }
                }

            // Cache the quotes
            cacheQuotes(serverQuotes)
            updateLastSyncTime(LAST_SYNC_QUOTES)
            _syncState.value = SyncState.Success
            
            serverQuotes
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing quotes", e)
            _syncState.value = SyncState.Error("Failed to sync quotes: ${e.message}")
            getCachedQuotes(coupleId)
        }
    }

    private suspend fun getCachedQuotes(coupleId: String): List<Quote> {
        return try {
            firestore.collection("quotes")
                .whereEqualTo("coupleId", coupleId)
                .get(Source.CACHE)
                .await()
                .documents
                .mapNotNull { doc ->
                    try {
                        doc.toObject(Quote::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing cached quote document ${doc.id}", e)
                        null
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cached quotes", e)
            emptyList()
        }
    }

    private fun cacheQuotes(quotes: List<Quote>) {
        quotes.forEach { quote ->
            firestore.collection("quotes")
                .document(quote.id)
                .set(quote)
        }
    }

    fun cacheCalendarId(coupleId: String, calendarId: String) {
        Log.d(TAG, "Caching calendar ID for couple $coupleId: $calendarId")
        sharedPreferences.edit()
            .putString("${CALENDAR_ID_PREFIX}$coupleId", calendarId)
            .putLong("${CALENDAR_ID_TIMESTAMP_PREFIX}$coupleId", System.currentTimeMillis())
            .apply()
    }

    fun getCachedCalendarId(coupleId: String): String? {
        val timestamp = sharedPreferences.getLong("${CALENDAR_ID_TIMESTAMP_PREFIX}$coupleId", 0)
        val currentTime = System.currentTimeMillis()
        
        return if (currentTime - timestamp <= CALENDAR_ID_CACHE_DURATION) {
            sharedPreferences.getString("${CALENDAR_ID_PREFIX}$coupleId", null)?.also {
                Log.d(TAG, "Retrieved cached calendar ID for couple $coupleId: $it")
            }
        } else {
            Log.d(TAG, "Cached calendar ID for couple $coupleId has expired")
            // Clear expired cache
            clearCalendarIdCache(coupleId)
            null
        }
    }

    private fun clearCalendarIdCache(coupleId: String) {
        Log.d(TAG, "Clearing calendar ID cache for couple $coupleId")
        sharedPreferences.edit()
            .remove("${CALENDAR_ID_PREFIX}$coupleId")
            .remove("${CALENDAR_ID_TIMESTAMP_PREFIX}$coupleId")
            .apply()
    }
} 