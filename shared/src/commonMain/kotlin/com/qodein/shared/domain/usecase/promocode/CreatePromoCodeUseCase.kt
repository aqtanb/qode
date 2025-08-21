package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.domain.repository.PromoCodeRepository
import com.qodein.shared.model.PromoCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class CreatePromoCodeUseCase constructor(private val promoCodeRepository: PromoCodeRepository) {
    operator fun invoke(promoCode: PromoCode): Flow<Result<PromoCode>> =
        promoCodeRepository.createPromoCode(promoCode)
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}
