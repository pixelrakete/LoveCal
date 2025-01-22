package com.pixelrakete.lovecal.ui.setup

import androidx.lifecycle.viewModelScope
import com.pixelrakete.lovecal.data.manager.UserManager
import com.pixelrakete.lovecal.data.model.AuthState
import com.pixelrakete.lovecal.ui.base.BaseViewModel
import com.pixelrakete.lovecal.util.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetupUiState(
    val name: String = "",
    val color: String = "#2196F3",
    val interests: List<String> = emptyList(),
    val city: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val needsCalendar: Boolean = false,
    val error: String? = null,
    val nameError: String? = null
)

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val userManager: UserManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userManager.authState.collect { authState ->
                when (authState) {
                    is AuthState.NeedsCalendar -> {
                        _uiState.update { it.copy(needsCalendar = true) }
                    }
                    is AuthState.Error -> {
                        _uiState.update { it.copy(error = authState.message) }
                    }
                    else -> {}
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateColor(color: String) {
        _uiState.update { it.copy(color = color) }
    }

    fun updateInterests(interests: List<String>) {
        _uiState.update { it.copy(interests = interests) }
    }

    fun updateCity(city: String) {
        _uiState.update { it.copy(city = city) }
    }

    fun createCouple() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val currentState = _uiState.value
                userManager.createNewCouple(
                    currentState.name,
                    currentState.color,
                    currentState.interests,
                    currentState.city
                )
                _uiState.update { it.copy(
                    isLoading = false,
                    error = null
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = handleError(e)
                ) }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 