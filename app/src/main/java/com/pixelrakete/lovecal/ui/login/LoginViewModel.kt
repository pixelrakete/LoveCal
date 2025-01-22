package com.pixelrakete.lovecal.ui.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes
import com.pixelrakete.lovecal.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val needsCalendarPermission: Boolean = false
)

sealed class LoginResult {
    object Success : LoginResult()
    object NeedsCalendarPermission : LoginResult()
    data class Error(val message: String) : LoginResult()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleSignInClient: GoogleSignInClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _loginResult = MutableStateFlow<LoginResult?>(null)
    val loginResult: StateFlow<LoginResult?> = _loginResult

    // Request Calendar scope during sign in
    val signInIntent = googleSignInClient.signInIntent.apply {
        putExtra("prompt", "select_account")
    }

    fun getContext(): Context = context

    fun signInWithGoogle() {
        try {
            Log.d("LoginViewModel", "Starting Google Sign-In")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            _loginResult.value = null
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error starting sign in", e)
            handleSignInError(e)
        }
    }

    fun handleSignInError(e: Exception) {
        Log.e("LoginViewModel", "Handling sign in error", e)
        _loginResult.value = LoginResult.Error(e.message ?: "An unexpected error occurred")
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = e.message ?: "An unexpected error occurred"
        )
    }

    fun resetLoginResult() {
        Log.d("LoginViewModel", "Resetting login result")
        _loginResult.value = null
        _uiState.value = _uiState.value.copy(isLoading = false, error = null)
    }

    fun handleSignInResult(account: GoogleSignInAccount?) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                if (account != null) {
                    Log.d("LoginViewModel", "Account signed in: ${account.email}")
                    
                    // First check calendar permissions
                    val hasCalendarScope = account.grantedScopes.any { 
                        it.scopeUri == CalendarScopes.CALENDAR 
                    }
                    
                    Log.d("LoginViewModel", "Has Calendar permission: $hasCalendarScope")
                    
                    if (hasCalendarScope) {
                        // Then establish Firebase authentication
                        val user = authRepository.signInWithGoogle(account)
                        if (user != null) {
                            Log.d("LoginViewModel", "Firebase auth successful: ${user.email}")
                            _loginResult.value = LoginResult.Success
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        } else {
                            Log.e("LoginViewModel", "Firebase auth failed")
                            _loginResult.value = LoginResult.Error("Failed to sign in")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Failed to sign in"
                            )
                        }
                    } else {
                        Log.d("LoginViewModel", "Need Calendar permission")
                        // Sign out from Google to force a new sign-in with calendar permissions
                        googleSignInClient.signOut()
                        _loginResult.value = LoginResult.NeedsCalendarPermission
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            needsCalendarPermission = true
                        )
                    }
                } else {
                    Log.e("LoginViewModel", "Sign in cancelled")
                    _loginResult.value = LoginResult.Error("Sign in cancelled")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Sign in cancelled"
                    )
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error handling sign in result", e)
                handleSignInError(e)
            }
        }
    }
} 