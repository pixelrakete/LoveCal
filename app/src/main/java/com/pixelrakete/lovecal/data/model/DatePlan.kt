package com.pixelrakete.lovecal.data.model

import java.time.LocalDateTime

data class DatePlan(
    val id: String? = null,
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val startDateTime: LocalDateTime = LocalDateTime.now(),
    val budget: Double = 0.0,
    val isSurprise: Boolean = false,
    val coupleId: String = "",
    val plannerId: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now()
) 