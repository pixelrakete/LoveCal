package com.pixelrakete.lovecal.data.services

import com.pixelrakete.lovecal.data.model.WeatherInfo
import com.pixelrakete.lovecal.data.model.WeatherType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherService @Inject constructor() {
    suspend fun getWeatherForLocation(location: String): WeatherInfo {
        // TODO: Implement actual weather API integration
        return WeatherInfo(
            temperature = 20.0,
            weatherType = WeatherType.SUNNY,
            location = location
        )
    }
} 