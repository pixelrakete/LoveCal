package com.pixelrakete.lovecal.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pixelrakete.lovecal.data.model.WeatherInfo
import com.pixelrakete.lovecal.data.model.WeatherType
import kotlin.math.roundToInt

@Composable
fun WeatherDisplay(weatherInfo: WeatherInfo) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = when (weatherInfo.weatherType) {
                WeatherType.SUNNY -> Icons.Default.WbSunny
                WeatherType.CLOUDY -> Icons.Default.Cloud
                WeatherType.RAINY -> Icons.Default.WaterDrop
                WeatherType.SNOWY -> Icons.Default.AcUnit
                WeatherType.STORMY -> Icons.Default.Bolt
                WeatherType.UNKNOWN -> Icons.Default.QuestionMark
            },
            contentDescription = when (weatherInfo.weatherType) {
                WeatherType.SUNNY -> "Sunny"
                WeatherType.CLOUDY -> "Cloudy"
                WeatherType.RAINY -> "Rainy"
                WeatherType.SNOWY -> "Snowy"
                WeatherType.STORMY -> "Stormy"
                WeatherType.UNKNOWN -> "Unknown"
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "${weatherInfo.temperature.roundToInt()}°C")
    }
} 