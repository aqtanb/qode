package com.qodein.shared.model

/**
 * Generic sorting options for content
 * Can be used across different content types (PromoCode, Posts, Promos, etc.)
 */
enum class ContentSortBy {
    /**
     * Sort by popularity/score (most popular first)
     * For PromoCode: upvotes - downvotes
     * For Posts: upvotes - downvotes
     * For Promos: upvotes - downvotes
     */
    POPULARITY,

    /**
     * Sort by creation date (newest first)
     */
    NEWEST,

    /**
     * Sort by expiry/end date (expiring soon first)
     * For PromoCode: end date
     * For Posts: It shouldn't have it
     * For Promos: end date
     */
    EXPIRING_SOON
}
