package com.pixelrakete.lovecal.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pixelrakete.lovecal.data.model.DatePlan
import com.pixelrakete.lovecal.data.model.Quote
import com.pixelrakete.lovecal.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val partner1Color: String = "#FF4081",
    val partner2Color: String = "#2196F3",
    val invitationCode: String? = null,
    val monthlyBudget: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val currentUserId: String = "",
    val upcomingDates: List<DatePlan> = emptyList(),
    val quote: String = "",
    val nextDate: DatePlan? = null,
    val daysUntilNextDate: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val coupleRepository: CoupleRepository,
    private val datePlanRepository: DatePlanRepository,
    private val budgetRepository: BudgetRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Load couple data
                val couple = coupleRepository.getCurrentCouple()
                if (couple != null) {
                    _uiState.update { state ->
                        state.copy(
                            partner1Color = couple.partner1Color,
                            partner2Color = couple.partner2Color,
                            invitationCode = couple.invitationCode,
                            monthlyBudget = couple.monthlyBudget ?: 0.0,
                            currentUserId = auth.currentUser?.uid ?: ""
                        )
                    }
                }

                // Load budget data
                val remainingBudget = budgetRepository.getRemainingBudget()
                _uiState.update { it.copy(remainingBudget = remainingBudget) }

                // Load upcoming dates
                val dates = datePlanRepository.getDatePlans()
                val nextDate = dates.firstOrNull()
                val daysUntil = nextDate?.let {
                    ChronoUnit.DAYS.between(
                        LocalDateTime.now(),
                        it.startDateTime
                    ).toInt()
                } ?: 0

                _uiState.update { state ->
                    state.copy(
                        upcomingDates = dates,
                        nextDate = nextDate,
                        daysUntilNextDate = daysUntil,
                        quote = "Love is in the air!",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message
                ) }
            }
        }
    }

    fun deleteDatePlan(dateId: String) {
        viewModelScope.launch {
            try {
                datePlanRepository.deleteDatePlan(dateId)
                loadData() // Reload data after deletion
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
} 