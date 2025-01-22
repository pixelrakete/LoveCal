package com.pixelrakete.lovecal.data.model

sealed class BudgetValidationResult {
    object Valid : BudgetValidationResult()
    data class Invalid(val message: String) : BudgetValidationResult()
}

object BudgetValidation {
    fun validateMonthlyBudget(amount: Double): BudgetValidationResult {
        return when {
            amount < 0 -> BudgetValidationResult.Invalid("Monthly budget cannot be negative")
            amount > 1_000_000 -> BudgetValidationResult.Invalid("Monthly budget cannot exceed 1,000,000")
            else -> BudgetValidationResult.Valid
        }
    }

    fun validateDateBudget(amount: Double, monthlyBudget: Double, remainingBudget: Double): BudgetValidationResult {
        return when {
            amount < 0 -> BudgetValidationResult.Invalid("Date budget cannot be negative")
            amount > monthlyBudget -> BudgetValidationResult.Invalid("Date budget cannot exceed monthly budget")
            amount > remainingBudget -> BudgetValidationResult.Invalid("Date budget exceeds remaining budget")
            else -> BudgetValidationResult.Valid
        }
    }
} 