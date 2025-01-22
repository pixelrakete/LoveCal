package com.pixelrakete.lovecal.data.model

sealed class CalendarError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    data class BatchOperationError(
        override val message: String,
        val failedItems: List<String>
    ) : CalendarError(message)

    data class PermissionError(
        override val message: String = "Calendar permission not granted",
        override val cause: Throwable? = null
    ) : CalendarError(message, cause)

    data class NetworkError(
        override val message: String = "Network error occurred",
        override val cause: Throwable? = null
    ) : CalendarError(message, cause)

    data class RateLimitError(
        override val message: String = "Rate limit exceeded",
        override val cause: Throwable? = null
    ) : CalendarError(message, cause)

    data class InvalidEventError(
        override val message: String,
        override val cause: Throwable? = null
    ) : CalendarError(message, cause)

    data class UnknownError(
        override val message: String = "Unknown calendar error occurred",
        override val cause: Throwable? = null
    ) : CalendarError(message, cause)
} 