package com.pixelrakete.lovecal.ui.login

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.pixelrakete.lovecal.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToCalendarSelection: () -> Unit,
    onNavigateToJoin: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showChoiceDialog by remember { mutableStateOf(false) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val account = GoogleSignIn.getLastSignedInAccount(viewModel.getContext())
            if (account != null) {
                viewModel.handleSignInResult(account)
            } else {
                Log.e("LoginScreen", "Sign-in successful but no account found")
                viewModel.handleSignInError(Exception("Sign-in successful but no account found"))
            }
        } else {
            Log.e("LoginScreen", "Sign-in cancelled or failed")
            viewModel.handleSignInError(Exception("Sign-in cancelled or failed"))
        }
    }

    LaunchedEffect(viewModel.loginResult.collectAsState().value) {
        viewModel.loginResult.collect { result ->
            when (result) {
                is LoginResult.Success -> {
                    onNavigateToCalendarSelection()
                }
                is LoginResult.NeedsCalendarPermission -> {
                    showPermissionDialog = true
                }
                is LoginResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
                null -> { /* Initial state, do nothing */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Love Calendar") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = "App Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Welcome to Love Calendar",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Plan and organize your dates with your loved one",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        viewModel.signInWithGoogle()
                        launcher.launch(viewModel.signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign in with Google")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onNavigateToJoin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Join Existing Couple")
                }
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Calendar Permission Required") },
            text = { Text("To use Love Calendar, we need access to your Google Calendar. Please sign in again to grant this permission.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        viewModel.signInWithGoogle()
                        launcher.launch(viewModel.signInIntent)
                    }
                ) {
                    Text("Sign In Again")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showChoiceDialog = false },
            title = { Text("Create or Join?") },
            text = { Text("Would you like to create a new couple or join an existing one?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showChoiceDialog = false
                        onNavigateToCalendarSelection()
                    }
                ) {
                    Text("Create New")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showChoiceDialog = false
                        onNavigateToJoin()
                    }
                ) {
                    Text("Join Existing")
                }
            }
        )
    }
} 