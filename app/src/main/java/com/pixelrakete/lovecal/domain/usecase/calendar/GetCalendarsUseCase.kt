package com.pixelrakete.lovecal.domain.usecase.calendar

import com.pixelrakete.lovecal.data.repository.CalendarListEntry
import com.pixelrakete.lovecal.data.repository.CalendarRepository
import javax.inject.Inject

class GetCalendarsUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(): List<CalendarListEntry> {
        return calendarRepository.getCalendars()
    }
} 