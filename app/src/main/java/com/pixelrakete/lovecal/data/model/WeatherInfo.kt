package com.pixelrakete.lovecal.data.model

import com.pixelrakete.lovecal.data.model.validation.ModelValidatable
import com.pixelrakete.lovecal.data.model.validation.ModelValidationResult

data class WeatherInfo(
    val temperature: Double,
    val weatherType: WeatherType,
    val location: String
) : ModelValidatable {
    companion object {
        const val FIELD_TEMPERATURE = "temperature"
        const val FIELD_WEATHER_TYPE = "weatherType"
        const val FIELD_LOCATION = "location"

        private const val MIN_TEMP = -50.0
        private const val MAX_TEMP = 60.0
        private const val MAX_LOCATION_LENGTH = 100
    }

    override fun validate(): ModelValidationResult {
        val errors = mutableMapOf<String, String>()

        // Validate temperature
        when {
            temperature.isNaN() || temperature.isInfinite() -> errors[FIELD_TEMPERATURE] = "Temperature must be a valid number"
            temperature < MIN_TEMP -> errors[FIELD_TEMPERATURE] = "Temperature cannot be below $MIN_TEMP°C"
            temperature > MAX_TEMP -> errors[FIELD_TEMPERATURE] = "Temperature cannot exceed $MAX_TEMP°C"
        }

        // Validate location
        when {
            location.isBlank() -> errors[FIELD_LOCATION] = "Location is required"
            location.length > MAX_LOCATION_LENGTH -> errors[FIELD_LOCATION] = "Location cannot exceed $MAX_LOCATION_LENGTH characters"
        }

        return if (errors.isEmpty()) ModelValidationResult.valid() else ModelValidationResult.invalid(errors)
    }
} 