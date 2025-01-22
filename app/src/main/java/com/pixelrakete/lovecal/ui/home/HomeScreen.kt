package com.pixelrakete.lovecal.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelrakete.lovecal.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDateDetails: (String) -> Unit,
    onNavigateToDateCreation: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDateWishes: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LoveCal") },
                actions = {
                    IconButton(onClick = onNavigateToDateWishes) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Date Wishes"
                        )
                    }
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
                onClick = onNavigateToDateCreation,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create date",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quote Card
            item {
                QuoteCard(
                    quote = uiState.quote,
                    isLoading = uiState.isLoading,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Countdown Card
            if (uiState.nextDate != null) {
                item {
                    CountdownCard(
                        nextDate = uiState.nextDate!!,
                        daysUntil = uiState.daysUntilNextDate,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Invitation Card
            if (uiState.invitationCode != null) {
                item {
                    InvitationCard(
                        code = uiState.invitationCode!!,
                        onShareClick = { /* TODO: Implement share functionality */ },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Budget Chart
            item {
                BudgetContent(
                    monthlyBudget = uiState.monthlyBudget,
                    remainingBudget = uiState.remainingBudget,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Upcoming Dates
            item {
                Text(
                    text = "Upcoming Dates",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(h