package com.qodein.shared.domain.usecase.promocode

import co.touchlab.kermit.Logger
import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.model.PromoCode
import kotlinx.coroutines.flow.Flow

/**
 * Use case for submitting promo codes with domain logic.
 *
 * Pure domain logic without presentation concerns.
 */
class SubmitPromocodeUseCase(private val promoCodeRepository: PromocodeRepository) {

    companion object {
        private const val TAG = "SubmitPromocodeUseCase"
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
}
