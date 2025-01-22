package com.pixelrakete.lovecal.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.pixelrakete.lovecal.data.manager.UserManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class MainUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val startDestination: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userManager: UserManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
} 