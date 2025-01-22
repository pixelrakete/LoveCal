package com.pixelrakete.lovecal.util

/**
 * Safely executes a block of code that might throw a NullPointerException
 * @param defaultValue The value to return if the block throws an exception
 * @param block The code block to execute
 */
inline fun <T> nullSafe(defaultValue: T, block: () -> T): T {
    return try {
        block()
    } catch (e: NullPointerException) {
        defaultValue
    }
}

/**
 * Safely executes a block of code that might throw a NullPointerException
 * and returns null if an exception occurs
 * @param block The code block to execute
 */
inline fun <T> nullSafeOrNull(block: () -> T): T? {
    return try {
        block()
    } catch (e: NullPointerException) {
        null
    }
}

/**
 * Safely executes a block of code that might throw a NullPointerException
 * and returns the result wrapped in a Result
 * @param block The code block to execute
 */
inline fun <T> nullSafeResult(block: () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: NullPointerException) {
        Result.failure(e)
    }
}

/**
 * Extension function to safely get a value from a map with a default value
 */
fun <K, V> Map<K, V>.getOrDefault(key: K, defaultValue: V): V {
    return this[key] ?: defaultValue
}

/**
 * Extension function to safely convert a String to Double
 */
fun String?.toDoubleOrNull(): Double? {
    return this?.toDoubleOrNull()
}

/**
 * Extension function to safely convert a String to Double with default value
 */
fun String?.toDoubleOrDefault(defaultValue: Double): Double {
    return this?.toDoubleOrNull() ?: defaultValue
}

/**
 * Extension function to safely convert a String to Int
 */
fun String?.toIntOrNull(): Int? {
    return this?.toIntOrNull()
}

/**
 * Extension function to safely convert a String to Int with default value
 */
fun String?.toIntOrDefault(defaultValue: Int): Int {
    return this?.toIntOrNull() ?: defaultValue
}

/**
 * Extension function to safely get a non-null string or empty string
 */
fun String?.orEmpty(): String {
    return this ?: ""
}

/**
 * Extension function to safely check if a string is not null and not blank
 */
fun String?.isNotNullOrBlank(): Boolean {
    return this?.isNotBlank() ?: false
}

/**
 * Extension function to safely check if a collection is not null and not empty
 */
fun <T> Collection<T>?.isNotNullOrEmpty(): Boolean {
    return this?.isNotEmpty() ?: false
}

/**
 * Extension function to safely get a non-null list or empty list
 */
fun <T> List<T>?.orEmpty(): List<T> {
    return this ?: emptyList()
}

/**
 * Extension function to safely execute a block with a nullable receiver
 */
inline fun <T, R> T?.withNotNull(block: (T) -> R): R? {
    return this?.let(block)
}

/**
 * Extension function to safely execute a block with a nullable receiver
 * and return a default value if null
 */
inline fun <T, R> T?.withNotNullOrDefault(defaultValue: R, block: (T) -> R): R {
    return this?.let(block) ?: defaultValue
} 