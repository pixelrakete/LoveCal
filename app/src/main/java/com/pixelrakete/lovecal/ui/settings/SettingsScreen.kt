package com.pixelrakete.lovecal.ui.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelrakete.lovecal.ui.components.ColorPickerDialog
import com.pixelrakete.lovecal.ui.components.InterestChips
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    isInitialSetup: Boolean = false,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showColorPicker1 by remember { mutableStateOf(false) }
    var showColorPicker2 by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(isInitialSetup) {
        if (isInitialSetup) {
            viewModel.setInitialSetup()
        }
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isInitialSetup) "Setup Profile" else "Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Setting up your profile...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Partner 1 Settings
                item {
                    Text(
                        text = if (isInitialSetup) "Your Details" else "Your Settings",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                item {
                    OutlinedTextField(
                        value = uiState.partner1Name,
                        onValueChange = { viewModel.updatePartner1Name(it) },
                        label = { Text("Your Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Your Color")
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(android.graphics.Color.parseColor(uiState.partner1Color)))
                                .clickable { showColorPicker1 = true }
                        )
                    }
                }

                item {
                    InterestChips(
                        interests = uiState.partner1Interests,
                        onInterestsChange = { viewModel.updatePartner1Interests(it) }
                    )
                }

                // Partner 2 Settings
                item {
                    Text(
                        text = "Partner Settings",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                item {
                    OutlinedTextField(
                        value = uiState.partner2Name,
                        onValueChange = { viewModel.updatePartner2Name(it) },
                        label = { Text("Partner Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Partner Color")
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(android.graphics.Color.parseColor(uiState.partner2Color)))
                                .clickable { showColorPicker2 = true }
                        )
                    }
                }

                item {
                    InterestChips(
                        interests = uiState.partner2Interests,
                        onInterestsChange = { viewModel.updatePartner2Interests(it) }
                    )
                }

                // General Settings
                item {
                    Text(
                        text = "General",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                item {
                    OutlinedTextField(
                        value = uiState.city,
                        onValueChange = { viewModel.updateCity(it) },
                        label = { Text("City") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = uiState.monthlyBudget.toString(),
                        onValueChange = { 
                            it.toDoubleOrNull()?.let { budget ->
                                viewModel.updateMonthlyBudget(budget)
                            }
                        },
                        label = { Text("Monthly Budget") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Button(
                        onClick = { 
                            if (isInitialSetup) {
                                viewModel.createCouple()
                            } else {
                                viewModel.saveSettings()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isInitialSetup) "Create Profile" else "Save Settings")
                    }
                }
            }
        }
    }

    if (showColorPicker1) {
        ColorPickerDialog(
            onDismissRequest = { showColorPicker1 = false },
            onColorSelected = { color ->
                viewModel.updatePartner1Color(String.format("#%06X", 0xFFFFFF and color.toArgb()))
                showColorPicker1 = false
            }
        )
    }

    if (showColorPicker2) {
        ColorPickerDialog(
            onDismissRequest = { showColorPicker2 = false },
            onColorSelected = { color ->
                viewModel.updatePartner2Color(String.format("#%06X", 0xFFFFFF and color.toArgb()))
                showColorPicker2 = false
            }
        )
    }

    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(uiState.error!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
}