package com.pixelrakete.lovecal.ui.setup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CoupleDecisionScreen(
    onCreateNewCouple: () -> Unit,
    onJoinCouple: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to LoveCal!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Would you like to create a new couple or join an existing one?",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCreateNewCouple,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create New Couple")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onJoinCouple,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Join Existing Couple")
        }
    }
} 