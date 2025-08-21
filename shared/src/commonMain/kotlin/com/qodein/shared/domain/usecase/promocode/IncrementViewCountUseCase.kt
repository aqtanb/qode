package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.PromoCodeRepository
import com.qodein.shared.model.PromoCodeId
import kotlinx.coroutines.flow.Flow

class IncrementViewCountUseCase constructor(private val promoCodeRepository: PromoCodeRepository) {
    operator fun invoke(id: PromoCodeId): Flow<Result<Unit>> =
        promoCodeRepository.incrementViewCount(id)
            .asResult()
}
