package com.pixelrakete.lovecal.data.manager

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataRefreshManager @Inject constructor() {
    companion object {
        private const val TAG = "DataRefreshManager"
        private const val REFRESH_INTERVAL = 5 * 60 * 1000L // 5 minutes in milliseconds
    }

    private val _refreshState = MutableStateFlow<RefreshState>(RefreshState.Idle)
    val refreshState: StateFlow<RefreshState> = _refreshState

    private val lastRefreshTimes = mutableMapOf<String, Long>()

    fun shouldRefresh(key: String): Boolean {
        val lastRefresh = lastRefreshTimes[key] ?: 0L
        val currentTime = Instant.now().toEpochMilli()
        val shouldRefresh = currentTime - lastRefresh > REFRESH_INTERVAL
        Log.d(TAG, "Checking refresh for $key: last=$lastRefresh, current=$currentTime, should=$shouldRefresh")
        return shouldRefresh
    }

    fun markRefreshed(key: String) {
        lastRefreshTimes[key] = Instant.now().toEpochMilli()
        Log.d(TAG, "Marked $key as refreshed at ${lastRefreshTimes[key]}")
    }

    fun invalidateData(key: String) {
        lastRefreshTimes.remove(key)
        Log.d(TAG, "Invalidated data for $key")
    }

    fun setRefreshing() {
        _refreshState.value = RefreshState.Refreshing
    }

    fun setIdle() {
        _refreshState.value = RefreshState.Idle
    }

    fun setError(error: String) {
        _refreshState.value = RefreshState.Error(error)
    }
}

sealed class RefreshState {
    object Idle : RefreshState()
    object Refreshing : RefreshState()
    data class Error(val message: String) : RefreshState()
} 