package com.pixelrakete.lovecal.util

import android.util.Patterns
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

object ValidationUtils {
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 8 -> "Password must be at least 8 characters long"
            else -> null
        }
    }

    fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Name is required"
            name.length < 2 -> "Name must be at least 2 characters long"
            else -> null
        }
    }

    fun validateBudget(budget: Double?): String? {
        return when {
            budget == null -> "Budget is required"
            budget < 0 -> "Budget cannot be negative"
            else -> null
        }
    }

    fun validateDescription(description: String): String? {
        return when {
            description.isBlank() -> "Description is required"
            description.length < 10 -> "Description must be at least 10 characters long"
            else -> null
        }
    }

    fun validateTitle(title: String): String? {
        return when {
            title.isBlank() -> "Title is required"
            title.length < 3 -> "Title must be at least 3 characters long"
            else -> null
        }
    }

    fun validateInvitationCode(code: String): String? {
        return when {
            code.isBlank() -> "Invitation code is required"
            !code.matches(Regex("^[A-Z0-9]{6}$")) -> "Invalid invitation code format"
            else -> null
        }
    }

    fun validateDate(date: String): String? {
        return try {
            if (date.isBlank()) {
                "Date is required"
            } else {
                LocalDate.parse(date)
                null
            }
        } catch (e: DateTimeParseException) {
            "Invalid date format"
        }
    }

    fun validateTime(time: String): String? {
        return try {
            if (time.isBlank()) {
                "Time is required"
            } else {
                LocalTime.parse(time)
                null
            }
        } catch (e: DateTimeParseException) {
            "Invalid time format"
        }
    }

    fun validateLocation(location: String): String? {
        return when {
            location.isBlank() -> "Location is required"
            else -> null
        }
    }

    fun validateInterests(interests: List<String>): String? {
        return when {
            interests.isEmpty() -> "At least one interest is required"
            else -> null
        }
    }

    fun validateColor(color: String): String? {
        return when {
            color.isBlank() -> "Color is required"
            !color.matches(Regex("^#[0-9A-Fa-f]{6}$")) -> "Invalid color format"
            else -> null
        }
    }

    fun validateForm(validations: Map<String, String?>): Map<String, String> {
        return validations.filterValues { it != null }.mapValues { it.value!! }
    }

    fun generateInvitationCode(): String {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        return (1..6)
            .map { allowedChars.random() }
            .joinToString("")
    }
} 