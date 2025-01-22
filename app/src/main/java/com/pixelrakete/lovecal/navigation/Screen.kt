package com.pixelrakete.lovecal.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Join : Screen("join")
    object Setup : Screen("setup")
    object DateWishes : Screen("date_wishes")
    object Partner2Setup : Screen("partner2_setup/{code}") {
        fun createRoute(code: String) = "partner2_setup/$code"
    }
    object PlanDate : Screen("plan_date/{dateId}") {
        fun createRoute(dateId: String?) = "plan_date/${dateId ?: "new"}"
    }
    object CalendarSelection : Screen("calendar_selection")
    object CoupleDecision : Screen("couple_decision")
    object DateDetails : Screen("date_details/{dateId}") {
        fun createRoute(dateId: String) = "date_details/$dateId"
    }

    companion object {
        const val DATE_ID_KEY = "dateId"
        const val CODE_KEY = "code"
    }
} 