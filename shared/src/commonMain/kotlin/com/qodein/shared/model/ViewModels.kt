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
 * Promocode combined with user interaction state for UI display
 */
@Serializable
data class PromoCodeWithUserState(val promoCode: Promocode, val userInteraction: UserInteraction?) {
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
            promoCode: Promocode,
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
        fun withoutUserState(promoCode: Promocode): PromoCodeWithUserState =
            PromoCodeWithUserState(
                promoCode = promoCode,
                userInteraction = null,
            )
    }
}
