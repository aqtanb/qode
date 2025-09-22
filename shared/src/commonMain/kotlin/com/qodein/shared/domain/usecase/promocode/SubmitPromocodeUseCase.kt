package com.qodein.shared.domain.usecase.promocode

import co.touchlab.kermit.Logger
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.PromoCodeRepository
import com.qodein.shared.model.PromoCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

/**
 * Use case for submitting promo codes with domain logic and analytics.
 *
 * Uses dependency injection to receive analytics interface, maintaining
 * clean architecture and KMP compatibility.
 */
class SubmitPromocodeUseCase(
    private val promoCodeRepository: PromoCodeRepository,
    private val analyticsHelper: AnalyticsHelper // Interface injection
) {

    companion object {
        private const val TAG = "SubmitPromocodeUseCase"
        private const val PROMO_CODE_TYPE_PERCENTAGE = "percentage"
        private const val PROMO_CODE_TYPE_FIXED_AMOUNT = "fixed_amount"
    }

    operator fun invoke(promoCode: PromoCode): Flow<Result<PromoCode>> {
        Logger.i(TAG) { "Creating promo code: ${promoCode.code} for service: ${promoCode.serviceName}" }
        Logger.d(TAG) { "PromoCode details: id=${promoCode.id.value}, type=${promoCode::class.simpleName}" }

        // Domain-level validation
        try {
            validatePromoCode(promoCode)
            Logger.d(TAG) { "Domain validation passed" }
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Domain validation failed for promo code: ${promoCode.code}" }
            throw IllegalArgumentException("Invalid promo code: ${e.message}", e)
        }

        return promoCodeRepository.createPromoCode(promoCode)
            .asResult()
            .onEach { result ->
                // Track business analytics
                when (result) {
                    is Result.Success -> trackSubmissionSuccess(result.data)
                    is Result.Error -> trackSubmissionFailure(promoCode)
                    else -> { /* Loading state */ }
                }
            }
    }

    private fun validatePromoCode(promoCode: PromoCode) {
        require(promoCode.code.isNotBlank()) { "Promo code cannot be blank" }
        require(promoCode.serviceName.isNotBlank()) { "Service name cannot be blank" }
        require(promoCode.endDate > promoCode.startDate) { "End date must be after start date" }

        when (promoCode) {
            is PromoCode.PercentagePromoCode -> {
                require(promoCode.discountPercentage > 0) { "Discount percentage must be positive" }
                require(promoCode.discountPercentage <= 100) { "Discount percentage cannot exceed 100%" }
            }
            is PromoCode.FixedAmountPromoCode -> {
                require(promoCode.discountAmount > 0) { "Discount amount must be positive" }
            }
        }

        require(promoCode.minimumOrderAmount >= 0) { "Minimum order amount cannot be negative" }
    }

    /**
     * Track successful promo code submission (business event)
     */
    private fun trackSubmissionSuccess(promoCode: PromoCode) {
        val type = when (promoCode) {
            is PromoCode.PercentagePromoCode -> PROMO_CODE_TYPE_PERCENTAGE
            is PromoCode.FixedAmountPromoCode -> PROMO_CODE_TYPE_FIXED_AMOUNT
        }

        Logger.d(TAG) { "Tracking successful submission for promo code: ${promoCode.id.value}" }

        // Use generic analytics interface method
        analyticsHelper.logEvent(
            name = "promo_code_submission",
            params = mapOf(
                "promocode_id" to promoCode.id.value,
                "promocode_type" to type,
                "success" to "true",
            ),
        )
    }

    /**
     * Track failed promo code submission (business event)
     */
    private fun trackSubmissionFailure(promoCode: PromoCode) {
        val type = when (promoCode) {
            is PromoCode.PercentagePromoCode -> PROMO_CODE_TYPE_PERCENTAGE
            is PromoCode.FixedAmountPromoCode -> PROMO_CODE_TYPE_FIXED_AMOUNT
        }

        Logger.d(TAG) { "Tracking failed submission for promo code: ${promoCode.id.value}" }

        // Use generic analytics interface method
        analyticsHelper.logEvent(
            name = "promo_code_submission",
            params = mapOf(
                "promocode_id" to promoCode.id.value,
                "promocode_type" to type,
                "success" to "false",
            ),
        )
    }
}
