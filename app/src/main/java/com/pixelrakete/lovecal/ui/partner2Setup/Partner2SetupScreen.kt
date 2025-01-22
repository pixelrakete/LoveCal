package com.pixelrakete.lovecal.ui.partner2Setup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelrakete.lovecal.ui.components.ColorPicker
import com.pixelrakete.lovecal.ui.components.InterestPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Partner2SetupScreen(
    onNavigateToHome: () -> Unit,
    viewModel: Partner2SetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateToHome()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partner Setup") }
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
                ErrorDialog(
                    message = errorMessage,
                    onDismiss = viewModel::clearError
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("Partner Name") },
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                ColorPicker(
                    selectedColor = uiState.color,
                    onColorSelected = { viewModel.updateColor(it) },
                    title = "Choose Partner Color"
                )
                Spacer(modifier = Modifier.height(16.dp))

                InterestPicker(
                    selectedInterests = uiState.interests,
                    onInterestsChanged = { viewModel.updateInterests(it) }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.completeSetup() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Complete Setup")
                }
            }
        }
    }
}

@Composable
private fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
} 