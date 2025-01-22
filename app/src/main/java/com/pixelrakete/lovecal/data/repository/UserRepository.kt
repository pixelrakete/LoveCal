package com.pixelrakete.lovecal.data.repository

import com.pixelrakete.lovecal.data.model.UserInfo

interface UserRepository {
    suspend fun getCurrentUserInfo(): UserInfo
} 