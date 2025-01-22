package com.pixelrakete.lovecal.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InterestChips(
    interests: List<String>,
    onInterestsChange: (List<String>) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Interests")
            IconButton(onClick = { showAddDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Interest"
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            interests.forEach { interest ->
                AssistChip(
                    onClick = {
                        val newInterests = interests.toMutableList()
                        newInterests.remove(interest)
                        onInterestsChange(newInterests)
                    },
                    label = { Text(interest) }
                )
            }
        }
    }

    if (showAddDialog) {
        var newInterest by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Interest") },
            text = {
                OutlinedTextField(
                    value = newInterest,
                    onValueChange = { newInterest = it },
                    label = { Text("Interest") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newInterest.isNotBlank()) {
                            val newInterests = interests.toMutableList()
                            newInterests.add(newInterest)
                            onInterestsChange(newInterests)
                        }
                        showAddDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 