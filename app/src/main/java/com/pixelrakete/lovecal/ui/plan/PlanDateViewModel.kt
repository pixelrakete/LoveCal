package com.pixelrakete.lovecal.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelrakete.lovecal.data.model.DatePlan
import com.pixelrakete.lovecal.data.repository.DatePlanRepository
import com.pixelrakete.lovecal.data.services.ai.GeminiService
import com.pixelrakete.lovecal.data.model.DateSuggestion
import com.pixelrakete.lovecal.data.model.DateWish
import com.pixelrakete.lovecal.data.repository.DateWishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class PlanDateUiState(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val budget: Double = 0.0,
    val dateTimeStr: String = "",
    val isSurprise: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val suggestions: List<DateSuggestion> = emptyList(),
    val randomWish: DateWish? = null
)

@HiltViewModel
class PlanDateViewModel @Inject constructor(
    private val datePlanRepository: DatePlanRepository,
    private val aiService: GeminiService,
    private val dateWishRepository: DateWishRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanDateUiState(
        dateTimeStr = String.format(
            "%02d.%02d.%04d %02d:%02d",
            LocalDateTime.now().dayOfMonth,
            LocalDateTime.now().monthValue,
            LocalDateTime.now().year,
            LocalDateTime.now().hour,
            LocalDateTime.now().minute
        )
    ))
    val uiState: StateFlow<PlanDateUiState> = _uiState.asStateFlow()

    fun loadDatePlan(id: String?) {
        viewModelScope.launch {
            // For new date plans, just initialize an empty form
            if (id == null || id.isEmpty()) {
                initializeNewDatePlan()
                return@launch
            }
            
            // Only set loading state when actually loading an existing date plan
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Try to load from repository
            try {
                val datePlan = datePlanRepository.getDatePlan(id)
                if (datePlan != null) {
                    loadExistingDatePlan(datePlan)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Date plan not found"
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

    private fun initializeNewDatePlan() {
        val currentDateTime = LocalDateTime.now()
        _uiState.value = _uiState.value.copy(
            id = "",
            title = "",
            description = "",
            location = "",
            budget = 0.0,
            dateTimeStr = String.format(
                "%02d.%02d.%04d %02d:%02d",
                currentDateTime.dayOfMonth,
                currentDateTime.monthValue,
                currentDateTime.year,
                currentDateTime.hour,
                currentDateTime.minute
            ),
            isSurprise = false,
            isLoading = false,
            error = null,
            isSuccess = false,
            suggestions = emptyList(),
            randomWish = null
        )
    }

    private fun loadExistingDatePlan(datePlan: DatePlan) {
        _uiState.value = _uiState.value.copy(
            id = datePlan.id,
            title = datePlan.title,
            description = datePlan.description ?: "",
            location = datePlan.location ?: "",
            budget = datePlan.budget ?: 0.0,
            dateTimeStr = datePlan.dateTimeStr ?: String.format(
                "%02d.%02d.%04d %02d:%02d",
                LocalDateTime.now().dayOfMonth,
                LocalDateTime.now().monthValue,
                LocalDateTime.now().year,
                LocalDateTime.now().hour,
                LocalDateTime.now().minute
            ),
            isSurprise = datePlan.isSurprise,
            isLoading = false,
            error = null
        )
    }

    fun generateAiSuggestion() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val suggestions = aiService.generateDateSuggestions()
                _uiState.value = _uiState.value.copy(
                    suggestions = suggestions,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to get AI suggestions: ${e.message}",
                    suggestions = emptyList()
                )
            }
        }
    }

    fun applySuggestion(suggestion: DateSuggestion) {
        _uiState.value = _uiState.value.copy(
            title = suggestion.title,
            description = suggestion.description,
            location = suggestion.location,
            budget = suggestion.budget
        )
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateLocation(location: String) {
        _uiState.value = _uiState.value.copy(location = location)
    }

    fun updateBudget(budget: Double) {
        _uiState.value = _uiState.value.copy(budget = budget)
    }

    fun updateDateTime(dateTimeStr: String) {
        _uiState.value = _uiState.value.copy(dateTimeStr = dateTimeStr)
    }

    fun updateIsSurprise(isSurprise: Boolean) {
        _uiState.value = _uiState.value.copy(isSurprise = isSurprise)
    }

    fun getRandomDateWish() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val randomWish = dateWishRepository.getRandomWish()
                _uiState.value = _uiState.value.copy(
                    randomWish = randomWish,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No date wishes available yet. Add some in the Date Wishes section!"
                )
            }
        }
    }

    fun saveDatePlan() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val state = _uiState.value
                if (state.id.isEmpty()) {
                    datePlanRepository.createDatePlan(
                        title = state.title,
                        description = state.description.takeIf { it.isNotBlank() },
                        location = state.location.takeIf { it.isNotBlank() },
                        budget = state.budget.takeIf { it > 0 },
                        dateTimeStr = state.dateTimeStr,
                        isSurprise = state.isSurprise
                    )
                } else {
                    datePlanRepository.updateDatePlan(
                        id = state.id,
                        title = state.title,
                        description = state.description.takeIf { it.isNotBlank() },
                        location = state.location.takeIf { it.isNotBlank() },
                        budget = state.budget.takeIf { it > 0 },
                        dateTimeStr = state.dateTimeStr,
                        isSurprise = state.isSurprise
                    )
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    error = null
                )
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

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false)
    }

    fun resetState() {
        _uiState.value = PlanDateUiState(
            dateTimeStr = String.format(
                "%02d.%02d.%04d %02d:%02d",
                LocalDateTime.now().dayOfMonth,
                LocalDateTime.now().monthValue,
                LocalDateTime.now().year,
                LocalDateTime.now().hour,
                LocalDateTime.now().minute
            )
        )
    }
} 