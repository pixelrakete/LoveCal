package com.pixelrakete.lovecal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.pixelrakete.lovecal.navigation.AuthState
import com.pixelrakete.lovecal.navigation.NavGraph
import com.pixelrakete.lovecal.navigation.Screen
import com.pixelrakete.lovecal.ui.theme.LoveCalTheme
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoveCalTheme {
                val navController = rememberNavController()
                val authState by viewModel.authState.collectAsState()

                LaunchedEffect(authState) {
                    when (authState) {
                        AuthState.LOADING -> {
                            // Show loading screen
                        }
                        AuthState.LOGGED_OUT -> {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                        AuthState.NEEDS_COUPLE_DECISION -> {
                            navController.navigate(Screen.CoupleDecision.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                        AuthState.NEEDS_CALENDAR -> {
                            navController.navigate(Screen.CalendarSelection.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                        AuthState.NEEDS_SETUP -> {
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                        AuthState.AUTHENTICATED -> {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(navController = navController, viewModel = viewModel)
                }
            }
        }
    }
}

fun NavHostController.navigateAndClearBackStack(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) { inclusive = true }
    }
} 