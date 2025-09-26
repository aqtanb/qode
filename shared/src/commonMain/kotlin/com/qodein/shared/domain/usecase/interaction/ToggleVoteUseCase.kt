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
    /**
     * Toggle upvote on content.
     * If user already upvoted, removes vote. If no vote or downvoted, sets upvote.
     */
    suspend fun toggleUpvote(
        itemId: String,
        itemType: ContentType,
        userId: UserId
    ): Result<UserInteraction, OperationError> = repository.toggleVote(itemId, itemType, userId, VoteState.UPVOTE)

    /**
     * Toggle downvote on content.
     * If user already downvoted, removes vote. If no vote or upvoted, sets downvote.
     */
    suspend fun toggleDownvote(
        itemId: String,
        itemType: ContentType,
        userId: UserId
    ): Result<UserInteraction, OperationError> = repository.toggleVote(itemId, itemType, userId, VoteState.DOWNVOTE)

    /**
     * Remove vote on content.
     */
    suspend fun removeVote(
        itemId: String,
        itemType: ContentType,
        userId: UserId
    ): Result<UserInteraction, OperationError> = repository.toggleVote(itemId, itemType, userId, VoteState.NONE)
}
