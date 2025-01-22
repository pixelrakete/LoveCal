package com.pixelrakete.lovecal.data.model

import com.google.firebase.firestore.PropertyName

data class Couple(
    val id: String = "",
    val partner1Id: String = "",
    val partner2Id: String? = null,
    val partner1Name: String = "",
    val partner2Name: String? = null,
    val partner1Color: String = "#FF4081",
    val partner2Color: String = "#2196F3",
    val partner1Interests: List<String> = emptyList(),
    val partner2Interests: List<String> = emptyList(),
    val monthlyBudget: Double? = null,
    val city: String? = null,
    val partner1CalendarId: String? = null,
    val partner2CalendarId: String? = null,
    @get:PropertyName("setupComplete")
    @set:PropertyName("setupComplete")
    var setupComplete: Boolean = false,
    val invitationCode: String? = null
) 