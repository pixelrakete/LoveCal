package com.pixelrakete.lovecal.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestPicker(
    selectedInterests: List<String>,
    onInterestsChanged: (List<String>) -> Unit
) {
    var newInterest by remember { mutableStateOf("") }

    Column {
        Text(
            text = "Your Interests",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = newInterest,
            onValueChange = { newInterest = it },
            label = { Text("Add Interest") }
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (newInterest.isNotBlank() && !selectedInterests.contains(newInterest)) {
                    onInterestsChanged(selectedInterests + newInterest)
                    newInterest = ""
                }
            },
            enabled = newInterest.isNotBlank()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Interest")
            Text("Add Interest")
        }
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            selectedInterests.forEach { interest ->
                ElevatedFilterChip(
                    selected = true,
                    onClick = {
                        onInterestsChanged(selectedInterests - interest)
                    },
                    label = { Text(interest) },
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                )
            }
        }
    }
} 