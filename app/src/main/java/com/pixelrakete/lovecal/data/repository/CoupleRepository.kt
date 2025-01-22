package com.pixelrakete.lovecal.data.repository

import com.pixelrakete.lovecal.data.model.Couple
import kotlinx.coroutines.flow.Flow

interface CoupleRepository {
    suspend fun getCurrentCouple(): Couple?
    fun observeCurrentCouple(): Flow<Couple?>
    suspend fun createCouple(
        partner1Name: String,
        partner1Color: String,
        monthlyBudget: Double,
        city: String
    ): Couple
    suspend fun updateCouple(couple: Couple)
    suspend fun updateCouple(
        partner1Name: String,
        partner2Name: String,
        partner1Color: String,
        partner2Color: String,
        partner1Interests: List<String>,
        partner2Interests: List<String>,
        monthlyBudget: Double,
        city: String
    )
    suspend fun acceptInvitation(invitationCode: String)
    
    // New methods for improved joining process
    suspend fun validateInvitationCode(invitationCode: String): Boolean
    
    suspend fun completePartner2Setup(
        invitationCode: String,
        partner2Name: String,
        partner2Color: String,
        partner2Interests: List<String>
    )
    
    suspend fun setupCalendar(
        calendarId: String,
        isPartner1: Boolean
    )
    
    suspend fun markSetupComplete()
    
    suspend fun storeTemporaryCalendarId(calendarId: String)
    
    suspend fun getTemporaryCalendarId(): String?
} 