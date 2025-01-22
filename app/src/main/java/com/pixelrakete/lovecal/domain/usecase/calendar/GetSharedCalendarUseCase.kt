package com.pixelrakete.lovecal.domain.usecase.calendar

import com.pixelrakete.lovecal.data.repository.CalendarListEntry
import com.pixelrakete.lovecal.data.repository.CalendarRepository
import com.pixelrakete.lovecal.data.manager.UserManager
import javax.inject.Inject

class GetSharedCalendarUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val userManager: UserManager
) {
    suspend operator fun invoke(): CalendarListEntry? {
        val coupleId = userManager.getCurrentCoupleId() ?: return null
        return calendarRepository.getSharedCalendar(coupleId)
    }
} 