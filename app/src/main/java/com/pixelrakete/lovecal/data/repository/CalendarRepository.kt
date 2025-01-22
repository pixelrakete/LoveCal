package com.pixelrakete.lovecal.data.repository

import java.time.LocalDateTime
import android.util.Log
import android.content.Context
import android.accounts.AccountManager
import android.provider.CalendarContract

data class Calendar(
    val id: Long,
    val name: String
)

interface CalendarRepository {
    suspend fun createCalendar(name: String): Long
    suspend fun getCalendars(): List<Calendar>
    suspend fun addDateToCalendar(
        title: String,
        description: String,
        location: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        calendarId: Long
    ): Long
} 