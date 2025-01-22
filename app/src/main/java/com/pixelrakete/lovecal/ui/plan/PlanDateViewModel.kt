package com.pixelrakete.lovecal.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pixelrakete.lovecal.data.repository.BudgetRepository
import com.pixelrakete.lovecal.data.repository.CoupleRepository
import com.pixelrakete.lovecal.data.repository.DatePlanRepository
import com.pixelrakete.lovecal.data.repository.DateWishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class PlanDateUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val startDateTime: LocalDateTime = LocalDateTime.now(),
    val budget: String = "0",
    val isSurprise: Boolean = false,
    val monthlyBudget: Double = 500.0
)

@HiltViewModel
class PlanDateViewModel @Inject constructor(
    private val datePlanRepository: DatePlanRepository,
    private val dateWishRepository: DateWishRepository,
    private val coupleRepository: CoupleRepository,
    private val budgetRepository: BudgetRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanDateUiState())
    val uiState: StateFlow<PlanDateUiState> = _uiState.asStateFlow()

    init {
        loadMonthlyBudget()
    }

    private fun loadMonthlyBudget() {
        viewModelScope.launch {
            try {
                val couple = coupleRepository.getCurrentCouple()
                if (couple != null) {
                    _uiState.update { it.copy(monthlyBudget = couple.monthlyBudget ?: 500.0) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateLocation(location: String) {
        _uiState.update { it.copy(location = location) }
    }

    fun updateStartDateTime(dateTime: LocalDateTime) {
        _uiState.update { it.copy(startDateTime = dateTime) }
    }

    fun updateBudget(budget: String) {
        _uiState.update { it.copy(budget = budget) }
    }

    fun updateIsSurprise(isSurprise: Boolean) {
        _uiState.update { it.copy(isSurprise = isSurprise) }
    }

    fun getRandomWish() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val couple = coupleRepository.getCurrentCouple() ?: throw IllegalStateException("No couple found")
                val currentUserId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
                
                // Get partner's ID
                val partnerId = if (currentUserId == couple.partner1Id) couple.partner2Id else couple.partner1Id
                
                // Get random wish from partner
                val partnerWishes = dateWishRepository.getDateWishes(partnerId ?: "")
                val randomWish = partnerWishes.randomOrNull()
                
                if (randomWish != null) {
                    _uiState.update { it.copy(
                        title = randomWish.title,
                        description = randomWish.description,
                        location = randomWish.location ?: "",
                        budget = randomWish.budget?.toString() ?: "0"
                    ) }
                } else {
                    _uiState.update { it.copy(error = "No wishes found from your partner") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun saveDatePlan() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                datePlanRepository.createDatePlan(
                    title = uiState.value.title,
                    description = uiState.value.description,
                    location = uiState.value.location,
                    startDateTime = uiState.value.startDateTime,
                    budget = uiState.value.budget.toDoubleOrNull() ?: 0.0,
                    isSurprise = uiState.value.isSurprise
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

    fun loadDatePlan(dateId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val datePlan = datePlanRepository.getDatePlan(dateId)
                if (datePlan != null) {
                    _uiState.update { it.copy(
                        title = datePlan.title,
                        description = datePlan.description,
                        location = datePlan.location,
                        startDateTime = datePlan.startDateTime,
                        budget = datePlan.budget.toString(),
                        isSurprise = datePlan.isSurprise
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 