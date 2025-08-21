package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.PromoCodeRepository
import com.qodein.shared.model.PromoCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ValidatePromoCodeUseCase constructor(private val promoCodeRepository: PromoCodeRepository) {
    operator fun invoke(
        code: String,
        orderAmount: Double
    ): Flow<Result<ValidationResult>> =
        promoCodeRepository.getPromoCodeByCode(code)
            .map { promoCode ->
                when {
                    promoCode == null -> ValidationResult.NotFound
                    !promoCode.isValidNow -> ValidationResult.Expired
                    orderAmount < (getMinimumOrderAmount(promoCode) ?: 0.0) -> {
                        ValidationResult.MinimumOrderNotMet(getMinimumOrderAmount(promoCode) ?: 0.0)
                    }
                    else -> {
                        val discountAmount = promoCode.calculateDiscount(orderAmount)
                        ValidationResult.Valid(promoCode, discountAmount)
                    }
                }
            }
            .asResult()

    private fun getMinimumOrderAmount(promoCode: PromoCode): Double? =
        when (promoCode) {
            is PromoCode.PercentagePromoCode -> promoCode.minimumOrderAmount
            is PromoCode.FixedAmountPromoCode -> promoCode.minimumOrderAmount
        }
}

sealed class ValidationResult {
    object NotFound : ValidationResult()
    object Expired : ValidationResult()
    object UsageLimitReached : ValidationResult()
    data class MinimumOrderNotMet(val requiredAmount: Double) : ValidationResult()
    data class Valid(val promoCode: PromoCode, val discountAmount: Double) : ValidationResult()
}
