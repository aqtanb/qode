package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.VoteRepository
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.UserId
import com.qodein.shared.model.Vote
import kotlinx.coroutines.flow.Flow

class GetPromocodeVoteUseCase(private val voteRepository: VoteRepository) {
    operator fun invoke(
        promoCodeId: PromoCodeId,
        userId: UserId
    ): Flow<Result<Vote?>> =
        voteRepository.getUserVote(
            itemId = promoCodeId.value,
            userId = userId,
        ).asResult()
}
