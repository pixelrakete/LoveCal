package com.pixelrakete.lovecal.data.model

data class CoupleSettings(
    val partner1Name: String = "",
    val partner1Color: String = "",
    val partner2Name: String = "",
    val partner2Color: String = "",
    val monthlyBudget: Double = 500.0,
    val dateFrequencyWeeks: Int = 2,
    val interests: List<String> = emptyList(),
    val city: String = "Berlin",
    val invitationCode: String = ""
) 