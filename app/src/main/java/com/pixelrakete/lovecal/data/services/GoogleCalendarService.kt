package com.pixelrakete.lovecal.data.services

import android.content.Context
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.api.client.http.HttpRequestInitializer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleCalendarService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val APPLICATION_NAME = "LoveCal"
    }

    fun getCalendarService(): Calendar {
        val accessToken = getAccessToken()
        val credentials = GoogleCredentials.create(AccessToken(accessToken, null))
        
        return Calendar.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credentials.createScoped(listOf("https://www.googleapis.com/auth/calendar")) as HttpRequestInitializer
        )
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    private fun getAccessToken(): String {
        val account = GoogleSignIn.getLastSignedInAccount(context)
            ?: throw IllegalStateException("User not signed in")
        return account.idToken ?: throw IllegalStateException("No ID token available")
    }
} 