package com.pixelrakete.lovecal.ui.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.BackHandler
import com.pixelrakete.lovecal.R
import com.pixelrakete.lovecal.data.model.DateSuggestion
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.IconButton
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDateScreen(
    viewModel: PlanDateViewModel = hiltViewModel(),
    dateId: String? = null,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateTime by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(dateId) {
        viewModel.loadDatePlan(dateId)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.clearSuccess()
            onNavigateBack()
        }
    }

    BackHandler {
        viewModel.resetState()
        onNavigateBack()
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(millis),
                            ZoneId.systemDefault()
                        )
                        showDatePicker = false
                        showTimePicker = true
                        selectedDateTime = selectedDate
                    }
                }) {
                    Text("Next")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                title = { Text("Select Date") }
            )
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedDateTime.hour,
            initialMinute = selectedDateTime.minute
        )
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                Button(onClick = {
                    val dateStr = String.format(
                        "%02d.%02d.%04d %02d:%02d",
                        selectedDateTime.dayOfMonth,
                        selectedDateTime.monthValue,
                        selectedDateTime.year,
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    viewModel.updateDateTime(dateStr)
                    showTimePicker = false
                }) {
                    Text("Confirm")
                }
            }
        ) {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (dateId == null) "Plan a Date" else "Edit Date Plan") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    enabled = !uiState.isLoading
                )

                OutlinedTextField(
                    value = uiState.location,
                    onValueChange = { viewModel.updateLocation(it) },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Date: ${uiState.dateTimeStr}",
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { showDatePicker = true },
                        enabled = !uiState.isLoading
                    ) {
                        Text("Select Date")
                    }
                }

                Text("Budget: €${uiState.budget.toInt()}")
                Slider(
                    value = uiState.budget.toFloat(),
                    onValueChange = { viewModel.updateBudget(it.toDouble()) },
                    valueRange = 0f..500f,
                    steps = 50,
                    enabled = !uiState.isLoading
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Surprise your partner?")
                    Switch(
                        checked = uiState.isSurprise,
                        onCheckedChange = { viewModel.updateIsSurprise(it) },
                        enabled = !uiState.isLoading
                    )
                }

                Button(
                    onClick = { viewModel.generateAiSuggestion() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Text("Get AI Suggestions")
                }

                Button(
                    onClick = { viewModel.saveDatePlan() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Text("Save Date")
                }

                if (uiState.suggestions.isNotEmpty()) {
                    Text(
                        text = "Suggestions",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.suggestions.forEach { suggestion ->
                            DateSuggestionCard(
                                suggestion = suggestion,
                                onClick = { viewModel.applySuggestion(suggestion) },
                                enabled = !uiState.isLoading
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (uiState.error != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    title = { Text("Error") },
                    text = { Text(text = uiState.error ?: "") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DateSuggestionCard(
    suggestion: DateSuggestion,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = suggestion.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = suggestion.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = "€${suggestion.budget.toInt()}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestPills(
    interests: List<String>,
    onRemove: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        interests.forEach { interest ->
            AssistChip(
                onClick = { },
                label = { Text(interest) },
                leadingIcon = {
                    IconButton(
                        onClick = { onRemove(interest) },
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove $interest",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismissRequest
                    ) {
                        Text("Cancel")
                    }
                    confirmButton()
                }
            }
        }
    }
} 