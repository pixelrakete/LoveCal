package com.pixelrakete.lovecal

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelrakete.lovecal.data.repository.AuthRepository
import com.pixelrakete.lovecal.data.repository.CoupleRepository
import com.pixelrakete.lovecal.navigation.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val coupleRepository: CoupleRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState.LOADING)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _authState.value = AuthState.LOGGED_OUT
                    return@launch
                }

                val couple = coupleRepository.getCurrentCouple()
                if (couple == null) {
                    // No couple exists, need to create or join
                    _authState.value = AuthState.NEEDS_COUPLE_DECISION
                } else if (couple.partner1CalendarId == null && couple.partner2CalendarId == null) {
                    // Couple exists but no calendar selected
                    _authState.value = AuthState.NEEDS_CALENDAR
                } else if (!couple.setupComplete) {
                    // Calendar selected but setup not complete
                    _authState.value = AuthState.NEEDS_SETUP
                } else {
                    // Everything complete
                    _authState.value = AuthState.AUTHENTICATED
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error checking auth state", e)
                _authState.value = AuthState.LOGGED_OUT
            }
        }
    }

    fun setupCalendar(calendarId: String) {
        viewModelScope.launch {
            try {
                // First check if we can get the current user
                val currentUser = try {
                    authRepository.getCurrentUser()
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Error getting current user", e)
                    _authState.value = AuthState.LOGGED_OUT
                    return@launch
                }

                if (currentUser == null) {
                    Log.d("MainViewModel", "No current user found")
                    _authState.value = AuthState.LOGGED_OUT
                    return@launch
                }

                // Then try to get the current couple
                val couple = try {
                    coupleRepository.getCurrentCouple()
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Error getting current couple", e)
                    // Store calendar ID before changing state
                    try {
                        coupleRepository.storeTemporaryCalendarId(calendarId)
                    } catch (e2: Exception) {
                        Log.e("MainViewModel", "Error storing temporary calendar ID", e2)
                    }
                    _authState.value = AuthState.NEEDS_SETUP
                    return@launch
                }

                if (couple == null) {
                    Log.d("MainViewModel", "No couple found, storing temporary calendar ID")
                    // Store calendar ID temporarily for later
                    try {
                        coupleRepository.storeTemporaryCalendarId(calendarId)
                    } catch (e: Exception) {
                        Log.e("MainViewModel", "Error storing temporary calendar ID", e)
                    }
                    _authState.value = AuthState.NEEDS_SETUP
                } else {
                    // Set up calendar for existing couple
                    try {
                        Log.d("MainViewModel", "Setting up calendar for existing couple")
                        coupleRepository.setupCalendar(calendarId, couple.partner1Id == currentUser.uid)
                        
                        // Add a small delay before checking setup completion
                        delay(100)
                        
                        val updatedCouple = coupleRepository.getCurrentCouple()
                        if (updatedCouple == null || !updatedCouple.setupComplete) {
                            Log.d("MainViewModel", "Couple setup not complete")
                            _authState.value = AuthState.NEEDS_SETUP
                        } else {
                            Log.d("MainViewModel", "Setup complete, moving to authenticated state")
                            // Use a separate coroutine for state transition
                            launch {
                                delay(100) // Small delay to ensure UI is ready
                                _authState.value = AuthState.AUTHENTICATED
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MainViewModel", "Error setting up calendar", e)
                        // If we fail to set up the calendar, store it temporarily
                        try {
                            coupleRepository.storeTemporaryCalendarId(calendarId)
                        } catch (e2: Exception) {
                            Log.e("MainViewModel", "Error storing temporary calendar ID", e2)
                        }
                        // Add a small delay before state transition
                        delay(100)
                        _authState.value = AuthState.NEEDS_SETUP
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error in setupCalendar", e)
                // Add a small delay before state transition
                delay(100)
                _authState.value = AuthState.LOGGED_OUT
            }
        }
    }

    fun markSetupComplete() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser() ?: run {
                    _authState.value = AuthState.LOGGED_OUT
                    return@launch
                }
                
                // Get and set up the stored calendar ID
                val tempCalendarId = coupleRepository.getTemporaryCalendarId()
                if (tempCalendarId != null) {
                    coupleRepository.setupCalendar(tempCalendarId, true)
                }
                
                coupleRepository.markSetupComplete()
                _authState.value = AuthState.AUTHENTICATED
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error marking setup complete", e)
            }
        }
    }
} 