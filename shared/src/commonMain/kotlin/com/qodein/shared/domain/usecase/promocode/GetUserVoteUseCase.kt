package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.VoteRepository
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.PromoCodeVote
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow

class GetUserVoteUseCase constructor(private val voteRepository: VoteRepository) {
    operator fun invoke(
        promoCodeId: PromoCodeId,
        userId: UserId
    ): Flow<Result<PromoCodeVote?>> =
        voteRepository.getUserVote(promoCodeId, userId)
            .asResult()
}
