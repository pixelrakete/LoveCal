package com.pixelrakete.lovecal.data.model

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object LoggedOut : AuthState()
    object NeedsCoupleDecision : AuthState()
    object NeedsCalendar : AuthState()
    object NeedsSetup : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
} 