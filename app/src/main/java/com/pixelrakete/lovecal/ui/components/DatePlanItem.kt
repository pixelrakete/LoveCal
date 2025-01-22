package com.pixelrakete.lovecal.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pixelrakete.lovecal.data.model.DatePlan
import java.time.format.DateTimeFormatter

@Composable
fun DatePlanItem(
    datePlan: DatePlan,
    currentUserId: String,
    partnerColor: Color,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (datePlan.isSurprise && datePlan.plannerId != currentUserId) "Surprise Date" else datePlan.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = datePlan.startDateTime.format(dateFormatter),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = datePlan.startDateTime.format(timeFormatter),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit date",
                            tint = partnerColor
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete date",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (!datePlan.isSurprise || datePlan.plannerId == currentUserId) {
                Text(
                    text = datePlan.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Location: ${datePlan.location}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Budget: €${String.format("%.2f", datePlan.budget)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
} 