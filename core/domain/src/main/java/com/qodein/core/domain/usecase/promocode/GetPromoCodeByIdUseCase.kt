package com.qodein.core.domain.usecase.promocode

import com.qodein.core.domain.repository.PromoCodeRepository
import com.qodein.core.model.PromoCode
import com.qodein.core.model.PromoCodeId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetPromoCodeByIdUseCase @Inject constructor(private val promoCodeRepository: PromoCodeRepository) {
    operator fun invoke(id: PromoCodeId): Flow<Result<PromoCode?>> =
        promoCodeRepository.getPromoCodeById(id)
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}
