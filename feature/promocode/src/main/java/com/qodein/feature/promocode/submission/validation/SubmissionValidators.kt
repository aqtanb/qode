package com.qodein.feature.promocode.submission.validation

import com.qodein.feature.promocode.submission.PromocodeType
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.shared.common.Result
import com.qodein.shared.model.PromocodeCode

/**
 * Centralized validation utilities for submission wizard.
 * Ensures UI validation and step progression use the same rules.
 */

/**
 * Validates promo code format with same rules as domain validation.
 */
fun isValidPromoCodeFormat(code: String): Boolean = PromocodeCode.create(code) is Result.Success

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
 * Validates service URL (basic check for non-blank and a domain-like structure).
 */
fun sanitizeServiceUrl(raw: String): String {
    val trimmed = raw.trim()
    val withoutScheme = trimmed
        .removePrefix("https://")
        .removePrefix("http://")
        .removePrefix("www.")
    return withoutScheme
        .substringBefore('/')
        .substringBefore('?')
        .substringBefore('#')
        .trim()
}

fun isValidServiceUrl(url: String): Boolean {
    val value = sanitizeServiceUrl(url)
    return value.isNotBlank() && value.contains(".") && !value.contains(" ")
}

/**
 * Validates discount value based on promo code type.
 */
fun isValidDiscountValue(data: SubmissionWizardData): Boolean =
    when (data.promocodeType) {
        PromocodeType.PERCENTAGE -> isValidDiscountPercentage(data.discountPercentage)
        PromocodeType.FIXED_AMOUNT -> isValidDiscountAmount(data.discountAmount)
        null -> false
    }

/**
 * Validates business logic rules to prevent loss-making promo codes.
 */
fun isValidBusinessLogic(data: SubmissionWizardData): Boolean {
    // For fixed amount discounts, discount cannot exceed minimum order
    if (data.promocodeType == PromocodeType.FIXED_AMOUNT) {
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
    if (data.promocodeType == PromocodeType.FIXED_AMOUNT) {
        val discount = data.discountAmount.toDoubleOrNull() ?: 0.0
        val minOrder = data.minimumOrderAmount.toDoubleOrNull() ?: 0.0
        if (minOrder > 0 && discount > minOrder) {
            return "Discount amount (₸${discount.toInt()}) cannot exceed minimum order (₸${minOrder.toInt()})"
        }
    }
    return null
}
