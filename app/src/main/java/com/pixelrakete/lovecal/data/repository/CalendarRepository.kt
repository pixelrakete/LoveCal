package com.pixelrakete.lovecal.data.repository

import com.pixelrakete.lovecal.data.model.Calendar
import com.pixelrakete.lovecal.data.model.DatePlan
import com.google.api.services.calendar.model.Event

interface CalendarRepository {
    suspend fun getCalendars(): List<CalendarListEntry>
    suspend fun addCalendar(calendar: CalendarListEntry)
    suspend fun getSharedCalendar(coupleId: String): CalendarListEntry?
} 