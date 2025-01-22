package com.pixelrakete.lovecal

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.pixelrakete.lovecal.data.manager.UserManager
import com.pixelrakete.lovecal.data.model.AuthState
import com.pixelrakete.lovecal.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class MainUiState(
    val authState: AuthState = AuthState.Initial,
    val error: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userManager: UserManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            try {
                userManager.authState
                    .onStart { _uiState.update { it.copy(authState = AuthState.Loading) } }
                    .catch { e ->
                        val errorMessage = when (e) {
                            is FirebaseAuthException -> "Authentication error: ${e.message}"
                            is FirebaseFirestoreException -> "Database error: ${e.message}"
                            is IOException -> "Network error: ${e.message}"
                            else -> e.message ?: "An unknown error occurred"
                        }
                        _uiState.update { it.copy(
                            authState = AuthState.Error(errorMessage),
                            error = errorMessage
                        ) }
                    }
                    .collect { authState ->
                        _uiState.update { it.copy(authState = authState) }
                    }
            } catch (e: Exception) {
                val errorMessage = handleError(e)
                _uiState.update { it.copy(
                    authState = AuthState.Error(errorMessage),
                    error = errorMessage
                ) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 