package com.pixelrakete.lovecal.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelrakete.lovecal.ui.calendar.CalendarSelectionScreen
import com.pixelrakete.lovecal.ui.calendar.CalendarSelectionViewModel
import com.pixelrakete.lovecal.ui.home.HomeScreen
import com.pixelrakete.lovecal.ui.home.HomeViewModel
import com.pixelrakete.lovecal.ui.login.LoginScreen
import com.pixelrakete.lovecal.ui.plan.PlanDateScreen
import com.pixelrakete.lovecal.ui.settings.SettingsScreen
import com.pixelrakete.lovecal.ui.setup.SetupScreen
import com.pixelrakete.lovecal.ui.setup.CoupleDecisionScreen
import com.pixelrakete.lovecal.ui.setup.JoinScreen
import com.pixelrakete.lovecal.ui.wishes.DateWishesScreen
import com.pixelrakete.lovecal.ui.wishes.DateWishesViewModel
import com.pixelrakete.lovecal.ui.setup.Partner2SetupScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    onSignInWithGoogle: () -> Unit
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSetup = {
                    navController.navigate(Screen.Setup.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToCalendarSelection = {
                    navController.navigate(Screen.CalendarSelection.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToCoupleDecision = {
                    navController.navigate(Screen.CoupleDecision.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSignInWithGoogle = onSignInWithGoogle
            )
        }
        composable(Screen.CoupleDecision.route) {
            CoupleDecisionScreen(
                onCreateNewCouple = {
                    navController.navigate(Screen.Setup.route) {
                        popUpTo(Screen.CoupleDecision.route) { inclusive = true }
                    }
                },
                onJoinCouple = {
                    navController.navigate(Screen.Join.route) {
                        popUpTo(Screen.CoupleDecision.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                uiState = uiState,
                onNavigateToCreateDatePlan = { navController.navigate(Screen.PlanDate.createRoute(null)) },
                onNavigateToEditDatePlan = { dateId -> navController.navigate(Screen.PlanDate.createRoute(dateId)) },
                onDeleteDatePlan = viewModel::deleteDatePlan,
                onCompleteDatePlan = viewModel::updateDatePlanCompleted,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.DateWishes.route) {
            val viewModel = hiltViewModel<DateWishesViewModel>()
            DateWishesScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.PlanDate.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("dateId")
            PlanDateScreen(
                dateId = if (id == "new") null else id,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.CalendarSelection.route) {
            val viewModel = hiltViewModel<CalendarSelectionViewModel>()
            CalendarSelectionScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.CalendarSelection.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Join.route) {
            JoinScreen(
                onNavigateToPartner2Setup = {
                    navController.navigate(Screen.Partner2Setup.route) {
                        popUpTo(Screen.Join.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Setup.route) {
            SetupScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                },
                onNavigateToCalendarSelection = {
                    navController.navigate(Screen.CalendarSelection.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Partner2Setup.route) {
            Partner2SetupScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Partner2Setup.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
} 