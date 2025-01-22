package com.pixelrakete.lovecal.util

import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.EventDateTime
import java.time.LocalDateTime
import java.time.ZoneId

fun LocalDateTime.toEventDateTime(): EventDateTime {
    val date = this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    return EventDateTime().setDateTime(DateTime(date))
} 