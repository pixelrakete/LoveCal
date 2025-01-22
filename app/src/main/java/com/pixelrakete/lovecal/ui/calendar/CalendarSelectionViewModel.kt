package com.pixelrakete.lovecal.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelrakete.lovecal.data.repository.CalendarListEntry
import com.pixelrakete.lovecal.data.repository.CalendarRepository
import com.pixelrakete.lovecal.data.manager.UserManager
import com.pixelrakete.lovecal.domain.usecase.calendar.GetCalendarsUseCase
import com.pixelrakete.lovecal.domain.usecase.calendar.GetSharedCalendarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalendarSelectionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val calendars: List<CalendarListEntry> = emptyList(),
    val selectedCalendarId: String? = null,
    val calendarPermissionGranted: Boolean = false,
    val showSharingConfirmation: Boolean = false,
    val selectedCalendar: CalendarListEntry? = null,
    val sharedCalendar: CalendarListEntry? = null,
    val isPartner2: Boolean = false
)

@HiltViewModel
class CalendarSelectionViewModel @Inject constructor(
    private val getCalendarsUseCase: GetCalendarsUseCase,
    private val getSharedCalendarUseCase: GetSharedCalendarUseCase,
    private val calendarRepository: CalendarRepository,
    private val userManager: UserManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarSelectionUiState())
    val uiState: StateFlow<CalendarSelectionUiState> = _uiState

    init {
        viewModelScope.launch {
            val isPartner2 = userManager.isPartner2()
            _uiState.update { it.copy(isPartner2 = isPartner2) }
            
            if (isPartner2) {
                loadSharedCalendar()
            }
        }
    }

    private suspend fun loadSharedCalendar() {
        try {
            val sharedCalendar = getSharedCalendarUseCase()
            _uiState.update { it.copy(sharedCalendar = sharedCalendar) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }

    fun loadCalendars() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val calendars = getCalendarsUseCase()
                _uiState.update { it.copy(
                    calendars = calendars,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = e.message,
                    isLoading = false
                ) }
            }
        }
    }

    fun initiateCalendarSelection(calendar: CalendarListEntry) {
        if (_uiState.value.isPartner2) {
            // For Partner2, directly select the shared calendar
            selectSharedCalendar()
        } else {
            // For Partner1, show sharing confirmation
            _uiState.update { it.copy(
                selectedCalendar = calendar,
                showSharingConfirmation = true
            ) }
        }
    }

    private fun selectSharedCalendar() {
        val sharedCalendar = _uiState.value.sharedCalendar ?: return
        viewModelScope.launch {
            try {
                userManager.setCalendarPermission(true)
                _uiState.update { it.copy(
                    selectedCalendarId = sharedCalendar.id,
                    calendarPermissionGranted = true
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun confirmCalendarSelection() {
        viewModelScope.launch {
            val calendar = _uiState.value.selectedCalendar ?: return@launch
            try {
                userManager.setCalendarPermission(true)
                calendarRepository.addCalendar(calendar)
                _uiState.update { it.copy(
                    selectedCalendarId = calendar.id,
                    calendarPermissionGranted = true,
                    showSharingConfirmation = false
                ) }
            } catch (e: Exception) {
                if (e.message?.contains("Calendar already shared") == true) {
                    // If calendar is already shared, just set permission and continue
                    userManager.setCalendarPermission(true)
                    _uiState.update { it.copy(
                        selectedCalendarId = calendar.id,
                        calendarPermissionGranted = true,
                        showSharingConfirmation = false
                    ) }
                } else {
                    _uiState.update { it.copy(
                        error = e.message,
                        showSharingConfirmation = false
                    ) }
                }
            }
        }
    }

    fun dismissSharingConfirmation() {
        _uiState.update { it.copy(
            showSharingConfirmation = false,
            selectedCalendar = null
        ) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun setError(message: String) {
        _uiState.update { it.copy(error = message) }
    }
} 