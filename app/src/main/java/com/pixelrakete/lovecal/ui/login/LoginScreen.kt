package com.pixelrakete.lovecal.ui.login

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pixelrakete.lovecal.R
import com.pixelrakete.lovecal.data.model.AuthState
import com.pixelrakete.lovecal.ui.login.LoginViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToSetup: () -> Unit = {},
    onNavigateToCalendarSelection: () -> Unit = {},
    onNavigateToCoupleDecision: () -> Unit = {},
    onSignInWithGoogle: () -> Unit = {}
) {
    val authState by viewModel.authState.collectAsState(initial = AuthState.Initial)

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> onNavigateToHome()
            is AuthState.NeedsSetup -> onNavigateToSetup()
            is AuthState.NeedsCalendar -> onNavigateToCalendarSelection()
            is AuthState.NeedsCoupleDecision -> onNavigateToCoupleDecision()
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "LoveCal Logo",
            modifier = Modifier.size(200.dp),
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "LoveCal",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onSignInWithGoogle,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_white),
                    contentDescription = "Google Logo",
                    tint = Color.Unspecified
                )
                Text("Sign in with Google")
            }
        }

        if (authState is AuthState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        }

        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
} 