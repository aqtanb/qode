package com.qodein.feature.promocode.submission

/**
 * Clean submission operation state without UI concerns.
 *
 * Represents the lifecycle of promo code submission operation.
 * UI behavior should be derived in presentation layer.
 */
sealed interface PromocodeSubmissionState {
    data object Idle : PromocodeSubmissionState
    data object Submitting : PromocodeSubmissionState
    data class Success(val promoCodeId: String) : PromocodeSubmissionState
    data class Error(val throwable: Throwable) : PromocodeSubmissionState
}
