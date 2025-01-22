package com.pixelrakete.lovecal.ui.wishes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pixelrakete.lovecal.R
import com.pixelrakete.lovecal.data.model.DateWish

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateWishesScreen(
    viewModel: DateWishesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.date_wishes)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddWishDialog() }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_wish))
            }
        }
    ) { padding ->
        Box(modifier = modifier.padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.error?.let { error ->
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    title = { Text("Error") },
                    text = { Text(error) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text(stringResource(R.string.ok))
                        }
                    }
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = stringResource(R.string.date_wishes),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )

                uiState.wishes.forEach { wish ->
                    DateWishCard(
                        wish = wish,
                        onDeleteClick = { viewModel.deleteDateWish(wish.id) }
                    )
                }
            }

            if (uiState.showAddWishDialog) {
                AddWishDialog(
                    title = uiState.newWishTitle,
                    description = uiState.newWishDescription,
                    validationErrors = uiState.validationErrors,
                    onTitleChange = viewModel::onTitleChanged,
                    onDescriptionChange = viewModel::onDescriptionChanged,
                    onDismiss = viewModel::hideAddWishDialog,
                    onSave = viewModel::saveDateWish
                )
            }
        }
    }
}

@Composable
private fun DateWishCard(
    wish: DateWish,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = wish.title,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete wish")
                }
            }
            if (wish.description.isNotBlank()) {
                Text(
                    text = wish.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWishDialog(
    title: String,
    description: String,
    validationErrors: Map<String, String>,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_wish)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text(stringResource(R.string.title)) },
                    isError = validationErrors.containsKey("title"),
                    supportingText = validationErrors["title"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text(stringResource(R.string.description)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
} 