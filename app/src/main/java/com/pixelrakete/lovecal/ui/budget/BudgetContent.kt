package com.pixelrakete.lovecal.ui.budget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.pixelrakete.lovecal.ui.components.DonutChart

@Composable
fun BudgetContent(
    monthlyBudget: Double,
    plannedBudget: Double,
    spentBudget: Double,
    remainingBudget: Double,
    isBudgetExpanded: Boolean,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Collapsed view
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandClick() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Donut chart
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = size.width * 0.2f
                    val radius = (size.width - strokeWidth) / 2
                    
                    // Background circle
                    drawCircle(
                        color = Color(0xFFE0E0E0),
                        radius = radius,
                        style = Stroke(strokeWidth)
                    )
                    
                    // Progress arc
                    val progress = (remainingBudget / monthlyBudget).toFloat().coerceIn(0f, 1f)
                    val sweepAngle = 360f * progress
                    drawArc(
                        color = Color(0xFFFF4081),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(strokeWidth)
                    )
                }
            }
            
            // Budget text
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = "Monthly Budget",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "€${remainingBudget.toInt()} of €${monthlyBudget.toInt()}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Expand icon
            Icon(
                imageVector = if (isBudgetExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isBudgetExpanded) "Show less" else "Show more"
            )
        }

        // Expanded view
        if (isBudgetExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                BudgetRow("Total Budget", monthlyBudget, monthlyBudget)
                BudgetRow("Planned", plannedBudget, monthlyBudget)
                BudgetRow("Spent", spentBudget, monthlyBudget)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                BudgetRow("Remaining", remainingBudget, monthlyBudget, true)
            }
        }
    }
}

@Composable
private fun BudgetRow(
    label: String,
    amount: Double,
    total: Double,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isHighlighted) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "€${amount.toInt()}",
            style = if (isHighlighted) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
    }
} 