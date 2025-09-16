@file:UseContextualSerialization(Instant::class)

package com.qodein.shared.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlin.time.Instant

/**
 * View models that combine clean content models with user interaction state.
 * These are used in UI layer when user context is needed.
 *
 * Benefits:
 * - Clean separation: Content models have no user-specific fields
 * - Efficient caching: Content can be cached globally, user state per-user
 * - Atomic loading: Single data source for combined state
 */

/**
 * PromoCode combined with user interaction state for UI display
 */
@Serializable
data class PromoCodeWithUserState(val promoCode: PromoCode, val userInteraction: UserInteraction?) {
    /**
     * Whether the current user has upvoted this promo code
     */
    val isUpvotedByCurrentUser: Boolean
        get() = userInteraction?.voteState == VoteState.UPVOTE

    /**
     * Whether the current user has downvoted this promo code
     */
    val isDownvotedByCurrentUser: Boolean
        get() = userInteraction?.voteState == VoteState.DOWNVOTE

    /**
     * Whether the current user has bookmarked this promo code
     */
    val isBookmarkedByCurrentUser: Boolean
        get() = userInteraction?.isBookmarked == true

    /**
     * Whether the current user has any interaction with this promo code
     */
    val hasUserInteraction: Boolean
        get() = userInteraction != null

    /**
     * Current user's vote state (null if no vote)
     */
    val currentUserVoteState: VoteState
        get() = userInteraction?.voteState ?: VoteState.NONE

    /**
     * When the user last interacted with this content (null if never)
     */
    val lastInteractionTime: Instant?
        get() = userInteraction?.updatedAt

    companion object {
        /**
         * Create view model from separate content and user interaction
         */
        fun create(
            promoCode: PromoCode,
            userInteraction: UserInteraction?
        ): PromoCodeWithUserState {
            // Validate that user interaction matches the content ID
            userInteraction?.let { interaction ->
                require(interaction.itemId == promoCode.id.value) {
                    "User interaction itemId (${interaction.itemId}) must match promo code ID (${promoCode.id.value})"
                }
                require(interaction.itemType == ContentType.PROMO_CODE) {
                    "User interaction must be for PROMO_CODE type, got ${interaction.itemType}"
                }
            }

            return PromoCodeWithUserState(
                promoCode = promoCode,
                userInteraction = userInteraction,
            )
        }

        /**
         * Create view model with no user interaction (anonymous/guest user)
         */
        fun withoutUserState(promoCode: PromoCode): PromoCodeWithUserState =
            PromoCodeWithUserState(
                promoCode = promoCode,
                userInteraction = null,
            )
    }
}

/**
 * Post combined with user interaction state for UI display
 * TODO: Implement when Post voting system is added
 */
@Serializable
data class PostWithUserState(val post: Post, val userInteraction: UserInteraction?) {
    val isUpvotedByCurrentUser: Boolean
        get() = userInteraction?.voteState == VoteState.UPVOTE

    val isDownvotedByCurrentUser: Boolean
        get() = userInteraction?.voteState == VoteState.DOWNVOTE

    val isBookmarkedByCurrentUser: Boolean
        get() = userInteraction?.isBookmarked == true

    val hasUserInteraction: Boolean
        get() = userInteraction != null

    val currentUserVoteState: VoteState
        get() = userInteraction?.voteState ?: VoteState.NONE

    val lastInteractionTime: Instant?
        get() = userInteraction?.updatedAt

    companion object {
        fun create(
            post: Post,
            userInteraction: UserInteraction?
        ): PostWithUserState {
            userInteraction?.let { interaction ->
                require(interaction.itemId == post.id.value) {
                    "User interaction itemId must match post ID"
                }
                require(interaction.itemType == ContentType.POST) {
                    "User interaction must be for POST type"
                }
            }

            return PostWithUserState(
                post = post,
                userInteraction = userInteraction,
            )
        }

        fun withoutUserState(post: Post): PostWithUserState =
            PostWithUserState(
                post = post,
                userInteraction = null,
            )
    }
}

/**
 * Comment combined with user interaction state for UI display
 * TODO: Implement when Comment voting system is added
 */
@Serializable
data class CommentWithUserState(val comment: Comment, val userInteraction: UserInteraction?) {
    val isUpvotedByCurrentUser: Boolean
        get() = userInteraction?.voteState == VoteState.UPVOTE

    val isDownvotedByCurrentUser: Boolean
        get() = userInteraction?.voteState == VoteState.DOWNVOTE

    val hasUserInteraction: Boolean
        get() = userInteraction != null

    val currentUserVoteState: VoteState
        get() = userInteraction?.voteState ?: VoteState.NONE

    companion object {
        fun create(
            comment: Comment,
            userInteraction: UserInteraction?
        ): CommentWithUserState {
            userInteraction?.let { interaction ->
                require(interaction.itemId == comment.id.value) {
                    "User interaction itemId must match comment ID"
                }
                require(interaction.itemType == ContentType.COMMENT) {
                    "User interaction must be for COMMENT type"
                }
            }

            return CommentWithUserState(
                comment = comment,
                userInteraction = userInteraction,
            )
        }

        fun withoutUserState(comment: Comment): CommentWithUserState =
            CommentWithUserState(
                comment = comment,
                userInteraction = null,
            )
    }
}

/**
 * Extension functions for easy conversion from content to view models
 */

/**
 * Convert PromoCode to PromoCodeWithUserState without user interaction
 */
fun PromoCode.withoutUserState(): PromoCodeWithUserState = PromoCodeWithUserState.withoutUserState(this)

/**
 * Convert PromoCode to PromoCodeWithUserState with user interaction
 */
fun PromoCode.withUserState(userInteraction: UserInteraction?): PromoCodeWithUserState =
    PromoCodeWithUserState.create(this, userInteraction)

/**
 * Convert Post to PostWithUserState without user interaction
 */
fun Post.withoutUserState(): PostWithUserState = PostWithUserState.withoutUserState(this)

/**
 * Convert Post to PostWithUserState with user interaction
 */
fun Post.withUserState(userInteraction: UserInteraction?): PostWithUserState = PostWithUserState.create(this, userInteraction)

/**
 * Convert Comment to CommentWithUserState without user interaction
 */
fun Comment.withoutUserState(): CommentWithUserState = CommentWithUserState.withoutUserState(this)

/**
 * Convert Comment to CommentWithUserState with user interaction
 */
fun Comment.withUserState(userInteraction: UserInteraction?): CommentWithUserState = CommentWithUserState.create(this, userInteraction)
