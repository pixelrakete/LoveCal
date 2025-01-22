package com.pixelrakete.lovecal.ui.partner2Setup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelrakete.lovecal.data.manager.UserManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Partner2SetupUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val name: String = "",
    val nameError: String? = null,
    val color: String = "#F44336",
    val interests: List<String> = emptyList()
)

@HiltViewModel
class Partner2SetupViewModel @Inject constructor(
    private val userManager: UserManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(Partner2SetupUiState())
    val uiState: StateFlow<Partner2SetupUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = if (name.isBlank()) "Name cannot be empty" else null
        )
    }

    fun updateColor(color: String) {
        _uiState.value = _uiState.value.copy(color = color)
    }

    fun updateInterests(interests: List<String>) {
        _uiState.value = _uiState.value.copy(interests = interests)
    }

    fun completeSetup() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                if (_uiState.value.name.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        nameError = "Name cannot be empty"
                    )
                    return@launch
                }

                userManager.updatePartner2Settings(
                    name = _uiState.value.name,
                    color = _uiState.value.color,
                    interests = _uiState.value.interests
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } catch (e: Exception) {
                Log.e("Partner2SetupViewModel", "Error completing setup", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to complete setup: ${e.message}"
                )
            }
        }
    }

    fun retryLoading() {
        _uiState.value = Partner2SetupUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 