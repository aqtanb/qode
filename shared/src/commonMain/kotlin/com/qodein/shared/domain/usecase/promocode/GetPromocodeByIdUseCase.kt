package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import kotlinx.coroutines.flow.Flow

class GetPromocodeByIdUseCase(private val promoCodeRepository: PromocodeRepository) {
    operator fun invoke(id: PromoCodeId): Flow<Result<PromoCode?, OperationError>> = promoCodeRepository.getPromoCodeById(id)
}
