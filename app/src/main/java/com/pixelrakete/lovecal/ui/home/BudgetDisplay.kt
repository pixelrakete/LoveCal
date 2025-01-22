package com.pixelrakete.lovecal.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
private fun DonutChart(
    progress: Float,
    backgroundColor: Color,
    progressColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        val strokeWidth = size.width * 0.2f
        val diameter = min(size.width, size.height) - strokeWidth
        val topLeft = Offset(
            x = (size.width - diameter) / 2,
            y = (size.height - diameter) / 2
        )
        val canvasSize = Size(diameter, diameter)
        
        // Background circle
        drawArc(
            color = backgroundColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = canvasSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Progress arc
        drawArc(
            color = progressColor,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = topLeft,
            size = canvasSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun BudgetDisplay(
    monthlyBudget: Double,
    remainingBudget: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Budget text
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Monthly Budget",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "€${String.format("%.2f", remainingBudget)} remaining of €${String.format("%.2f", monthlyBudget)}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        // Donut chart with percentage
        Box(
            modifier = Modifier
                .size(80.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            val progress = (remainingBudget / monthlyBudget).coerceIn(0.0, 1.0).toFloat()
            
            // Draw the donut chart
            DonutChart(
                progress = progress,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                progressColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxSize()
            )
            
            // Overlay the percentage text
            val percentage = (progress * 100).toInt()
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
} 