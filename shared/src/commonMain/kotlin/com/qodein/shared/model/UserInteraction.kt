package com.qodein.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class VoteState {
    UPVOTE,
    DOWNVOTE,
    NONE;

    fun toggleTo(target: VoteState): VoteState =
        when (target) {
            UPVOTE -> if (this == UPVOTE) NONE else UPVOTE
            DOWNVOTE -> if (this == DOWNVOTE) NONE else DOWNVOTE
            NONE -> NONE
        }

    companion object {
        fun computeVoteCounts(
            previousVote: VoteState,
            newVote: VoteState,
            currentUpvotes: Int,
            currentDownvotes: Int
        ): Pair<Int, Int> =
            when (previousVote to newVote) {
                NONE to UPVOTE -> (currentUpvotes + 1) to currentDownvotes
                NONE to DOWNVOTE -> currentUpvotes to (currentDownvotes + 1)
                UPVOTE to NONE -> (currentUpvotes - 1).coerceAtLeast(0) to currentDownvotes
                DOWNVOTE to NONE -> currentUpvotes to (currentDownvotes - 1).coerceAtLeast(0)
                UPVOTE to DOWNVOTE -> (currentUpvotes - 1).coerceAtLeast(0) to (currentDownvotes + 1)
                DOWNVOTE to UPVOTE -> (currentUpvotes + 1) to (currentDownvotes - 1).coerceAtLeast(0)
                else -> currentUpvotes to currentDownvotes
            }

        fun computeScoreDelta(
            from: VoteState,
            to: VoteState
        ): Int =
            when (from to to) {
                NONE to UPVOTE -> 1
                NONE to DOWNVOTE -> -1
                UPVOTE to NONE -> -1
                UPVOTE to DOWNVOTE -> -2
                DOWNVOTE to NONE -> 1
                DOWNVOTE to UPVOTE -> 2
                else -> 0
            }
    }
}

@Serializable
enum class ContentType {
    PROMOCODE,
    POST
}

/**
 * Unified user interaction model combining votes and bookmarks.
 * Stored in Firestore: /users/{userId}/interactions/{itemType}_{itemId}
 */
@Serializable
data class UserInteraction(
    val itemId: String,
    val itemType: ContentType,
    val userId: UserId,
    val voteState: VoteState,
    val isBookmarked: Boolean
) {
    val id: String get() = generateDocumentId(itemType, itemId)

    init {
        require(itemId.isNotBlank()) { "Item ID cannot be blank" }
    }

    fun toggleUpvote(): UserInteraction =
        copy(
            voteState = when (voteState) {
                VoteState.UPVOTE -> VoteState.NONE
                VoteState.DOWNVOTE, VoteState.NONE -> VoteState.UPVOTE
            },
        )

    fun toggleDownvote(): UserInteraction =
        copy(
            voteState = when (voteState) {
                VoteState.DOWNVOTE -> VoteState.NONE
                VoteState.UPVOTE, VoteState.NONE -> VoteState.DOWNVOTE
            },
        )

    fun toggleBookmark(): UserInteraction = copy(isBookmarked = !isBookmarked)

    companion object {
        fun generateDocumentId(
            itemType: ContentType,
            itemId: String
        ): String = "${itemType.name}_$itemId"
    }
}
