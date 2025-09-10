package com.qodein.core.data.repository

import com.qodein.core.data.datasource.FirestoreVoteDataSource
import com.qodein.shared.domain.repository.VoteRepository
import com.qodein.shared.domain.repository.VoteType
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.PromoCodeVote
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoteRepositoryImpl @Inject constructor(private val voteDataSource: FirestoreVoteDataSource) : VoteRepository {

    override fun voteOnPromoCode(
        promoCodeId: PromoCodeId,
        userId: UserId,
        voteType: VoteType?
    ): Flow<PromoCodeVote?> =
        flow {
            emit(voteDataSource.voteOnPromoCode(promoCodeId, userId, voteType))
        }

    override fun getUserVote(
        promoCodeId: PromoCodeId,
        userId: UserId
    ): Flow<PromoCodeVote?> =
        flow {
            emit(voteDataSource.getUserVote(promoCodeId.value, userId))
        }
}
