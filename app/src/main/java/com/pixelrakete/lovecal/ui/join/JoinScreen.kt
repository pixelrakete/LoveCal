package com.pixelrakete.lovecal.ui.join

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinScreen(
    viewModel: JoinViewModel,
    onNavigateToPartner2Setup: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Join Couple",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.code,
                    onValueChange = { viewModel.updateCode(it.uppercase()) },
                    label = { Text("Enter Couple Code") },
                    isError = uiState.error != null,
                    supportingText = uiState.error?.let { { Text(it) } },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onNavigateBack() },
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading
            ) {
                Text("Back")
            }

            Button(
                onClick = { viewModel.joinCouple { onNavigateToPartner2Setup(uiState.code) } },
                modifier = Modifier.weight(1f),
                enabled = !uiState.isLoading && uiState.code.length == 6
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Join")
                }
            }
        }
    }
} 