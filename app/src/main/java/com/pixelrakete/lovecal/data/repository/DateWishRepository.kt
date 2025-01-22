package com.pixelrakete.lovecal.data.repository

import com.pixelrakete.lovecal.data.model.DateWish

interface DateWishRepository {
    suspend fun getDateWishes(userId: String): List<DateWish>
    suspend fun saveDateWish(wish: DateWish)
    suspend fun addDateWish(
        title: String,
        description: String,
        location: String?,
        budget: Double?,
        createdBy: String
    )
    suspend fun deleteDateWish(id: String)
} 