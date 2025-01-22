package com.pixelrakete.lovecal.data.model

data class UserData(
    val id: String,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val coupleId: String? = null,
    val isPartner1: Boolean = false,
    val invitationCode: String? = null
) 