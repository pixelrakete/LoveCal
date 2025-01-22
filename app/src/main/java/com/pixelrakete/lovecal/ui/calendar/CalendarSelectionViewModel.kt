package com.pixelrakete.lovecal.ui.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.services.calendar.CalendarScopes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.pixelrakete.lovecal.data.repository.CalendarRepository
import com.pixelrakete.lovecal.data.repository.Calendar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class CalendarSelectionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val calendarId: Long = -1L,
    val availableCalendars: List<Calendar> = emptyList(),
    val shouldNavigateToLogin: Boolean = false
)

@HiltViewModel
class CalendarSelectionViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val auth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarSelectionUiState())
    val uiState: StateFlow<CalendarSelectionUiState> = _uiState

    init {
        // Listen for Firebase auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                Log.d("CalendarSelection", "Firebase auth state changed: User signed out")
                // Only navigate to login if we have no valid Google account and we're not in the process of signing in
                if (!_uiState.value.shouldNavigateToLogin && !_uiState.value.isLoading) {
                    val googleAccount = GoogleSignIn.getLastSignedInAccount(googleSignInClient.applicationContext)
                    if (googleAccount == null || googleAccount.idToken == null) {
                        Log.d("CalendarSelection", "No valid Google account found, navigating to login")
                        _uiState.value = _uiState.value.copy(shouldNavigateToLogin = true)
                    } else {
                        // If we have a Google account but no Firebase auth, try to reestablish it
                        viewModelScope.launch {
                            tryReestablishFirebaseAuth()
                        }
                    }
                }
            } else {
                Log.d("CalendarSelection", "Firebase auth state changed: User signed in: ${user.email}")
                // Only check calendar auth if we're not in the process of signing in
                if (!_uiState.value.isLoading) {
                    checkGoogleCalendarAuth()
                }
            }
        }
    }

    private fun checkGoogleCalendarAuth() {
        val googleAccount = GoogleSignIn.getLastSignedInAccount(googleSignInClient.applicationContext)
        if (googleAccount?.grantedScopes?.any { it.scopeUri == CalendarScopes.CALENDAR } != true) {
            Log.d("CalendarSelection", "Missing calendar scope, navigating to login")
            _uiState.value = _uiState.value.copy(shouldNavigateToLogin = true)
        }
    }

    private suspend fun tryReestablishFirebaseAuth() {
        val googleAccount = GoogleSignIn.getLastSignedInAccount(googleSignInClient.applicationContext)
        if (googleAccount != null && googleAccount.idToken != null) {
            Log.d("CalendarSelection", "Attempting to reestablish Firebase auth with Google account: ${googleAccount.email}")
            try {
                val credential = GoogleAuthProvider.getCredential(googleAccount.idToken, null)
                auth.signInWithCredential(credential).await()
                Log.d("CalendarSelection", "Successfully reestablished Firebase auth")
                
                // Check if we have all necessary permissions
                if (googleAccount.grantedScopes.any { it.scopeUri == CalendarScopes.CALENDAR }) {
                    // We're fully authenticated, no need to navigate
                    _uiState.value = _uiState.value.copy(shouldNavigateToLogin = false)
                    return
                }
            } catch (e: Exception) {
                Log.e("CalendarSelection", "Failed to reestablish Firebase auth", e)
            }
        }
        
        // If we get here, we need to navigate to login
        Log.d("CalendarSelection", "User not logged in, navigating to login")
        _uiState.value = _uiState.value.copy(shouldNavigateToLogin = true)
    }

    fun navigateToLogin() {
        _uiState.value = _uiState.value.copy(shouldNavigateToLogin = true)
    }

    fun resetNavigation() {
        _uiState.value = _uiState.value.copy(shouldNavigateToLogin = false)
    }

    fun isUserLoggedIn(): Boolean {
        val firebaseUser = auth.currentUser
        val googleAccount = GoogleSignIn.getLastSignedInAccount(googleSignInClient.applicationContext)
        
        val isFirebaseLoggedIn = firebaseUser != null
        val isGoogleLoggedIn = googleAccount != null
        val hasCalendarScope = googleAccount?.grantedScopes?.any { it.scopeUri == CalendarScopes.CALENDAR } == true
        
        Log.d("CalendarSelection", "Auth status - Firebase: $isFirebaseLoggedIn, Google: $isGoogleLoggedIn, Calendar Scope: $hasCalendarScope")
        
        // Don't trigger reauth if we're in the process of signing in
        if (!isFirebaseLoggedIn && isGoogleLoggedIn && !_uiState.value.isLoading) {
            viewModelScope.launch {
                tryReestablishFirebaseAuth()
            }
            return false
        }
        
        return isFirebaseLoggedIn && isGoogleLoggedIn && hasCalendarScope
    }

    fun loadCalendars() {
        viewModelScope.launch {
            try {
                if (!isUserLoggedIn()) {
                    Log.d("CalendarSelection", "User not logged in, skipping calendar load")
                    return@launch
                }
                
                Log.d("CalendarSelection", "Loading calendars")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val calendars = calendarRepository.getCalendars()
                Log.d("CalendarSelection", "Found ${calendars.size} calendars")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    availableCalendars = calendars
                )
            } catch (e: Exception) {
                Log.e("CalendarSelection", "Error loading calendars", e)
                if (e.message?.contains("not logged in", ignoreCase = true) == true) {
                    Log.d("CalendarSelection", "Not logged in error detected, navigating to login")
                    navigateToLogin()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load calendars",
                        success = false,
                        calendarId = -1L
                    )
                }
            }
        }
    }

    fun setLoading(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = loading)
    }

    fun selectCalendar(calendarId: Long) {
        viewModelScope.launch {
            try {
                if (!isUserLoggedIn()) {
                    Log.d("CalendarSelection", "User not logged in, skipping calendar selection")
                    navigateToLogin()
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null
                )

                // Add a small delay to ensure UI state is consistent
                delay(100)

                // First update success and calendarId
                _uiState.value = _uiState.value.copy(
                    error = null,
                    success = true,
                    calendarId = calendarId
                )

                // Then update loading state in a separate update
                delay(50)
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("CalendarSelection", "Error selecting calendar", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to select calendar",
                    success = false,
                    calendarId = -1L
                )
            }
        }
    }

    fun createCalendar() {
        viewModelScope.launch {
            try {
                if (!isUserLoggedIn()) {
                    Log.d("CalendarSelection", "User not logged in, skipping calendar creation")
                    return@launch
                }
                
                Log.d("CalendarSelection", "Creating calendar")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val calendarId = calendarRepository.createCalendar("LoveCal")
                Log.d("CalendarSelection", "Calendar created with ID: $calendarId")
                
                if (calendarId != -1L) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        success = true,
                        calendarId = calendarId
                    )
                } else {
                    Log.e("CalendarSelection", "Failed to create calendar")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to create calendar",
                        success = false,
                        calendarId = -1L
                    )
                }
            } catch (e: Exception) {
                Log.e("CalendarSelection", "Error creating calendar", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = when {
                        e.message?.contains("No couple found") == true -> "Please complete the setup first"
                        e.message?.contains("not logged in", ignoreCase = true) == true -> {
                            navigateToLogin()
                            "Not logged in"
                        }
                        else -> e.message ?: "Failed to create calendar"
                    },
                    success = false,
                    calendarId = -1L
                )
            }
        }
    }

    fun resetSuccess() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = false)
            delay(50)
            _uiState.value = _uiState.value.copy(
                success = false,
                calendarId = -1L,
                error = "Failed to set up calendar. Please try again."
            )
        }
    }
} 