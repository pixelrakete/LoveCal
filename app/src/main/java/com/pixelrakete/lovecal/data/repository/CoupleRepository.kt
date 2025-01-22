package com.pixelrakete.lovecal.data.repository

import com.pixelrakete.lovecal.data.model.Couple
import kotlinx.coroutines.flow.Flow

interface CoupleRepository {
    suspend fun getCouple(coupleId: String): Couple?
    suspend fun updateCouple(couple: Couple)
    suspend fun createCouple(couple: Couple): String
    fun observeCouple(coupleId: String): Flow<Couple?>
} 