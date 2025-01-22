package com.pixelrakete.lovecal.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pixelrakete.lovecal.R
import com.pixelrakete.lovecal.data.model.DatePlan
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePlanItem(
    datePlan: DatePlan,
    currentUserId: String,
    partnerColor: Color,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    val isPlanner = datePlan.plannerId == currentUserId

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = {
            if (!datePlan.isSurprise || isPlanner) {
                expanded = !expanded
            }
        }
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Color bar indicating the planner's color
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(partnerColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (datePlan.isSurprise && !isPlanner) {
                            stringResource(R.string.surprise_date)
                        } else {
                            datePlan.title
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (!datePlan.isSurprise || isPlanner) {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (expanded) "Show less" else "Show more"
                            )
                        }
                    }
                }

                Text(
                    text = datePlan.startDateTime.format(dateFormatter),
                    style = MaterialTheme.typography.bodyMedium
                )

                AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        if (datePlan.description.isNotBlank()) {
                            Text(
                                text = datePlan.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (datePlan.location.isNotBlank()) {
                            Text(
                                text = datePlan.location,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        if (datePlan.budget > 0) {
                            Text(
                                text = "€${datePlan.budget.toInt()}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        if (isPlanner) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = onEditClick) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit date"
                                    )
                                }
                                IconButton(onClick = onDeleteClick) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete date"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 