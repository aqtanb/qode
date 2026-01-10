package com.qodein.feature.promocode.submission.validation

import com.qodein.feature.promocode.submission.PromocodeType
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.shared.common.Result
import com.qodein.shared.model.Discount
import com.qodein.shared.model.PromocodeCode

/**
 * Centralized validation utilities for submission wizard.
 * Ensures UI validation and step progression use the same rules.
 */

/**
 * Validates promocode format with same rules as domain validation.
 */
fun isValidPromoCodeFormat(code: String): Boolean = PromocodeCode.create(code) is Result.Success

/**
 * Validates minimum order amount (non-negative).
 */
fun isValidMinimumOrderAmount(amount: String): Boolean {
    val value = amount.toDoubleOrNull()
    return value != null && value >= 0
}

/**
 * Validates discount value using domain model validation.
 */
fun isValidDiscountValue(data: SubmissionWizardData): Boolean {
    val minimumOrder = data.minimumOrderAmount.toDoubleOrNull() ?: 0.0

    val discount = when (data.promocodeType) {
        PromocodeType.PERCENTAGE -> {
            val value = data.discountPercentage.toDoubleOrNull() ?: return false
            Discount.Percentage(value)
        }
        PromocodeType.FIXED_AMOUNT -> {
            val value = data.discountAmount.toDoubleOrNull() ?: return false
            Discount.FixedAmount(value)
        }
        PromocodeType.FREE_ITEM -> {
            Discount.FreeItem(data.freeItemDescription)
        }
        null -> return false
    }

    return discount.validate(minimumOrder) is Result.Success
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
