package com.pixelrakete.lovecal.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pixelrakete.lovecal.MainViewModel
import com.pixelrakete.lovecal.ui.calendar.CalendarSelectionScreen
import com.pixelrakete.lovecal.ui.home.HomeScreen
import com.pixelrakete.lovecal.ui.login.LoginScreen
import com.pixelrakete.lovecal.ui.plan.PlanDateScreen
import com.pixelrakete.lovecal.ui.settings.SettingsScreen
import com.pixelrakete.lovecal.ui.setup.CoupleDecisionScreen
import com.pixelrakete.lovecal.ui.setup.JoinScreen
import com.pixelrakete.lovecal.ui.setup.Partner2SetupScreen
import com.pixelrakete.lovecal.ui.wishes.DateWishesScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToCalendarSelection = {
                    navController.navigate(Screen.CoupleDecision.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToJoin = {
                    navController.navigate(Screen.Join.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CoupleDecision.route) {
            CoupleDecisionScreen(
                onCreateNewCouple = {
                    navController.navigate(Screen.CalendarSelection.route)
                },
                onJoinCouple = {
                    navController.navigate(Screen.Join.route)
                }
            )
        }

        composable(Screen.CalendarSelection.route) {
            CalendarSelectionScreen(
                onCalendarSelected = { calendarId ->
                    viewModel.setupCalendar(calendarId.toString())
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    viewModel.markSetupComplete()
                },
                isInitialSetup = true
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDateDetails = { dateId ->
                    navController.navigate(Screen.DateDetails.createRoute(dateId))
                },
                onNavigateToDateCreation = {
                    navController.navigate(Screen.PlanDate.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.DateWishes.route) {
            DateWishesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PlanDate.route) {
            PlanDateScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Join.route) {
            JoinScreen(
                onNavigateToSetup = { code ->
                    navController.navigate(Screen.Partner2Setup.route.replace("{code}", code)) {
                        popUpTo(Screen.Join.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Partner2Setup.route,
            arguments = listOf(navArgument("code") { type = NavType.StringType })
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code") ?: ""
            Partner2SetupScreen(
                invitationCode = code,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
    }
} 