package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.domain.repository.PromoCodeRepository
import com.qodein.shared.model.PromoCodeId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class IncrementViewCountUseCase constructor(private val promoCodeRepository: PromoCodeRepository) {
    operator fun invoke(id: PromoCodeId): Flow<Result<Unit>> =
        promoCodeRepository.incrementViewCount(id)
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}
