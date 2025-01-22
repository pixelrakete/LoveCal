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

data class Partner2SetupUiState(
    val name: String = "",
    val color: Long = 0xFF000000,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val validationError: String? = null
)

@HiltViewModel
class Partner2SetupViewModel @Inject constructor(
    private val userManager: UserManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(Partner2SetupUiState())
    val uiState: StateFlow<Partner2SetupUiState> = _uiState.asStateFlow()

    companion object {
        private const val MIN_NAME_LENGTH = 2
        private const val MAX_NAME_LENGTH = 50
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(
            name = name,
            validationError = null
        ) }
    }

    fun updateColor(color: Long) {
        _uiState.update { it.copy(
            color = color,
            validationError = null
        ) }
    }

    fun completeSetup() {
        val currentState = _uiState.value
        
        if (!validateInput(currentState.name)) {
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                userManager.completePartner2Setup(
                    name = currentState.name,
                    color = currentState.color
                )
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

    private fun validateInput(name: String): Boolean {
        when {
            name.isBlank() -> {
                _uiState.update { it.copy(validationError = "Name is required") }
                return false
            }
            name.length < MIN_NAME_LENGTH -> {
                _uiState.update { it.copy(validationError = "Name must be at least $MIN_NAME_LENGTH characters long") }
                return false
            }
            name.length > MAX_NAME_LENGTH -> {
                _uiState.update { it.copy(validationError = "Name cannot exceed $MAX_NAME_LENGTH characters") }
                return false
            }
        }
        return true
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 