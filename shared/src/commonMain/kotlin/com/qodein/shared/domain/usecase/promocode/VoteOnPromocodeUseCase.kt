package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.resultOf
import com.qodein.shared.domain.repository.VoteRepository
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.UserId
import com.qodein.shared.model.Vote
import com.qodein.shared.model.VoteState
import com.qodein.shared.model.VoteType

class VoteOnPromocodeUseCase(private val voteRepository: VoteRepository) {
    suspend operator fun invoke(
        promoCodeId: PromoCodeId,
        userId: UserId,
        targetVoteState: VoteState
    ): Result<Vote?> =
        resultOf {
            voteRepository.voteOnContent(
                itemId = promoCodeId.value,
                itemType = VoteType.PROMO_CODE,
                userId = userId,
                targetVoteState = targetVoteState,
            )
        }
}
