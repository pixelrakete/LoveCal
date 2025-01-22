package com.pixelrakete.lovecal.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelrakete.lovecal.ui.components.ColorPicker
import com.pixelrakete.lovecal.ui.components.InterestPicker
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            if (uiState.shouldNavigateToLogin) {
                onNavigateToLogin()
            } else {
                onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.error?.let { errorMessage ->
                AlertDialog(
                    onDismissRequest = viewModel::clearError,
                    title = { Text("Error") },
                    text = { Text(errorMessage) },
                    confirmButton = {
                        TextButton(onClick = viewModel::clearError) {
                            Text("OK")
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Partner 1 Section
                Text(
                    text = "Partner 1 Settings",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = uiState.partner1Name,
                    onValueChange = { newName ->
                        viewModel.updateSettings(
                            partner1Name = newName,
                            partner1Color = uiState.partner1Color,
                            partner1Interests = uiState.partner1Interests,
                            partner2Name = uiState.partner2Name,
                            partner2Color = uiState.partner2Color,
                            partner2Interests = uiState.partner2Interests,
                            monthlyBudget = uiState.monthlyBudget,
                            dateFrequencyWeeks = uiState.dateFrequencyWeeks,
                            city = uiState.city
                        )
                    },
                    label = { Text("Partner 1 Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                ColorPicker(
                    selectedColor = uiState.partner1Color,
                    onColorSelected = { newColor ->
                        viewModel.updateSettings(
                            partner1Name = uiState.partner1Name,
                            partner1Color = newColor,
                            partner1Interests = uiState.partner1Interests,
                            partner2Name = uiState.partner2Name,
                            partner2Color = uiState.partner2Color,
                            partner2Interests = uiState.partner2Interests,
                            monthlyBudget = uiState.monthlyBudget,
                            dateFrequencyWeeks = uiState.dateFrequencyWeeks,
                            city = uiState.city
                        )
                    },
                    title = "Partner 1 Color"
                )

                Text(
                    text = "Partner 1 Interests",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )

                InterestPicker(
                    selectedInterests = uiState.partner1Interests,
                    onInterestsChanged = { newInterests ->
                        viewModel.updateSettings(
                            partner1Name = uiState.partner1Name,
                            partner1Color = uiState.partner1Color,
                            partner1Interests = newInterests,
                            partner2Name = uiState.partner2Name,
                            partner2Color = uiState.partner2Color,
                            partner2Interests = uiState.partner2Interests,
                            monthlyBudget = uiState.monthlyBudget,
                            dateFrequencyWeeks = uiState.dateFrequencyWeeks,
                            city = uiState.city
                        )
                    }
                )

                // Partner 2 Section
                Text(
                    text = "Partner 2 Settings",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                OutlinedTextField(
                    value = uiState.partner2Name,
                    onValueChange = { newName ->
                        viewModel.updateSettings(
                            partner1Name = uiState.partner1Name,
                            partner1Color = uiState.partner1Color,
                            partner1Interests = uiState.partner1Interests,
                            partner2Name = newName,
                            partner2Color = uiState.partner2Color,
                            partner2Interests = uiState.partner2Interests,
                            monthlyBudget = uiState.monthlyBudget,
                            dateFrequencyWeeks = uiState.dateFrequencyWeeks,
                            city = uiState.city
                        )
                    },
                    label = { Text("Partner 2 Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                ColorPicker(
                    selectedColor = uiState.partner2Color,
                    onColorSelected = { newColor ->
                        viewModel.updateSettings(
                            partner1Name = uiState.partner1Name,
                            partner1Color = uiState.partner1Color,
                            partner1Interests = uiState.partner1Interests,
                            partner2Name = uiState.partner2Name,
                            partner2Color = newColor,
                            partner2Interests = uiState.partner2Interests,
                            monthlyBudget = uiState.monthlyBudget,
                            dateFrequencyWeeks = uiState.dateFrequencyWeeks,
                            city = uiState.city
                        )
                    },
                    title = "Partner 2 Color"
                )

                Text(
                    text = "Partner 2 Interests",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )

                InterestPicker(
                    selectedInterests = uiState.partner2Interests,
                    onInterestsChanged = { newInterests ->
                        viewModel.updateSettings(
                            partner1Name = uiState.partner1Name,
                            partner1Color = uiState.partner1Color,
                            partner1Interests = uiState.partner1Interests,
                            partner2Name = uiState.partner2Name,
                            partner2Color = uiState.partner2Color,
                            partner2Interests = newInterests,
                            monthlyBudget = uiState.monthlyBudget,
                            dateFrequencyWeeks = uiState.dateFrequencyWeeks,
                            city = uiState.city
                        )
                    }
                )

                // Couple Settings Section
                Text(
                    text = "Couple Settings",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                OutlinedTextField(
                    value = uiState.city,
                    onValueChange = { newCity ->
                        viewModel.updateSettings(
                            partner1Name = uiState.partner1Name,
                            partner1Color = uiState.partner1Color,
                            partner1Interests = uiState.partner1Interests,
                            partner2Name = uiState.partner2Name,
                            partner2Color = uiState.partner2Color,
                            partner2Interests = uiState.partner2Interests,
                            monthlyBudget = uiState.monthlyBudget,
                            dateFrequencyWeeks = uiState.dateFrequencyWeeks,
                            city = newCity
                        )
                    },
                    label = { Text("City") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Monthly Budget Slider
                Text(
                    text = "Monthly Budget: €${uiState.monthlyBudget.roundToInt()}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Slider(
                    value = uiState.monthlyBudget.toFloat(),
                    onValueChange = { newValue ->
                        viewModel.updateSettings(
                            partner1Name = uiState.partner1Name,
                            partner1Color = uiState.partner1Color,
                            partner1Interests = uiState.partner1Interests,
                            partner2Name = uiState.partner2Name,
                            partner2Color = uiState.partner2Color,
                            partner2Interests = uiState.partner2Interests,
                            monthlyBudget = newValue.roundToInt().toDouble(),
                            dateFrequencyWeeks = uiState.dateFrequencyWeeks,
                            city = uiState.city
                        )
                    },
                    valueRange = 100f..1000f,
                    steps = 18, // (1000-100)/50 = 18 steps
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Frequency Selection
                Text(
                    text = "Date Frequency: ${when(uiState.dateFrequencyWeeks) {
                        1 -> "Once a week"
                        2 -> "Every 2 weeks"
                        4 -> "Once a month"
                        else -> "Every ${uiState.dateFrequencyWeeks} weeks"
                    }}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.dateFrequencyWeeks == 1,
                        onClick = {
                            viewModel.updateSettings(
                                partner1Name = uiState.partner1Name,
                                partner1Color = uiState.partner1Color,
                                partner1Interests = uiState.partner1Interests,
                                partner2Name = uiState.partner2Name,
                                partner2Color = uiState.partner2Color,
                                partner2Interests = uiState.partner2Interests,
                                monthlyBudget = uiState.monthlyBudget,
                                dateFrequencyWeeks = 1,
                                city = uiState.city
                            )
                        },
                        label = { Text("Weekly") }
                    )

                    FilterChip(
                        selected = uiState.dateFrequencyWeeks == 2,
                        onClick = {
                            viewModel.updateSettings(
                                partner1Name = uiState.partner1Name,
                                partner1Color = uiState.partner1Color,
                                partner1Interests = uiState.partner1Interests,
                                partner2Name = uiState.partner2Name,
                                partner2Color = uiState.partner2Color,
                                partner2Interests = uiState.partner2Interests,
                                monthlyBudget = uiState.monthlyBudget,
                                dateFrequencyWeeks = 2,
                                city = uiState.city
                            )
                        },
                        label = { Text("Bi-weekly") }
                    )

                    FilterChip(
                        selected = uiState.dateFrequencyWeeks == 4,
                        onClick = {
                            viewModel.updateSettings(
                                partner1Name = uiState.partner1Name,
                                partner1Color = uiState.partner1Color,
                                partner1Interests = uiState.partner1Interests,
                                partner2Name = uiState.partner2Name,
                                partner2Color = uiState.partner2Color,
                                partner2Interests = uiState.partner2Interests,
                                monthlyBudget = uiState.monthlyBudget,
                                dateFrequencyWeeks = 4,
                                city = uiState.city
                            )
                        },
                        label = { Text("Monthly") }
                    )
                }

                // Save Button
                if (uiState.hasUnsavedChanges) {
                    Button(
                        onClick = { viewModel.saveSettings() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text("Save Changes")
                    }
                }

                // Account Actions Section
                Text(
                    text = "Account Actions",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )

                Button(
                    onClick = { viewModel.signOut() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Out")
                }

                Button(
                    onClick = { viewModel.deleteUserData() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete User Data")
                }
            }
        }
    }
}