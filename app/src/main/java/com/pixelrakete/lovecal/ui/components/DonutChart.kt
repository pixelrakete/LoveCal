package com.pixelrakete.lovecal.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DonutChart(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 12.dp,
    backgroundColor: Color = Color(0x33FF4081), // Light pink background
    foregroundColor: Color = Color(0xFFFF4081)  // Pink foreground
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val stroke = strokeWidth.toPx()
        
        // Draw background circle
        drawCircle(
            color = backgroundColor,
            radius = (size.minDimension - stroke) / 2,
            center = Offset(canvasWidth / 2, canvasHeight / 2),
            style = Stroke(width = stroke)
        )
        
        // Draw progress arc
        val startAngle = -90f
        val sweepAngle = progress * 360f
        drawArc(
            color = foregroundColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(canvasWidth - stroke, canvasHeight - stroke),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
} 