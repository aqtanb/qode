package com.qodein.shared.domain.usecase.interaction

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.InteractionError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.UnifiedUserInteractionRepository
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
import com.qodein.shared.model.VoteState

/**
 * Use case to toggle user's vote on content.
 * Handles the business logic for vote state transitions.
 */
class ToggleVoteUseCase(private val repository: UnifiedUserInteractionRepository) {
    suspend operator fun invoke(
        itemId: String,
        itemType: ContentType,
        userId: UserId?,
        currentVoteState: VoteState,
        targetVoteState: VoteState
    ): Result<UserInteraction, OperationError> {
        if (userId == null) {
            return Result.Error(InteractionError.VotingFailure.NotAuthorized)
        }

        val newVoteState = when (targetVoteState) {
            VoteState.UPVOTE ->
                if (currentVoteState == VoteState.UPVOTE) VoteState.NONE else VoteState.UPVOTE
            VoteState.DOWNVOTE ->
                if (currentVoteState == VoteState.DOWNVOTE) VoteState.NONE else VoteState.DOWNVOTE
            VoteState.NONE -> VoteState.NONE
        }

        return repository.toggleVote(
            itemId = itemId,
            itemType = itemType,
            userId = userId,
            newVoteState,
        )
    }
}
