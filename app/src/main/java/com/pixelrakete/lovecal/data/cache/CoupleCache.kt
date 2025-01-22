package com.pixelrakete.lovecal.data.cache

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.pixelrakete.lovecal.data.model.Couple
import com.pixelrakete.lovecal.util.nullSafeOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoupleCache @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    companion object {
        private const val PREFS_NAME = "couple_cache"
        private const val KEY_CURRENT_COUPLE = "current_couple"
        private const val KEY_LAST_UPDATE = "last_update"
        private const val CACHE_EXPIRY_MS = 5 * 60 * 1000 // 5 minutes
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveCouple(couple: Couple) {
        prefs.edit().apply {
            putString(KEY_CURRENT_COUPLE, gson.toJson(couple))
            putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            apply()
        }
    }

    fun getCouple(): Couple? {
        val json = prefs.getString(KEY_CURRENT_COUPLE, null) ?: return null
        return nullSafeOrNull { gson.fromJson(json, Couple::class.java) }
    }

    fun isCacheValid(): Boolean {
        val lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0)
        return System.currentTimeMillis() - lastUpdate < CACHE_EXPIRY_MS
    }

    fun clearCache() {
        prefs.edit().apply {
            remove(KEY_CURRENT_COUPLE)
            remove(KEY_LAST_UPDATE)
            apply()
        }
    }

    fun updateLastUpdateTime() {
        prefs.edit().putLong(KEY_LAST_UPDATE, System.currentTimeMillis()).apply()
    }

    fun hasValidCache(): Boolean {
        return getCouple() != null && isCacheValid()
    }
} 