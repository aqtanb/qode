package com.qodein.feature.promocode.submission.validation

import com.qodein.feature.promocode.submission.PromoCodeType
import com.qodein.feature.promocode.submission.SubmissionWizardData

/**
 * Centralized validation utilities for submission wizard.
 * Ensures UI validation and step progression use the same rules.
 */

/**
 * Validates promo code format with same rules as domain validation.
 */
fun isValidPromoCodeFormat(code: String): Boolean {
    // Clean promo code (remove spaces, hyphens for validation)
    val cleanCode = code.replace("[\\s-]".toRegex(), "")

    return cleanCode.length >= 2 &&
        cleanCode.length <= 50 &&
        cleanCode.matches("[A-Za-z0-9]+".toRegex())
}

/**
 * Validates discount percentage (1-100%).
 */
fun isValidDiscountPercentage(percentage: String): Boolean {
    val value = percentage.toDoubleOrNull()
    return value != null && value > 0 && value <= 100
}

/**
 * Validates discount amount (positive number).
 */
fun isValidDiscountAmount(amount: String): Boolean {
    val value = amount.toDoubleOrNull()
    return value != null && value > 0
}

/**
 * Validates minimum order amount (non-negative).
 */
fun isValidMinimumOrderAmount(amount: String): Boolean {
    val value = amount.toDoubleOrNull()
    return value != null && value >= 0
}

/**
 * Validates service name (not blank).
 */
fun isValidServiceName(name: String): Boolean = name.trim().isNotBlank()

/**
 * Validates discount value based on promo code type.
 */
fun isValidDiscountValue(data: SubmissionWizardData): Boolean =
    when (data.promoCodeType) {
        PromoCodeType.PERCENTAGE -> isValidDiscountPercentage(data.discountPercentage)
        PromoCodeType.FIXED_AMOUNT -> isValidDiscountAmount(data.discountAmount)
        null -> false
    }

/**
 * Gets validation error message for promo code.
 */
fun getPromoCodeValidationError(code: String): String? {
    if (code.isBlank()) return "Promo code is required"

    val cleanCode = code.replace("[\\s-]".toRegex(), "")
    return when {
        cleanCode.length < 2 -> "Promo code is too short"
        cleanCode.length > 50 -> "Promo code is too long"
        !cleanCode.matches("[A-Za-z0-9]+".toRegex()) -> "Use only letters and numbers"
        else -> null
    }
}

/**
 * Gets validation error message for discount percentage.
 */
fun getDiscountPercentageValidationError(percentage: String): String? {
    if (percentage.isBlank()) return "Discount percentage is required"

    val value = percentage.toDoubleOrNull()
    return when {
        value == null -> "Enter a valid percentage"
        value <= 0 -> "Discount percentage must be positive"
        value > 100 -> "Discount percentage cannot exceed 100%"
        else -> null
    }
}

/**
 * Gets validation error message for discount amount.
 */
fun getDiscountAmountValidationError(amount: String): String? {
    if (amount.isBlank()) return "Discount amount is required"

    val value = amount.toDoubleOrNull()
    return when {
        value == null -> "Enter a valid amount"
        value <= 0 -> "Discount amount must be positive"
        else -> null
    }
}

/**
 * Gets validation error message for minimum order amount.
 */
fun getMinimumOrderAmountValidationError(amount: String): String? {
    if (amount.isBlank()) return "Minimum order amount is required"

    val value = amount.toDoubleOrNull()
    return when {
        value == null -> "Enter a valid amount"
        value < 0 -> "Minimum order amount cannot be negative"
        else -> null
    }
}

/**
 * Validates business logic rules to prevent loss-making promo codes.
 */
fun isValidBusinessLogic(data: SubmissionWizardData): Boolean {
    // For fixed amount discounts, discount cannot exceed minimum order
    if (data.promoCodeType == PromoCodeType.FIXED_AMOUNT) {
        val discount = data.discountAmount.toDoubleOrNull() ?: 0.0
        val minOrder = data.minimumOrderAmount.toDoubleOrNull() ?: 0.0
        if (minOrder > 0) { // Only validate if minimum order is set
            return discount <= minOrder
        }
    }
    return true
}

/**
 * Gets business logic validation error message.
 */
fun getBusinessLogicValidationError(data: SubmissionWizardData): String? {
    if (data.promoCodeType == PromoCodeType.FIXED_AMOUNT) {
        val discount = data.discountAmount.toDoubleOrNull() ?: 0.0
        val minOrder = data.minimumOrderAmount.toDoubleOrNull() ?: 0.0
        if (minOrder > 0 && discount > minOrder) {
            return "Discount amount (₸${discount.toInt()}) cannot exceed minimum order (₸${minOrder.toInt()})"
        }
    }
    return null
}
