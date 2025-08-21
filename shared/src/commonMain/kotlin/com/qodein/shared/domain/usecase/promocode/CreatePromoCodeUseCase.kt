package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.PromoCodeRepository
import com.qodein.shared.model.PromoCode
import kotlinx.coroutines.flow.Flow

class CreatePromoCodeUseCase constructor(private val promoCodeRepository: PromoCodeRepository) {
    operator fun invoke(promoCode: PromoCode): Flow<Result<PromoCode>> =
        promoCodeRepository.createPromoCode(promoCode)
            .asResult()
}
