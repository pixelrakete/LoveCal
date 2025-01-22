package com.pixelrakete.lovecal.data.model

data class Quote(
    val id: String = "",
    val text: String = "",
    val author: String = "Unknown",
    val language: String = "de",
    val tags: List<String> = emptyList()
) 