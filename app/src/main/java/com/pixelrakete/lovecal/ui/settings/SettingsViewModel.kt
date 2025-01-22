package com.pixelrakete.lovecal.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelrakete.lovecal.data.repository.CoupleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val partner1Name: String = "",
    val partner2Name: String = "",
    val partner1Color: String = "#FF4081",
    val partner2Color: String = "#2196F3",
    val partner1Interests: List<String> = emptyList(),
    val partner2Interests: List<String> = emptyList(),
    val monthlyBudget: Double = 500.0,
    val city: String = "",
    val isInitialSetup: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val coupleRepository: CoupleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun setInitialSetup() {
        _uiState.update { it.copy(isInitialSetup = true) }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val couple = coupleRepository.getCurrentCouple()
                if (couple != null) {
                    _uiState.update { state ->
                        state.copy(
                            partner1Name = couple.partner1Name,
                            partner2Name = couple.partner2Name ?: "",
                            partner1Color = couple.partner1Color,
                            partner2Color = couple.partner2Color,
                            partner1Interests = couple.partner1Interests,
                            partner2Interests = couple.partner2Interests,
                            monthlyBudget = couple.monthlyBudget ?: 500.0,
                            city = couple.city ?: "",
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load settings"
                ) }
            }
        }
    }

    fun createCouple() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                if (uiState.value.partner1Name.isBlank()) {
                    throw IllegalStateException("Please enter your name")
                }
                
                coupleRepository.createCouple(
                    partner1Name = uiState.value.partner1Name,
                    partner1Color = uiState.value.partner1Color,
                    monthlyBudget = uiState.value.monthlyBudget,
                    city = uiState.value.city
                )
                
                _uiState.update { it.copy(
                    isLoading = false,
                    success = true
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create couple"
                ) }
            }
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                if (uiState.value.partner1Name.isBlank()) {
                    throw IllegalStateException("Please enter your name")
                }
                
                coupleRepository.updateCouple(
                    partner1Name = uiState.value.partner1Name,
                    partner2Name = uiState.value.partner2Name,
                    partner1Color = uiState.value.partner1Color,
                    partner2Color = uiState.value.partner2Color,
                    partner1Interests = uiState.value.partner1Interests,
                    partner2Interests = uiState.value.partner2Interests,
                    monthlyBudget = uiState.value.monthlyBudget,
                    city = uiState.value.city
                )
                
                _uiState.update { it.copy(
                    isLoading = false,
                    success = true
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save settings"
                ) }
            }
        }
    }

    fun updatePartner1Name(name: String) {
        _uiState.update { it.copy(partner1Name = name) }
    }

    fun updatePartner2Name(name: String) {
        _uiState.update { it.copy(partner2Name = name) }
    }

    fun updatePartner1Color(color: String) {
        _uiState.update { it.copy(partner1Color = color) }
    }

    fun updatePartner2Color(color: String) {
        _uiState.update { it.copy(partner2Color = color) }
    }

    fun updatePartner1Interests(interests: List<String>) {
        _uiState.update { it.copy(partner1Interests = interests) }
    }

    fun updatePartner2Interests(interests: List<String>) {
        _uiState.update { it.copy(partner2Interests = interests) }
    }

    fun updateMonthlyBudget(budget: Double) {
        _uiState.update { it.copy(monthlyBudget = budget) }
    }

    fun updateCity(city: String) {
        _uiState.update { it.copy(city = city) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 