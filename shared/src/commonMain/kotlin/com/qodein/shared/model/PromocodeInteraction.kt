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
data class PromocodeInteraction(val promocode: Promocode, val userInteraction: UserInteraction?) {
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

    companion object {
        /**
         * Create view model from separate content and user interaction
         */
        fun create(
            promocode: Promocode,
            userInteraction: UserInteraction?
        ): PromocodeInteraction {
            userInteraction?.let { interaction ->
                require(interaction.itemId == promocode.id.value) {
                    "User interaction itemId (${interaction.itemId}) must match promo code ID (${promocode.id.value})"
                }
                require(interaction.itemType == ContentType.PROMOCODE) {
                    "User interaction must be for PROMO_CODE type, got ${interaction.itemType}"
                }
            }

            return PromocodeInteraction(
                promocode = promocode,
                userInteraction = userInteraction,
            )
        }
    }
}
