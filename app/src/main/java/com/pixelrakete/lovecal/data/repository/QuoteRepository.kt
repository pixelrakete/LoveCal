package com.pixelrakete.lovecal.data.repository

import com.pixelrakete.lovecal.data.model.Quote

interface QuoteRepository {
    suspend fun getRandomQuote(): Quote?
    suspend fun saveQuote(quote: Quote): Boolean
    suspend fun getQuoteById(id: String): Quote?
    suspend fun getAllQuotes(): List<Quote>
    suspend fun getQuoteOfTheDay(): Quote?
} 