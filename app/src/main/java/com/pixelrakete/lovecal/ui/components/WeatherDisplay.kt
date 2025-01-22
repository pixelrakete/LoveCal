package com.pixelrakete.lovecal.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
        WeatherIcon(weatherType = weatherInfo.weatherType)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "${weatherInfo.temperature.roundToInt()}°C")
    }
}

@Composable
fun WeatherIcon(
    weatherType: WeatherType,
    modifier: Modifier = Modifier
) {
    val (icon, description) = when (weatherType) {
        WeatherType.SUNNY -> Icons.Default.WbSunny to "Sunny weather"
        WeatherType.CLOUDY -> Icons.Default.Cloud to "Cloudy weather"
        WeatherType.RAINY -> Icons.Default.WaterDrop to "Rainy weather"
        WeatherType.SNOWY -> Icons.Default.AcUnit to "Snowy weather"
        WeatherType.PARTLY_CLOUDY -> Icons.Default.CloudQueue to "Partly cloudy weather"
        WeatherType.WINDY -> Icons.Default.Air to "Windy weather"
        WeatherType.STORMY -> Icons.Default.Thunderstorm to "Stormy weather"
        WeatherType.UNKNOWN -> Icons.Default.QuestionMark to "Unknown weather"
    }

    Icon(
        imageVector = icon,
        contentDescription = description,
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
} 