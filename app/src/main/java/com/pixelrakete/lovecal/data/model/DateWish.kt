package com.pixelrakete.lovecal.data.model

data class DateWish(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
) 