package com.qodein.shared.common.error

/**
 * Domain errors for PromoCode operations.
 * Only contains business logic errors. Infrastructure errors (NotFound, PermissionDenied, etc.)
 * should use FirestoreError or SystemError directly.
 */
sealed interface PromocodeError : OperationError {

    /**
     * Failures when user tries to create a promo code (domain validation).
     */
    sealed interface CreationFailure : PromocodeError {
        data object EmptyCode : CreationFailure
        data object CodeTooShort : CreationFailure
        data object CodeTooLong : CreationFailure
        data object InvalidPercentageDiscount : CreationFailure
        data object InvalidFixedAmountDiscount : CreationFailure
        data object DiscountExceedsMinimumAmount : CreationFailure
        data object InvalidMinimumAmount : CreationFailure
        data object DescriptionTooLong : CreationFailure
        data object InvalidDateRange : CreationFailure
        data object InvalidPromocodeId : CreationFailure
    }

    /**
     * Failures when submitting a promo code (business logic errors).
     */
    sealed interface SubmissionFailure : PromocodeError {
        data object DuplicateCode : SubmissionFailure
    }
}
