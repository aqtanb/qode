package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.domain.repository.PromoCodeRepository
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.PromoCodeVote
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class VoteOnPromoCodeUseCase constructor(private val promoCodeRepository: PromoCodeRepository) {
    operator fun invoke(
        promoCodeId: PromoCodeId,
        userId: UserId,
        isUpvote: Boolean
    ): Flow<Result<PromoCodeVote>> =
        promoCodeRepository.voteOnPromoCode(promoCodeId, userId, isUpvote)
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}
