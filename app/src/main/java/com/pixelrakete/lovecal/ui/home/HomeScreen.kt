package com.pixelrakete.lovecal.ui.home

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pixelrakete.lovecal.R
import com.pixelrakete.lovecal.data.model.DatePlan
import com.pixelrakete.lovecal.ui.components.InvitationCard
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter

@Composable
private fun DateCountdown(
    datePlan: DatePlan,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Next Date Coming Up!",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = datePlan.title ?: "Untitled Date",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            datePlan.dateTimeStr?.let { dateStr ->
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNavigateToCreateDatePlan: () -> Unit,
    onNavigateToEditDatePlan: (String) -> Unit,
    onDeleteDatePlan: (String) -> Unit,
    onCompleteDatePlan: (String, Boolean) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Find next upcoming date within a week
    val nextDate = remember(uiState.datePlans) {
        uiState.datePlans
            .filter { !it.completed }
            .firstOrNull { datePlan ->
                datePlan.dateTimeStr?.let { dateStr ->
                    try {
                        val formatter = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm")
                        val dateTime = LocalDateTime.parse(dateStr, formatter)
                        val now = LocalDateTime.now()
                        val daysUntil = ChronoUnit.DAYS.between(now, dateTime)
                        
                        daysUntil in 0..7
                    } catch (e: Exception) {
                        Log.e("HomeScreen", "Error parsing date: $dateStr", e)
                        false
                    }
                } ?: false
            }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.logo_lovecal),
                            contentDescription = "LoveCal Logo",
                            modifier = Modifier.size(40.dp),
                            tint = Color.Unspecified
                        )
                        Text(
                            text = "LoveCal",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateDatePlan,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create new date plan"
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Quote tile
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = uiState.quote ?: "Love is in the air",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "- ${uiState.quoteAuthor ?: "Unknown"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Countdown tile for upcoming date
            item {
                nextDate?.let { datePlan ->
                    DateCountdown(
                        datePlan = datePlan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
            
            // Invitation Card
            if (uiState.invitationCode != null && uiState.partner2Id.isNullOrEmpty()) {
                item {
                    InvitationCard(
                        code = uiState.invitationCode,
                        onShareClick = {},
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Budget tile
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    BudgetDisplay(
                        monthlyBudget = uiState.monthlyBudget,
                        remainingBudget = uiState.remainingBudget
                    )
                }
            }
            
            // Date plans section
            item {
                Text(
                    text = "Upcoming Dates",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            // Date plan items
            items(uiState.datePlans) { datePlan ->
                DatePlanItem(
                    datePlan = datePlan,
                    plannerColor = Color(android.graphics.Color.parseColor(uiState.plannerColor)),
                    isPlanner = uiState.isPlanner,
                    onEditClick = { onNavigateToEditDatePlan(datePlan.id) },
                    onDeleteClick = { onDeleteDatePlan(datePlan.id) },
                    onCompletedChange = { completed -> onCompleteDatePlan(datePlan.id, completed) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
} 