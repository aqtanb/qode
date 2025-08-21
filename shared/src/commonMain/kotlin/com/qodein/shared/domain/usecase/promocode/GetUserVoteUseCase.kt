package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.PromoCodeRepository
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.PromoCodeVote
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow

class GetUserVoteUseCase constructor(private val promoCodeRepository: PromoCodeRepository) {
    operator fun invoke(
        promoCodeId: PromoCodeId,
        userId: UserId
    ): Flow<Result<PromoCodeVote?>> =
        promoCodeRepository.getUserVote(promoCodeId, userId)
            .asResult()
}
