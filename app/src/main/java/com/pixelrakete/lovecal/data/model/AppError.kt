package com.pixelrakete.lovecal.data.model

sealed class AppError(
    open val message: String,
    open val cause: Throwable? = null
) {
    data class NetworkError(
        override val message: String = "Network error occurred",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class AuthenticationError(
        override val message: String = "Authentication error occurred",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class DatabaseError(
        override val message: String = "Database error occurred",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class ValidationError(
        override val message: String,
        val errors: Map<String, String> = emptyMap(),
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class CalendarError(
        override val message: String = "Calendar error occurred",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class PermissionError(
        override val message: String = "Permission denied",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class UnknownError(
        override val message: String = "An unknown error occurred",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    fun getUserMessage(): String {
        return when (this) {
            is ValidationError -> {
                if (errors.isNotEmpty()) {
                    errors.values.joinToString("\n")
                } else {
                    message
                }
            }
            else -> message
        }
    }
} 