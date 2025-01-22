package com.pixelrakete.lovecal.ui.wishes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pixelrakete.lovecal.data.model.DateWish
import com.pixelrakete.lovecal.data.model.Couple
import com.pixelrakete.lovecal.data.repository.DateWishRepository
import com.pixelrakete.lovecal.data.repository.CoupleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DateWishesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val wishes: List<DateWish> = emptyList(),
    val couple: Couple? = null
)

@HiltViewModel
class DateWishesViewModel @Inject constructor(
    private val dateWishRepository: DateWishRepository,
    private val coupleRepository: CoupleRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(DateWishesUiState())
    val uiState: StateFlow<DateWishesUiState> = _uiState.asStateFlow()

    init {
        loadWishes()
    }

    private fun loadWishes() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val wishes = dateWishRepository.getDateWishes(userId)
                    val couple = coupleRepository.getCurrentCouple()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        wishes = wishes,
                        couple = couple
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun addWish(
        title: String,
        description: String,
        location: String?,
        budget: String?
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
                dateWishRepository.addDateWish(
                    title = title,
                    description = description,
                    location = location,
                    budget = budget?.toDoubleOrNull(),
                    createdBy = userId
                )
                loadWishes()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 