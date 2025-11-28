package com.qodein.shared.model

/**
 * Base interface for all sorting options.
 * Used to make pagination type-safe across different content types.
 */
sealed interface SortBy

/**
 * Generic sorting options for content
 * Can be used across different content types (Promocode, Promos, etc.)
 */
enum class ContentSortBy : SortBy {
    /**
     * Sort by popularity/score (most popular first)
     */
    POPULARITY,

    /**
     * Sort by creation date (newest first)
     */
    NEWEST,

    /**
     * Sort by expiry/end date (expiring soon first)
     */
    EXPIRING_SOON
}

/**
 * Sorting options for posts (no expiry date)
 */
enum class PostSortBy : SortBy {
    /**
     * Sort by popularity (upvotes descending)
     */
    POPULARITY,

    /**
     * Sort by creation date (newest first)
     */
    NEWEST
}
