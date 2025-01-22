package com.pixelrakete.lovecal.data.model

data class CoupleSettings(
    val coupleId: String = "",
    val partner1Id: String = "",
    val partner2Id: String? = null,
    val partner1Name: String = "",
    val partner2Name: String = "",
    val partner1Color: Long = 0xFFC4146C,
    val partner2Color: Long = 0xFFCDA34F,
    val partner1Interests: List<String> = emptyList(),
    val partner2Interests: List<String> = emptyList(),
    val monthlyBudget: Double? = null
) 