package com.qodein.shared.common.error

/**
 * Domain errors for PromoCode operations.
 * User-focused hierarchical errors that abstract away implementation details.
 */
sealed interface PromocodeError : OperationError {

    /**
     * Failures when user tries to create a promo code (client-side validation).
     */
    sealed interface CreationFailure : PromocodeError {
        data object EmptyCode : CreationFailure
        data object EmptyServiceName : CreationFailure
        data object InvalidDiscount : CreationFailure
        data object InvalidMinimumAmount : CreationFailure
        data object InvalidDateRange : CreationFailure
    }

    /**
     * Failures when submitting a promo code to the backend (server-side rejection).
     */
    sealed interface SubmissionFailure : PromocodeError {
        data object DuplicateCode : SubmissionFailure
        data object NotAuthorized : SubmissionFailure
        data object InvalidData : SubmissionFailure
    }

    /**
     * Failures when user tries to get/view promo codes.
     */
    sealed interface RetrievalFailure : PromocodeError {
        data object NotFound : RetrievalFailure
        data object NoResults : RetrievalFailure
    }
}
