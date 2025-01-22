package com.pixelrakete.lovecal.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pixelrakete.lovecal.data.model.DatePlan
import java.time.format.DateTimeFormatter

@Composable
fun CountdownCard(
    nextDate: DatePlan,
    daysUntil: Int,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Next Date",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = if (nextDate.isSurprise) "Surprise Date" else nextDate.title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = nextDate.startDateTime.format(dateFormatter),
                style = MaterialTheme.typography.bodyLarge
            )
            
            Text(
                text = when {
                    daysUntil == 0 -> "Today!"
                    daysUntil == 1 -> "Tomorrow!"
                    else -> "$daysUntil days to go"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
} 