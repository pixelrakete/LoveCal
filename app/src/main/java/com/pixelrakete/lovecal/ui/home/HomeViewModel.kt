package com.pixelrakete.lovecal.ui.home

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelrakete.lovecal.data.manager.UserManager
import com.pixelrakete.lovecal.data.model.DatePlan
import com.pixelrakete.lovecal.data.repository.DatePlanRepository
import com.pixelrakete.lovecal.data.repository.BudgetRepository
import com.pixelrakete.lovecal.data.services.QuoteService
import com.pixelrakete.lovecal.data.model.Quote
import com.pixelrakete.lovecal.data.services.ai.GeminiQuoteService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeUiState(
    val datePlans: List<DatePlan> = emptyList(),
    val monthlyBudget: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val quote: String? = null,
    val quoteAuthor: String? = null,
    val plannerColor: String = "#2196F3",
    val isPlanner: Boolean = false,
    val invitationCode: String? = null,
    val partner2Id: String? = null,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userManager: UserManager,
    private val datePlanRepository: DatePlanRepository,
    private val budgetRepository: BudgetRepository,
    private val quoteService: QuoteService
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
        private val dateFormatter = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm")
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun shareInvitationCode(context: Context, code: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Join me on LoveCal! Use this code to connect: $code")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share invitation code"))
    }

    fun updateDatePlanCompleted(id: String, completed: Boolean) {
        viewModelScope.launch {
            try {
                datePlanRepository.updateDatePlanCompleted(id, completed)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating date plan completion status", e)
                _uiState.value = _uiState.value.copy(error = "Failed to update date plan status")
            }
        }
    }

    fun deleteDatePlan(id: String) {
        viewModelScope.launch {
            try {
                datePlanRepository.deleteDatePlan(id)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting date plan", e)
                _uiState.value = _uiState.value.copy(error = "Failed to delete date plan")
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val coupleId = userManager.getCoupleId() ?: return@launch
                
                // Load initial budget values
                val monthlyBudget = budgetRepository.getMonthlyBudget()
                val remainingBudget = budgetRepository.getRemainingBudget()
                
                Log.d(TAG, "Initial budget values - monthly: $monthlyBudget, remaining: $remainingBudget")
                
                // Update UI state with initial budget values
                _uiState.value = _uiState.value.copy(
                    monthlyBudget = monthlyBudget,
                    remainingBudget = remainingBudget
                )
                
                // Combine couple settings and date plans flows
                userManager.getCoupleFlow(coupleId).combine(datePlanRepository.getDatePlans()) { couple, plans ->
                    // Update budget values whenever couple settings change
                    val updatedMonthlyBudget = couple?.monthlyBudget ?: 0.0
                    val updatedRemainingBudget = budgetRepository.getRemainingBudget()
                    
                    Log.d(TAG, "Updating budget - monthly: $updatedMonthlyBudget, remaining: $updatedRemainingBudget")
                    
                    // Sort plans by date
                    val sortedPlans = plans
                        .filter { !it.completed }
                        .sortedBy { plan ->
                            plan.dateTimeStr?.let { dateStr ->
                                try {
                                    LocalDateTime.parse(dateStr, dateFormatter)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                        }
                    
                    // Get planner color based on whether user is partner1
                    val userId = userManager.getCurrentUserId() ?: return@combine
                    val isPartner1 = couple?.partner1Id == userId
                    val plannerColor = if (isPartner1) couple?.partner1Color else couple?.partner2Color
                    
                    // Update invitation code visibility
                    val showInvitation = couple?.partner2Id.isNullOrEmpty()
                    val invitationCode = if (showInvitation) couple?.invitationCode else null
                    
                    Log.d(TAG, "Invitation state - showInvitation: $showInvitation, code: $invitationCode, partner2Id: ${couple?.partner2Id}")
                    
                    _uiState.value = _uiState.value.copy(
                        monthlyBudget = updatedMonthlyBudget,
                        remainingBudget = updatedRemainingBudget,
                        datePlans = sortedPlans,
                        plannerColor = plannerColor ?: "#2196F3",
                        isPlanner = true,
                        invitationCode = invitationCode,
                        partner2Id = couple?.partner2Id
                    )
                }.collect()
                
                // Load quote using QuoteService
                try {
                    val quote = quoteService.getRandomQuote()
                    if (quote != null) {
                        _uiState.value = _uiState.value.copy(
                            quote = quote.text,
                            quoteAuthor = quote.author
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading quote", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading data", e)
                _uiState.value = _uiState.value.copy(error = "Failed to load data")
            }
        }
    }
} 