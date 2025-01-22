package com.pixelrakete.lovecal.data.services

import com.pixelrakete.lovecal.data.model.DateSuggestion
import com.pixelrakete.lovecal.data.services.ai.GeminiService
import com.pixelrakete.lovecal.data.repository.DateWishRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateSuggestionService @Inject constructor(
    private val geminiService: GeminiService,
    private val dateWishRepository: DateWishRepository
) {
    suspend fun generateDateSuggestions(): List<DateSuggestion> {
        val randomWish = try {
            dateWishRepository.getRandomWish()
        } catch (e: Exception) {
            null
        }
        
        return geminiService.generateDateSuggestions(randomWish)
    }
} 