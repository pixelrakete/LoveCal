package com.pixelrakete.lovecal.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BudgetContent(
    monthlyBudget: Double,
    remainingBudget: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Monthly Budget",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "€${String.format("%.2f", remainingBudget)} remaining of €${String.format("%.2f", monthlyBudget)}",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                CircularProgressIndicator(
                    progress = if (monthlyBudget <= 0) 0f else (remainingBudget / monthlyBudget).toFloat().coerceIn(0f, 1f),
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
} 