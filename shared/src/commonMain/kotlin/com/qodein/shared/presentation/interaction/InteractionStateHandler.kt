package com.qodein.shared.presentation.interaction

import com.qodein.shared.model.ContentType
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
import com.qodein.shared.model.VoteState

/**
 * Result of vote computation - new counts and vote state.
 * ViewModel applies these to their content type (Promocode, Post, Comment).
 */
data class VoteUpdate(val newUpvotes: Int, val newDownvotes: Int, val newVoteState: VoteState)

/**
 * Handles optimistic UI state computation for user interactions (votes, bookmarks).
 * Pure functions - no side effects, easy to test, fully generic.
 *
 * Singleton object shared across all ViewModels - no state, just pure computation.
 */
object InteractionStateHandler {

    /**
     * Compute vote changes for any content type (Promocode, Post, Comment).
     * Returns new vote counts and vote state.
     *
     * @param currentUpvotes Current upvote count on content
     * @param currentDownvotes Current downvote count on content
     * @param currentVoteState User's current vote state (from UserInteraction or NONE)
     * @param targetVoteState Vote state user is trying to set (UPVOTE or DOWNVOTE)
     * @return New upvotes, downvotes, and vote state
     */
    fun computeVoteUpdate(
        currentUpvotes: Int,
        currentDownvotes: Int,
        currentVoteState: VoteState,
        targetVoteState: VoteState
    ): VoteUpdate {
        val isCurrentlyVoted = currentVoteState == targetVoteState

        // Compute deltas based on state transition
        val (upvoteDelta, downvoteDelta) = when {
            // Removing existing vote
            isCurrentlyVoted -> when (targetVoteState) {
                VoteState.UPVOTE -> Pair(-1, 0)
                VoteState.DOWNVOTE -> Pair(0, -1)
                VoteState.NONE -> Pair(0, 0)
            }
            // Adding vote from NONE
            currentVoteState == VoteState.NONE -> when (targetVoteState) {
                VoteState.UPVOTE -> Pair(1, 0)
                VoteState.DOWNVOTE -> Pair(0, 1)
                VoteState.NONE -> Pair(0, 0)
            }
            // Switching vote (UPVOTE <-> DOWNVOTE)
            else -> when (targetVoteState) {
                VoteState.UPVOTE -> Pair(1, -1) // Was downvote, now upvote
                VoteState.DOWNVOTE -> Pair(-1, 1) // Was upvote, now downvote
                VoteState.NONE -> Pair(0, 0)
            }
        }

        val newVoteState = if (isCurrentlyVoted) VoteState.NONE else targetVoteState

        return VoteUpdate(
            newUpvotes = maxOf(0, currentUpvotes + upvoteDelta),
            newDownvotes = maxOf(0, currentDownvotes + downvoteDelta),
            newVoteState = newVoteState,
        )
    }

    /**
     * Create or update UserInteraction with new vote state.
     * Works for any content type.
     *
     * @param currentInteraction Existing interaction or null
     * @param newVoteState New vote state to set
     * @param contentId ID of content being voted on
     * @param contentType Type of content (PROMO_CODE, POST, COMMENT)
     * @param userId User performing vote
     * @return Updated or new UserInteraction
     */
    fun createOrUpdateVoteInteraction(
        currentInteraction: UserInteraction?,
        newVoteState: VoteState,
        contentId: String,
        contentType: ContentType,
        userId: UserId
    ): UserInteraction =
        currentInteraction?.copy(voteState = newVoteState)
            ?: UserInteraction.create(
                itemId = contentId,
                itemType = contentType,
                userId = userId,
                voteState = newVoteState,
            )

    /**
     * Create or update UserInteraction with toggled bookmark state.
     * Content itself unchanged - only UserInteraction updated.
     * Works for any content type (Promocode, Post, Comment).
     *
     * @param currentInteraction Existing interaction or null
     * @param contentId ID of content being bookmarked
     * @param contentType Type of content
     * @param userId User performing bookmark
     * @return Updated or new UserInteraction with toggled bookmark
     */
    fun createOrUpdateBookmarkInteraction(
        currentInteraction: UserInteraction?,
        contentId: String,
        contentType: ContentType,
        userId: UserId
    ): UserInteraction {
        val currentIsBookmarked = currentInteraction?.isBookmarked ?: false

        return currentInteraction?.copy(isBookmarked = !currentIsBookmarked)
            ?: UserInteraction.create(
                itemId = contentId,
                itemType = contentType,
                userId = userId,
                isBookmarked = true,
            )
    }
}
