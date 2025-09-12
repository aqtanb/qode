package com.qodein.core.data.repository

import com.qodein.core.data.datasource.FirestoreVoteDataSource
import com.qodein.shared.domain.repository.VoteRepository
import com.qodein.shared.model.UserId
import com.qodein.shared.model.Vote
import com.qodein.shared.model.VoteState
import com.qodein.shared.model.VoteType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoteRepositoryImpl @Inject constructor(private val voteDataSource: FirestoreVoteDataSource) : VoteRepository {

    override suspend fun voteOnContent(
        itemId: String,
        itemType: VoteType,
        userId: UserId,
        targetVoteState: VoteState
    ): Vote? = voteDataSource.voteOnContent(itemId, itemType, userId, targetVoteState)

    override fun getUserVote(
        itemId: String,
        itemType: VoteType,
        userId: UserId
    ): Flow<Vote?> = voteDataSource.getUserVote(itemId, itemType, userId)
}
