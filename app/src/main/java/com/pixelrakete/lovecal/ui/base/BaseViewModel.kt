package com.pixelrakete.lovecal.ui.base

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.IOException

abstract class BaseViewModel : ViewModel() {
    protected fun handleError(e: Exception): String {
        return when (e) {
            is FirebaseAuthException -> "Authentication error: ${e.message}"
            is FirebaseFirestoreException -> "Database error: ${e.message}"
            is IOException -> "Network error: ${e.message}"
            else -> e.message ?: "An unknown error occurred"
        }
    }
} 