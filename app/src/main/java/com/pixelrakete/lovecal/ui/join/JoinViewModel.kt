package com.pixelrakete.lovecal.ui.join

import androidx.lifecycle.viewModelScope
import com.pixelrakete.lovecal.data.manager.UserManager
import com.pixelrakete.lovecal.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JoinUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class JoinViewModel @Inject constructor(
    private val userManager: UserManager
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(JoinUiState())
    val uiState: StateFlow<JoinUiState> = _uiState.asStateFlow()

    fun updateCode(code: String) {
        _uiState.update { it.copy(code = code) }
    }

    fun joinCouple(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                if (!validateInput()) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Please enter a valid code"
                    ) }
                    return@launch
                }
                
                userManager.joinCouple(_uiState.value.code)
                
                _uiState.update { it.copy(isLoading = false, error = null) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to join couple"
                ) }
            }
        }
    }

    private fun validateInput(): Boolean {
        val code = _uiState.value.code.trim()
        return when {
            code.isBlank() -> {
                _uiState.update { it.copy(error = "Please enter an invitation code") }
                false
            }
            code.length != 6 -> {
                _uiState.update { it.copy(error = "Invitation code must be 6 characters") }
                false
            }
            !code.matches(Regex("^[A-Z0-9]{6}$")) -> {
                _uiState.update { it.copy(error = "Invalid code format. Use only uppercase letters and numbers") }
                false
            }
            else -> true
        }
    }
} 