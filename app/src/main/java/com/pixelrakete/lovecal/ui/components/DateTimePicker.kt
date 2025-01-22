package com.pixelrakete.lovecal.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun DateTimePicker(
    onDateTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            showTimePicker = true
        },
        selectedDate.year,
        selectedDate.monthValue - 1,
        selectedDate.dayOfMonth
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
                selectedTime = LocalTime.of(hour, minute)
                val dateStr = String.format(
                    "%02d.%02d.%04d %02d:%02d",
                    selectedDate.dayOfMonth,
                    selectedDate.monthValue,
                    selectedDate.year,
                    selectedTime.hour,
                    selectedTime.minute
                )
                onDateTimeSelected(dateStr)
                onDismiss()
            },
            selectedTime.hour,
            selectedTime.minute,
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