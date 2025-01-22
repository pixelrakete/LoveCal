package com.pixelrakete.lovecal.util

import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RateLimiter @Inject constructor() {
    private val rateLimits = ConcurrentHashMap<String, Int>()
    private val requestCounts = ConcurrentHashMap<String, Int>()
    private val lastResetTimes = ConcurrentHashMap<String, Long>()
    private val MINUTE_IN_MILLIS = 60_000L

    fun setRateLimit(key: String, limit: Int) {
        rateLimits[key] = limit
        requestCounts[key] = 0
        lastResetTimes[key] = System.currentTimeMillis()
    }

    suspend fun waitForRateLimit(key: String) {
        val limit = rateLimits[key] ?: return
        val currentTime = System.currentTimeMillis()
        val lastResetTime = lastResetTimes[key] ?: currentTime
        val count = requestCounts[key] ?: 0

        // Reset counter if a minute has passed
        if (currentTime - lastResetTime >= MINUTE_IN_MILLIS) {
            requestCounts[key] = 1
            lastResetTimes[key] = currentTime
            return
        }

        // If we've hit the limit, wait until the next minute
        if (count >= limit) {
            val waitTime = MINUTE_IN_MILLIS - (currentTime - lastResetTime)
            delay(waitTime)
            requestCounts[key] = 1
            lastResetTimes[key] = System.currentTimeMillis()
        } else {
            requestCounts[key] = count + 1
        }
    }

    fun clearRateLimit(key: String) {
        rateLimits.remove(key)
        requestCounts.remove(key)
        lastResetTimes.remove(key)
    }
} 