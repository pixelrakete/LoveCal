package com.pixelrakete.lovecal.ui.calendar

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes

@Composable
fun CalendarSelectionScreen(
    onCalendarSelected: (Long) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: CalendarSelectionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Handle successful calendar selection
    LaunchedEffect(uiState.success, uiState.calendarId) {
        if (uiState.success && uiState.calendarId != -1L) {
            try {
                // Set loading to false before navigation
                viewModel.setLoading(false)
                onCalendarSelected(uiState.calendarId)
            } catch (e: Exception) {
                Log.e("CalendarSelection", "Error during calendar selection", e)
                viewModel.resetSuccess()
            }
        }
    }

    // Check if we need to navigate to login
    LaunchedEffect(uiState.shouldNavigateToLogin) {
        if (uiState.shouldNavigateToLogin) {
            // Set loading to false before navigation
            viewModel.setLoading(false)
            onNavigateToLogin()
            viewModel.resetNavigation()
        }
    }

    // Permission launcher setup
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d("CalendarSelection", "Permissions result: $permissions")
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            Log.d("CalendarSelection", "All permissions granted, checking Google Calendar auth")
            checkGoogleCalendarAuthAndLoad(context, viewModel)
        } else {
            Log.d("CalendarSelection", "Some permissions denied")
            viewModel.navigateToLogin()
        }
    }

    // Check calendar permissions and load calendars if we have them
    LaunchedEffect(Unit) {
        Log.d("CalendarSelection", "Initial permission check")
        val readPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        )
        val writePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR
        )
        val hasPermissions = readPermission == PackageManager.PERMISSION_GRANTED &&
                writePermission == PackageManager.PERMISSION_GRANTED
        
        Log.d("CalendarSelection", "Has permissions: $hasPermissions (read: ${readPermission == PackageManager.PERMISSION_GRANTED}, write: ${writePermission == PackageManager.PERMISSION_GRANTED})")
        
        if (!hasPermissions) {
            Log.d("CalendarSelection", "Requesting permissions")
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR
                )
            )
        } else {
            Log.d("CalendarSelection", "All permissions granted, checking Google Calendar auth")
            checkGoogleCalendarAuthAndLoad(context, viewModel)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Select Calendar",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Choose an existing calendar or create a new one for your date plans.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        if (uiState.error != null) {
            Text(
                text = uiState.error ?: "",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (!uiState.success) {  // Only show content when not in success state
            if (uiState.availableCalendars.isEmpty()) {
                Text(
                    text = if (uiState.isLoading) "Loading calendars..." else "No calendars found",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.availableCalendars) { calendar ->
                        ElevatedCard(
                            onClick = { 
                                if (!uiState.isLoading) {
                                    viewModel.selectCalendar(calendar.id)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = calendar.name,
                                    color = if (uiState.isLoading) 
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.createCalendar() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text(if (uiState.isLoading) "Loading..." else "Create New Calendar")
            }
        }
    }
}

private fun checkGoogleCalendarAuthAndLoad(
    context: android.content.Context,
    viewModel: CalendarSelectionViewModel
) {
    Log.d("CalendarSelection", "Checking Google Calendar authentication")
    val account = GoogleSignIn.getLastSignedInAccount(context)
    if (account == null || !account.grantedScopes.any { it.scopeUri == CalendarScopes.CALENDAR }) {
        Log.d("CalendarSelection", "No Google Calendar authentication, navigating to login")
        viewModel.navigateToLogin()
    } else {
        Log.d("CalendarSelection", "Google Calendar authentication present, loading calendars")
        viewModel.loadCalendars()
    }
} 