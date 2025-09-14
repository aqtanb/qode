package com.qodein.shared.domain.repository

import com.qodein.shared.model.UserId
import com.qodein.shared.model.Vote
import com.qodein.shared.model.VoteState
import com.qodein.shared.model.VoteType
import kotlinx.coroutines.flow.Flow

interface VoteRepository {
    /**
     * Vote on any content type with the specified target vote state.
     * Handles all 3-state transitions: NONE ↔ UPVOTE ↔ DOWNVOTE
     * Returns the updated vote or null if removed.
     */
    suspend fun voteOnContent(
        itemId: String,
        itemType: VoteType,
        userId: UserId,
        targetVoteState: VoteState
    ): Vote?

    /**
     * Get the current user's vote on any content type with real-time updates.
     * Returns null if user has not voted (VoteState.NONE).
     */
    fun getUserVote(
        itemId: String,
        userId: UserId
    ): Flow<Vote?>
}
