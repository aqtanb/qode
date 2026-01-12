package com.qodein.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class VoteState {
    UPVOTE,
    DOWNVOTE,
    NONE
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
    val id: String get() = "${itemType.name}_$itemId"

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
}
