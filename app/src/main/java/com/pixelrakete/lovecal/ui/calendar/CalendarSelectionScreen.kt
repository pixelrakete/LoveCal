package com.pixelrakete.lovecal.ui.calendar

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.pixelrakete.lovecal.R
import com.pixelrakete.lovecal.ui.components.ErrorView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarSelectionScreen(
    viewModel: CalendarSelectionViewModel,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            viewModel.loadCalendars()
        } else {
            viewModel.setError("Calendar permissions are required to continue")
        }
    }

    // Check permissions on first launch
    LaunchedEffect(Unit) {
        val readPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
        val writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
        
        if (readPermission != PackageManager.PERMISSION_GRANTED || writePermission != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR
            ))
        } else {
            viewModel.loadCalendars()
        }
    }

    LaunchedEffect(uiState.selectedCalendarId) {
        if (uiState.selectedCalendarId != null && uiState.calendarPermissionGranted) {
            onNavigateToHome()
        }
    }

    if (uiState.showSharingConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::dismissSharingConfirmation,
            title = { Text("Share Calendar") },
            text = {
                Column {
                    Text("Do you want to share this calendar with your future partner?")
                    Text(
                        text = "They will be able to view and edit events in this calendar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = viewModel::confirmCalendarSelection) {
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissSharingConfirmation) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.select_calendar)) }
            )
        }
    ) { padding ->
        Box(modifier = modifier.padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.error?.let { error ->
                AlertDialog(
                    onDismissRequest = viewModel::clearError,
                    title = { Text("Error") },
                    text = { Text(error) },
                    confirmButton = {
                        TextButton(onClick = viewModel::clearError) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.isPartner2 && uiState.sharedCalendar != null) {
                    item {
                        val sharedCalendar = uiState.sharedCalendar
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { 
                                sharedCalendar?.let { calendar ->
                                    viewModel.initiateCalendarSelection(calendar)
                                }
                            }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Shared Couple Calendar",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Calendar shared by your partner",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else if (!uiState.isPartner2) {
                    items(uiState.calendars) { calendar ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { viewModel.initiateCalendarSelection(calendar) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = calendar.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (calendar.isReadOnly) {
                                    Text(
                                        text = "Read Only",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
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