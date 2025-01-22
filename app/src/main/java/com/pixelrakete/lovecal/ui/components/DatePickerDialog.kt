package com.pixelrakete.lovecal.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun DatePickerDialog(
    onDateTimeSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDateTime = LocalDateTime.of(
                LocalDate.of(year, month + 1, dayOfMonth),
                selectedDateTime.toLocalTime()
            )
            showTimePicker = true
        },
        selectedDateTime.year,
        selectedDateTime.monthValue - 1,
        selectedDateTime.dayOfMonth
    )
    
    DisposableEffect(Unit) {
        datePickerDialog.show()
        onDispose {
            datePickerDialog.dismiss()
        }
    }
    
    if (showTimePicker) {
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hour, minute ->
                selectedDateTime = selectedDateTime.with(LocalTime.of(hour, minute))
                onDateTimeSelected(selectedDateTime)
                onDismiss()
            },
            selectedDateTime.hour,
            selectedDateTime.minute,
            true // 24h format
        )
        
        DisposableEffect(Unit) {
            timePickerDialog.show()
            onDispose {
                timePickerDialog.dismiss()
            }
        }
    }
} 