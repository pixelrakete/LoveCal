package com.pixelrakete.lovecal.data.model

sealed class DataState<out T> {
    object Loading : DataState<Nothing>()
    data class Success<T>(val data: T) : DataState<T>()
    data class Error(val error: AppError) : DataState<Nothing>()
}

sealed class QuoteState {
    object Loading : QuoteState()
    data class Success(
        val quotes: List<Quote> = emptyList(),
        val quoteOfTheDay: Quote? = null,
        val randomQuote: Quote? = null
    ) : QuoteState()
    data class Error(val error: AppError) : QuoteState()
} 