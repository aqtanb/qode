package com.qodein.shared.domain.repository

import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.PromoCodeVote
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow

enum class ContentType {
    PROMO_CODE,
    POST,
    COMMENT,
    PROMO
}

enum class VoteType {
    UPVOTE,
    DOWNVOTE
}

interface VoteRepository {
    fun voteOnPromoCode(
        promoCodeId: PromoCodeId,
        userId: UserId,
        voteType: VoteType?
    ): Flow<PromoCodeVote?>

    fun getUserVote(
        promoCodeId: PromoCodeId,
        userId: UserId
    ): Flow<PromoCodeVote?>
}
