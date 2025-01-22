package com.pixelrakete.lovecal

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LoveCalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            Log.d("LoveCalApp", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("LoveCalApp", "Error initializing Firebase", e)
        }
    }
} 