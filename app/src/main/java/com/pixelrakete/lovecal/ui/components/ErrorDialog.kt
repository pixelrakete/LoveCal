package com.pixelrakete.lovecal.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.pixelrakete.lovecal.data.model.AppError

@Composable
fun ErrorDialog(
    error: AppError,
    onDismiss: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val title = when (error) {
        is AppError.NetworkError -> "Network Error"
        is AppError.AuthenticationError -> "Authentication Error"
        is AppError.ValidationError -> "Validation Error"
        is AppError.DatabaseError -> "Database Error"
        is AppError.CalendarError -> "Calendar Error"
        is AppError.PermissionError -> "Permission Error"
        is AppError.UnknownError -> "Error"
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(error.getUserMessage()) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
} 