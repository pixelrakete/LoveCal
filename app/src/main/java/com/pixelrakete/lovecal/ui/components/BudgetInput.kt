package com.pixelrakete.lovecal.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.pixelrakete.lovecal.R
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetInput(
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val numberFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    var textFieldValue by remember(value) { 
        mutableStateOf(if (value == 0.0) "" else value.toString())
    }

    Column {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue.filter { it.isDigit() || it == '.' }
                val parsedValue = newValue.toDoubleOrNull() ?: 0.0
                onValueChange(parsedValue)
            },
            label = { Text(stringResource(R.string.plan_date_budget)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            enabled = enabled,
            isError = isError,
            supportingText = if (isError && !errorMessage.isNullOrBlank()) {
                { Text(errorMessage) }
            } else {
                null
            },
            modifier = modifier
        )

        if (!isError && value > 0) {
            Text(
                text = numberFormat.format(value),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 