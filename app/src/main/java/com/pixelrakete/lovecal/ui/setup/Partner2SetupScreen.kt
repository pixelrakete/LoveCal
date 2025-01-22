package com.pixelrakete.lovecal.ui.setup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Partner2SetupScreen(
    invitationCode: String,
    onNavigateToHome: () -> Unit,
    viewModel: Partner2SetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("#2196F3") }
    var interests by remember { mutableStateOf("") }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onNavigateToHome()
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
            text = "Complete Your Profile",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Your Color (Hex)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = interests,
            onValueChange = { interests = it },
            label = { Text("Your Interests (comma separated)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                viewModel.completeSetup(
                    invitationCode = invitationCode,
                    name = name,
                    color = color,
                    interests = interests.split(",").map { it.trim() }.filter { it.isNotBlank() }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && color.isNotBlank() && !uiState.isLoading
        ) {
            Text("Complete Setup")
        }

        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }
    }
} 