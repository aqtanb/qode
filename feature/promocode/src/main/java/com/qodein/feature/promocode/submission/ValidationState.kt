package com.qodein.feature.promocode.submission

import com.qodein.shared.common.error.PromocodeError

/**
 * Form validation field identifiers for type safety.
 */
enum class SubmissionField {
    SERVICE_NAME,
    PROMO_CODE_TYPE,
    PROMO_CODE,
    DISCOUNT_PERCENTAGE,
    DISCOUNT_AMOUNT,
    MINIMUM_ORDER_AMOUNT,
    START_DATE,
    END_DATE,
    DESCRIPTION
}

/**
 * Pure data carrier for form validation results.
 *
 * This class is the "inspection report" from validation use cases.
 * Contains no mutation logic - validation logic belongs in use cases.
 * Each field holds a domain-level creation failure; UI layers turn them into text.
 */
data class ValidationState(val fieldErrors: Map<SubmissionField, PromocodeError.CreationFailure> = emptyMap()) {
    val isValid get() = fieldErrors.isEmpty()

    fun withFieldError(
        field: SubmissionField,
        error: PromocodeError.CreationFailure?
    ): ValidationState {
        val updated = fieldErrors.toMutableMap()
        if (error == null) {
            updated.remove(field)
        } else {
            updated[field] = error
        }
        return copy(fieldErrors = updated)
    }

    companion object {
        fun valid() = ValidationState()
        fun invalid(errors: Map<SubmissionField, PromocodeError.CreationFailure>) = ValidationState(errors)
    }
}
