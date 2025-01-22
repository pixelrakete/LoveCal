package com.pixelrakete.lovecal.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun BudgetContent(
    monthlyBudget: Double,
    remainingBudget: Double,
    spentBudget: Double,
    isBudgetExpanded: Boolean,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3C3C3C)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Chart
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = size.width * 0.15f
                        val radius = (size.width - strokeWidth) / 2
                        
                        // Background circle
                        drawCircle(
                            color = Color(0xFF666666),
                            radius = radius,
                            style = Stroke(strokeWidth)
                        )
                        
                        // Progress arc
                        val progress = if (monthlyBudget <= 0) 0f 
                            else (remainingBudget / monthlyBudget).toFloat().coerceIn(0f, 1f)
                        val sweepAngle = 360f * progress
                        drawArc(
                            color = Color(0xFFFF4081),
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    
                    // Center text showing percentage
                    val percentage = if (monthlyBudget <= 0) 0
                        else ((remainingBudget / monthlyBudget) * 100).toInt()
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Text(
                        text = "Monthly Budget",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "€${String.format("%.2f", remainingBudget)} remaining of €${String.format("%.2f", monthlyBudget)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Icon(
                    imageVector = if (isBudgetExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isBudgetExpanded) "Show less" else "Show more",
                    tint = Color.White
                )
            }

            if (isBudgetExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    BudgetRow("Total Budget", monthlyBudget)
                    BudgetRow("Spent", spentBudget)
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                    )
                    BudgetRow("Remaining", remainingBudget, isHighlighted = true)
                }
            }
        }
    }
}

@Composable
private fun BudgetRow(
    label: String,
    amount: Double,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isHighlighted) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = if (isHighlighted) 1f else 0.7f)
        )
        Text(
            text = "€${String.format("%.2f", amount)}",
            style = if (isHighlighted) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = if (isHighlighted) 1f else 0.7f)
        )
    }
} 