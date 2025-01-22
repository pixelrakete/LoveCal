package com.pixelrakete.lovecal

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LoveCalApp : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Log.d(TAG, "Firebase initialized successfully")

        val availability = GoogleApiAvailability.getInstance()
        val result = availability.isGooglePlayServicesAvailable(this)
        if (result == com.google.android.gms.common.ConnectionResult.SUCCESS) {
            Log.d(TAG, "Google Play Services is available")
        } else {
            Log.e(TAG, "Google Play Services is not available: $result")
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setDefaultProcessName("${packageName}.background")
            .build()

    companion object {
        private const val TAG = "LoveCalApp"
    }
} 