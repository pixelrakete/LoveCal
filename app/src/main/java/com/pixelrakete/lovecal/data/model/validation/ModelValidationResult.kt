package com.pixelrakete.lovecal.data.model.validation

sealed class ModelValidationResult {
    object Valid : ModelValidationResult()
    data class Invalid(val errors: Map<String, String>) : ModelValidationResult()

    companion object {
        fun valid(): ModelValidationResult = Valid
        fun invalid(errors: Map<String, String>): ModelValidationResult = Invalid(errors)
        fun invalid(field: String, error: String): ModelValidationResult = Invalid(mapOf(field to error))
    }
}

interface ModelValidatable {
    fun validate(): ModelValidationResult
}

fun List<ModelValidationResult>.combine(): ModelValidationResult {
    val errors = filterIsInstance<ModelValidationResult.Invalid>()
        .flatMap { it.errors.entries }
        .associate { it.key to it.value }
    
    return if (errors.isEmpty()) {
        ModelValidationResult.valid()
    } else {
        ModelValidationResult.invalid(errors)
    }
} 