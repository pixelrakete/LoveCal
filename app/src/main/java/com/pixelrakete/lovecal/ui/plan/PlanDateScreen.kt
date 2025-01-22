package com.pixelrakete.lovecal.ui.plan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelrakete.lovecal.R
import com.pixelrakete.lovecal.ui.components.DatePickerDialog
import com.pixelrakete.lovecal.ui.components.LoadingScreen
import com.pixelrakete.lovecal.ui.components.OutlinedTextFieldWithLabel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDateScreen(
    onNavigateBack: () -> Unit,
    dateId: String? = null,
    viewModel: PlanDateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    LaunchedEffect(dateId) {
        if (dateId != null) {
            viewModel.loadDatePlan(dateId)
        }
    }

    if (uiState.isLoading) {
        LoadingScreen()
        return
    }

    if (uiState.success) {
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.plan_date)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextFieldWithLabel(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = stringResource(R.string.title),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextFieldWithLabel(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = stringResource(R.string.description),
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 3
            )

            OutlinedTextFieldWithLabel(
                value = uiState.location,
                onValueChange = { viewModel.updateLocation(it) },
                label = stringResource(R.string.location),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.startDateTime.format(dateFormatter),
                    style = MaterialTheme.typography.bodyLarge
                )
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select date")
                }
            }

            OutlinedTextFieldWithLabel(
                value = uiState.budget,
                onValueChange = { viewModel.updateBudget(it) },
                label = stringResource(R.string.budget),
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Number
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.surprise_date),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = uiState.isSurprise,
                    onCheckedChange = { viewModel.updateIsSurprise(it) }
                )
            }

            Button(
                onClick = { viewModel.getRandomWish() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.get_random_wish))
            }

            Button(
                onClick = { viewModel.saveDatePlan() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDateTimeSelected = { dateTime ->
                    viewModel.updateStartDateTime(dateTime)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }

        if (uiState.error != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text(stringResource(R.string.error)) },
                text = { Text(uiState.error!!) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            )
        }
    }
} 