package com.pixelrakete.lovecal.data.model

import java.util.Date

data class Couple(
    val id: String = "",
    val partner1Id: String = "",
    val partner1Name: String = "",
    val partner1Color: String = "",
    val partner1Interests: List<String> = emptyList(),
    val partner2Id: String = "",
    val partner2Name: String = "",
    val partner2Color: String = "",
    val partner2Interests: List<String> = emptyList(),
    val monthlyBudget: Double = 500.0,
    val dateFrequencyWeeks: Int = 2,
    val members: List<String> = emptyList(),
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val invitationCode: String = "",
    val city: String = ""
) 