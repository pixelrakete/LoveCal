package com.pixelrakete.lovecal.util

import android.util.Log
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.pixelrakete.lovecal.data.model.AppError
import java.io.IOException

object ErrorHandler {
    fun handleError(error: Throwable, tag: String): AppError {
        Log.e(tag, "Error occurred", error)
        return when (error) {
            is FirebaseAuthException -> AppError.AuthenticationError(
                message = error.message ?: "Authentication error occurred",
                cause = error
            )
            is FirebaseFirestoreException -> AppError.DatabaseError(
                message = error.message ?: "Database error occurred",
                cause = error
            )
            is IOException -> AppError.NetworkError(
                message = error.message ?: "Network error occurred",
                cause = error
            )
            else -> AppError.UnknownError(
                message = error.message ?: "An unexpected error occurred",
                cause = error
            )
        }
    }
} 