package com.qodein.shared.domain.usecase.interaction

import com.qodein.shared.common.Result
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
        userId: UserId,
        currentVoteState: VoteState,
        isBookmarked: Boolean,
        targetVoteState: VoteState
    ): Result<UserInteraction, OperationError> {
        val currentInteraction = UserInteraction(
            itemId = itemId,
            itemType = itemType,
            userId = userId,
            voteState = currentVoteState,
            isBookmarked = isBookmarked,
        )

        val updatedInteraction = when (targetVoteState) {
            VoteState.UPVOTE -> currentInteraction.toggleUpvote()
            VoteState.DOWNVOTE -> currentInteraction.toggleDownvote()
            VoteState.NONE -> currentInteraction.copy(voteState = VoteState.NONE)
        }

        return repository.toggleVote(updatedInteraction)
    }
}
