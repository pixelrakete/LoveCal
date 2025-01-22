package com.pixelrakete.lovecal.ui.setup

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

data class Partner2SetupUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class Partner2SetupViewModel @Inject constructor(
    private val coupleRepository: CoupleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(Partner2SetupUiState())
    val uiState: StateFlow<Partner2SetupUiState> = _uiState.asStateFlow()

    fun completeSetup(
        invitationCode: String,
        name: String,
        color: String,
        interests: List<String>
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                coupleRepository.completePartner2Setup(
                    invitationCode = invitationCode,
                    partner2Name = name,
                    partner2Color = color,
                    partner2Interests = interests
                )
                
                _uiState.update { it.copy(
                    isLoading = false,
                    success = true
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message
                ) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 