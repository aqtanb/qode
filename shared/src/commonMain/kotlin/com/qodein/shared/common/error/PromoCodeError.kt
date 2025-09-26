package com.qodein.shared.common.error

/**
 * Domain errors for PromoCode operations.
 * User-focused hierarchical errors that abstract away implementation details.
 */
sealed interface PromoCodeError : OperationError {

    /**
     * Failures when user tries to create/submit a promo code.
     */
    sealed interface SubmissionFailure : PromoCodeError {
        data object DuplicateCode : SubmissionFailure
        data object NotAuthorized : SubmissionFailure
        data object InvalidData : SubmissionFailure
    }

    /**
     * Failures when user tries to get/view promo codes.
     */
    sealed interface RetrievalFailure : PromoCodeError {
        data object NotFound : RetrievalFailure
        data object NoResults : RetrievalFailure
        data object TooManyResults : RetrievalFailure
    }
}
