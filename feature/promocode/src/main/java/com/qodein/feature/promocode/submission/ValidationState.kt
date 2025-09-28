package com.qodein.feature.promocode.submission

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
 * Uses Int values representing StringRes for localization-ready error messages.
 */
data class ValidationState(val fieldErrors: Map<SubmissionField, Int> = emptyMap()) {
    val isValid get() = fieldErrors.isEmpty()

    companion object {
        fun valid() = ValidationState()
        fun invalid(errors: Map<SubmissionField, Int>) = ValidationState(errors)
    }
}
