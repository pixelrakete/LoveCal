package com.pixelrakete.lovecal.ui.settings

import androidx.lifecycle.viewModelScope
import com.pixelrakete.lovecal.data.manager.UserManager
import com.pixelrakete.lovecal.data.model.CoupleSettings
import com.pixelrakete.lovecal.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val partner1Name: String = "",
    val partner1Color: String = "#2196F3",
    val partner1Interests: List<String> = emptyList(),
    val partner2Name: String = "",
    val partner2Color: String = "#F44336",
    val partner2Interests: List<String> = emptyList(),
    val monthlyBudget: Double = 500.0,
    val dateFrequencyWeeks: Int = 2,
    val city: String = "",
    val hasUnsavedChanges: Boolean = false,
    val shouldNavigateToLogin: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userManager: UserManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val settings = userManager.getCoupleSettings()
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        partner1Name = settings.partner1Name,
                        partner1Color = settings.partner1Color,
                        partner1Interests = settings.interests.take(settings.interests.size / 2),
                        partner2Name = settings.partner2Name,
                        partner2Color = settings.partner2Color,
                        partner2Interests = settings.interests.drop(settings.interests.size / 2),
                        monthlyBudget = settings.monthlyBudget,
                        dateFrequencyWeeks = settings.dateFrequencyWeeks,
                        city = settings.city,
                        hasUnsavedChanges = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = handleError(e)
                ) }
            }
        }
    }

    fun updateSettings(
        partner1Name: String,
        partner1Color: String,
        partner1Interests: List<String>,
        partner2Name: String,
        partner2Color: String,
        partner2Interests: List<String>,
        monthlyBudget: Double,
        dateFrequencyWeeks: Int,
        city: String
    ) {
        _uiState.update { it.copy(
            partner1Name = partner1Name,
            partner1Color = partner1Color,
            partner1Interests = partner1Interests,
            partner2Name = partner2Name,
            partner2Color = partner2Color,
            partner2Interests = partner2Interests,
            monthlyBudget = monthlyBudget,
            dateFrequencyWeeks = dateFrequencyWeeks,
            city = city,
            hasUnsavedChanges = true
        ) }
    }

    fun saveSettings() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val currentState = _uiState.value
                val allInterests = currentState.partner1Interests + currentState.partner2Interests
                val settings = CoupleSettings(
                    partner1Name = currentState.partner1Name,
                    partner1Color = currentState.partner1Color,
                    partner2Name = currentState.partner2Name,
                    partner2Color = currentState.partner2Color,
                    monthlyBudget = currentState.monthlyBudget,
                    dateFrequencyWeeks = currentState.dateFrequencyWeeks,
                    interests = allInterests,
                    city = currentState.city
                )
                userManager.updateCoupleSettings(settings)
                _uiState.update { it.copy(
                    isLoading = false,
                    isSuccess = true,
                    hasUnsavedChanges = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = handleError(e)
                ) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                userManager.signOut()
                _uiState.update { it.copy(
                    isLoading = false,
                    isSuccess = true,
                    shouldNavigateToLogin = true
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = handleError(e)
                ) }
            }
        }
    }

    fun deleteUserData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                userManager.deleteUserData()
                _uiState.update { it.copy(
                    isLoading = false,
                    isSuccess = true,
                    shouldNavigateToLogin = true
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
        _uiState.update { it.copy(error = null) }
    }
} 