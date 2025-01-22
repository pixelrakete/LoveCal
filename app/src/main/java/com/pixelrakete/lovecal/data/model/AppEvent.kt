package com.pixelrakete.lovecal.data.model

sealed class AppEvent {
    object DatePlanCreated : AppEvent()
} 