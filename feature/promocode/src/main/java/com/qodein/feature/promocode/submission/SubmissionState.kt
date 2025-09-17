package com.qodein.feature.promocode.submission

/**
 * Clean submission operation state without UI concerns.
 *
 * Represents the lifecycle of promo code submission operation.
 * UI behavior should be derived in presentation layer.
 */
sealed interface SubmissionState {
    data object Idle : SubmissionState
    data object Submitting : SubmissionState
    data class Success(val promoCodeId: String) : SubmissionState
    data class Error(val throwable: Throwable) : SubmissionState
}
