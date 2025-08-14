package com.qodein.core.domain.usecase.promocode

import com.qodein.core.domain.repository.PromoCodeRepository
import com.qodein.core.model.PromoCodeId
import com.qodein.core.model.PromoCodeVote
import com.qodein.core.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoteOnPromoCodeUseCase @Inject constructor(private val promoCodeRepository: PromoCodeRepository) {
    operator fun invoke(
        promoCodeId: PromoCodeId,
        userId: UserId,
        isUpvote: Boolean
    ): Flow<Result<PromoCodeVote>> =
        promoCodeRepository.voteOnPromoCode(promoCodeId, userId, isUpvote)
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}
