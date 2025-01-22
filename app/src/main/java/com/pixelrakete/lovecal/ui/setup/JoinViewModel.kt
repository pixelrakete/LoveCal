package com.pixelrakete.lovecal.ui.setup

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.pixelrakete.lovecal.data.manager.UserManager
import com.pixelrakete.lovecal.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class JoinUiState(
    val invitationCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val validationError: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class JoinViewModel @Inject constructor(
    private val userManager: UserManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(JoinUiState())
    val uiState: StateFlow<JoinUiState> = _uiState.asStateFlow()

    fun updateInvitationCode(code: String) {
        _uiState.update { it.copy(
            invitationCode = code,
            validationError = null
        ) }
    }

    fun joinCouple() {
        val currentState = _uiState.value
        
        if (!validateInvitationCode(currentState.invitationCode)) {
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                userManager.joinCouple(currentState.invitationCode)
                _uiState.update { it.copy(
                    isLoading = false,
                    isSuccess = true,
                    validationError = null
                ) }
            } catch (e: Exception) {
                val errorMessage = handleError(e)
                _uiState.update { it.copy(
                    error = errorMessage,
                    isLoading = false
                ) }
            }
        }
    }

    private fun validateInvitationCode(code: String): Boolean {
        when {
            code.isBlank() -> {
                _uiState.update { it.copy(validationError = "Invitation code is required") }
                return false
            }
            !code.matches(Regex("^[A-Za-z0-9]{6}$")) -> {
                _uiState.update { it.copy(validationError = "Invalid invitation code format") }
                return false
            }
        }
        return true
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 