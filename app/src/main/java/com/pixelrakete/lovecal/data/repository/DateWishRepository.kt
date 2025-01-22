package com.pixelrakete.lovecal.data.repository

import com.pixelrakete.lovecal.data.model.DateWish
import kotlinx.coroutines.flow.Flow

interface DateWishRepository {
    suspend fun getRandomWish(): DateWish?
    suspend fun saveDateWish(title: String, description: String)
    suspend fun deleteDateWish(wishId: String)
    fun getDateWishes(): Flow<List<DateWish>>
}