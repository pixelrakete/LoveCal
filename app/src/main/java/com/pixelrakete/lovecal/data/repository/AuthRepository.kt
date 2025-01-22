package com.pixelrakete.lovecal.data.repository

interface AuthRepository {
    suspend fun signInWithEmailAndPassword(email: String, password: String)
    suspend fun signOut()
    suspend fun isUserSignedIn(): Boolean
    suspend fun getCurrentUserId(): String?
} 