package com.pixelrakete.lovecal.ui.wishes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelrakete.lovecal.data.model.DateWish
import com.pixelrakete.lovecal.data.repository.DateWishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DateWishesUiState(
    val wishes: List<DateWish> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddWishDialog: Boolean = false,
    val newWishTitle: String = "",
    val newWishDescription: String = "",
    val validationErrors: Map<String, String> = emptyMap()
)

@HiltViewModel
class DateWishesViewModel @Inject constructor(
    private val dateWishRepository: DateWishRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DateWishesUiState())
    val uiState: StateFlow<DateWishesUiState> = _uiState.asStateFlow()

    init {
        loadWishes()
    }

    private fun loadWishes() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                dateWishRepository.getDateWishes()
                    .collect { wishes ->
                        _uiState.update { it.copy(
                            wishes = wishes,
                            isLoading = false
                        ) }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = e.message ?: "Failed to load wishes",
                    isLoading = false
                ) }
            }
        }
    }

    fun showAddWishDialog() {
        _uiState.update { it.copy(
            showAddWishDialog = true,
            newWishTitle = "",
            newWishDescription = "",
            validationErrors = emptyMap()
        ) }
    }

    fun hideAddWishDialog() {
        _uiState.update { it.copy(showAddWishDialog = false) }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(newWishTitle = title) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(newWishDescription = description) }
    }

    fun saveDateWish() {
        val validationErrors = validateWish()
        if (validationErrors.isNotEmpty()) {
            _uiState.update { it.copy(validationErrors = validationErrors) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                dateWishRepository.saveDateWish(
                    title = _uiState.value.newWishTitle,
                    description = _uiState.value.newWishDescription
                )
                _uiState.update { it.copy(
                    showAddWishDialog = false,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = e.message ?: "Failed to save wish",
                    isLoading = false
                ) }
            }
        }
    }

    fun deleteDateWish(wishId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                dateWishRepository.deleteDateWish(wishId)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = e.message ?: "Failed to delete wish",
                    isLoading = false
                ) }
            }
        }
    }

    private fun validateWish(): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        
        if (_uiState.value.newWishTitle.isBlank()) {
            errors["title"] = "Title is required"
        }
        
        return errors
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 