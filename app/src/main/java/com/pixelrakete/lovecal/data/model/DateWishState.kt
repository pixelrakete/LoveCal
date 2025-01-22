package com.pixelrakete.lovecal.data.model

sealed class DateWishState {
    object Initial : DateWishState()
    object Loading : DateWishState()
    data class Success(val wishes: List<DateWish>) : DateWishState()
    data class Error(val error: AppError) : DateWishState()
} 