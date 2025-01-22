package com.pixelrakete.lovecal.data.manager

import android.content.Context
import android.util.Log
import com.google.api.client.googleapis.batch.BatchRequest
import com.google.api.client.googleapis.batch.json.JsonBatchCallback
import com.google.api.client.googleapis.json.GoogleJsonError
import com.google.api.client.http.HttpHeaders
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.Events
import com.google.api.services.calendar.model.EventReminder
import com.pixelrakete.lovecal.data.model.CalendarError
import com.pixelrakete.lovecal.data.model.DatePlan
import com.pixelrakete.lovecal.util.RateLimiter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class CalendarManager @Inject constructor(
    private val calendar: Calendar,
    private val rateLimiter: RateLimiter,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CalendarManager"
        private const val BATCH_SIZE = 50 // Maximum batch size for Calendar API
        private const val CALENDAR_API_RATE_LIMIT = 100 // Maximum calls per minute to Calendar API
        private const val CALENDAR_API_KEY = "calendar_api"
        
        // Reminder constants in minutes
        private const val REMINDER_ONE_DAY = 24 * 60 // 1 day in minutes
        private const val REMINDER_THREE_HOURS = 3 * 60 // 3 hours in minutes
    }

    init {
        rateLimiter.setRateLimit(CALENDAR_API_KEY, CALENDAR_API_RATE_LIMIT)
    }

    private fun LocalDateTime.toEventDateTime(): EventDateTime {
        val date = Date.from(this.atZone(ZoneId.systemDefault()).toInstant())
        return EventDateTime()
            .setDateTime(com.google.api.client.util.DateTime(date))
            .setTimeZone(ZoneId.systemDefault().id)
    }

    private fun createEventWithReminders(datePlan: DatePlan): Event {
        val now = LocalDateTime.now()
        return Event().apply {
            summary = datePlan.title
            description = datePlan.description
            start = now.toEventDateTime()
            end = now.plusHours(2).toEventDateTime()
            
            // Add reminders
            reminders = Event.Reminders().apply {
                useDefault = false
                overrides = listOf(
                    EventReminder().apply {
                        method = "popup"
                        minutes = REMINDER_ONE_DAY
                    },
                    EventReminder().apply {
                        method = "popup"
                        minutes = REMINDER_THREE_HOURS
                    }
                )
            }
        }
    }

    private fun <T> checkBatchCompletion(
        completed: Int,
        results: List<T>,
        continuation: Continuation<List<T>>
    ) {
        if (completed == results.size) {
            continuation.resume(results)
        }
    }

    private fun checkBatchCompletion(
        completed: Int,
        continuation: Continuation<Unit>
    ) {
        if (completed == 1) {
            continuation.resume(Unit)
        }
    }

    private fun handleCalendarException(
        e: Exception,
        message: String,
        continuation: Continuation<*>
    ) {
        Log.e(TAG, message, e)
        continuation.resumeWithException(mapToCalendarError(e, message))
    }

    private fun mapToCalendarError(e: Exception, message: String): CalendarError {
        return when (e) {
            is IOException -> CalendarError.NetworkError(message, e)
            is SecurityException -> CalendarError.PermissionError(message, e)
            is CalendarError -> e
            else -> CalendarError.UnknownError(message, e)
        }
    }

    suspend fun batchCreateEvents(events: List<Event>): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            val batch = calendar.batch()
            val results = mutableListOf<Event>()
            val callback = object : JsonBatchCallback<Event>() {
                override fun onSuccess(event: Event, responseHeaders: HttpHeaders) {
                    results.add(event)
                }

                override fun onFailure(e: GoogleJsonError, responseHeaders: HttpHeaders) {
                    throw CalendarError.BatchOperationError("Failed to create events", results.map { it.id })
                }
            }

            events.chunked(BATCH_SIZE).forEach { chunk ->
                chunk.forEach { event ->
                    calendar.events().insert("primary", event).queue(batch, callback)
                }
                batch.execute()
            }

            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun batchUpdateEvents(events: List<Event>): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            val batch = calendar.batch()
            val results = mutableListOf<Event>()
            val callback = object : JsonBatchCallback<Event>() {
                override fun onSuccess(event: Event, responseHeaders: HttpHeaders) {
                    results.add(event)
                }

                override fun onFailure(e: GoogleJsonError, responseHeaders: HttpHeaders) {
                    throw CalendarError.BatchOperationError("Failed to update events", results.map { it.id })
                }
            }

            events.chunked(BATCH_SIZE).forEach { chunk ->
                chunk.forEach { event ->
                    calendar.events().update("primary", event.id, event).queue(batch, callback)
                }
                batch.execute()
            }

            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun batchDeleteEvents(eventIds: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val batch = calendar.batch()
            val deletedIds = mutableListOf<String>()
            val callback = object : JsonBatchCallback<Void>() {
                override fun onSuccess(void: Void?, responseHeaders: HttpHeaders) {
                    // Do nothing
                }

                override fun onFailure(e: GoogleJsonError, responseHeaders: HttpHeaders) {
                    throw CalendarError.BatchOperationError("Failed to delete events", deletedIds)
                }
            }

            eventIds.chunked(BATCH_SIZE).forEach { chunk ->
                chunk.forEach { eventId ->
                    calendar.events().delete("primary", eventId).queue(batch, callback)
                    deletedIds.add(eventId)
                }
                batch.execute()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun batchGetEvents(eventIds: List<String>): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            val batch = calendar.batch()
            val results = mutableListOf<Event>()
            val callback = object : JsonBatchCallback<Event>() {
                override fun onSuccess(event: Event, responseHeaders: HttpHeaders) {
                    results.add(event)
                }

                override fun onFailure(e: GoogleJsonError, responseHeaders: HttpHeaders) {
                    throw CalendarError.BatchOperationError("Failed to get events", results.map { it.id })
                }
            }

            eventIds.chunked(BATCH_SIZE).forEach { chunk ->
                chunk.forEach { eventId ->
                    calendar.events().get("primary", eventId).queue(batch, callback)
                }
                batch.execute()
            }

            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 