package com.pixelrakete.lovecal.ui.login

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.pixelrakete.lovecal.data.manager.UserManager
import com.pixelrakete.lovecal.data.model.AuthState
import com.pixelrakete.lovecal.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userManager: UserManager
) : BaseViewModel() {
    private val TAG = "LoginViewModel"

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    val authState = userManager.authState.onEach { state ->
        Log.d(TAG, "Auth state changed to: $state")
    }

    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting Google sign in")
                _uiState.update { it.copy(isLoading = true) }
                userManager.signInWithGoogle(account)
                Log.d(TAG, "Google sign in completed")
                _uiState.update { it.copy(isLoading = false, error = null) }
            } catch (e: Exception) {
                Log.e(TAG, "Error signing in with Google", e)
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to sign in"
                ) }
            }
        }
    }
} 