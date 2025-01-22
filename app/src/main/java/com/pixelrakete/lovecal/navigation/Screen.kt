package com.pixelrakete.lovecal.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object CalendarSelection : Screen("calendar_selection")
    object Settings : Screen("settings")
    object Join : Screen("join")
    object Partner2Setup : Screen("partner2_setup/{code}")
    object Home : Screen("home")
    object PlanDate : Screen("plan_date")
    object DateDetails : Screen("date_details/{dateId}") {
        fun createRoute(dateId: String) = "date_details/$dateId"
    }
    object DateWishes : Screen("date_wishes")
    object CoupleDecision : Screen("couple_decision")
} 