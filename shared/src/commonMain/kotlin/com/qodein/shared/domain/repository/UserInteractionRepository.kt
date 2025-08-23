package com.qodein.shared.domain.repository

import com.qodein.shared.model.ActivityType
import com.qodein.shared.model.BookmarkType
import com.qodein.shared.model.UserActivity
import com.qodein.shared.model.UserBookmark
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserVote
import com.qodein.shared.model.VoteType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user interaction operations (bookmarks, votes, activities).
 * Handles cross-content user interactions and activity tracking.
 *
 * All methods may throw standard exceptions which will be handled by use cases
 * or ViewModels using the .asResult() extension.
 *
 * Follows NIA pattern - uses standard exceptions, no custom exception hierarchies.
 */
interface UserInteractionRepository {

    // ================================================================================================
    // BOOKMARK OPERATIONS
    // ================================================================================================

    /**
     * Create a bookmark for content (promo code or post).
     *
     * @param userId The user ID
     * @param itemId The content ID (PromoCodeId.value or PostId.value)
     * @param itemType The type of content (PROMO_CODE or POST)
     * @param itemTitle The content title (denormalized)
     * @param itemCategory The content category (denormalized, optional)
     * @return Flow that emits [UserBookmark] on successful creation
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws IllegalArgumentException when item already bookmarked
     */
    fun createBookmark(
        userId: UserId,
        itemId: String,
        itemType: BookmarkType,
        itemTitle: String,
        itemCategory: String? = null
    ): Flow<UserBookmark>

    /**
     * Remove a bookmark.
     *
     * @param userId The user ID
     * @param itemId The content ID
     * @return Flow that emits [Unit] on successful removal
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun removeBookmark(
        userId: UserId,
        itemId: String
    ): Flow<Unit>

    /**
     * Get all bookmarks for a user with filtering.
     *
     * @param userId The user ID
     * @param itemType Filter by content type (optional)
     * @param category Filter by category (optional)
     * @param limit Maximum number of results
     * @param offset Pagination offset
     * @return Flow that emits List<[UserBookmark]> ordered by creation date (newest first)
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getUserBookmarks(
        userId: UserId,
        itemType: BookmarkType? = null,
        category: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<UserBookmark>>

    /**
     * Check if content is bookmarked by user.
     *
     * @param userId The user ID
     * @param itemId The content ID
     * @return Flow that emits [Boolean] indicating bookmark status
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun isBookmarked(
        userId: UserId,
        itemId: String
    ): Flow<Boolean>

    // ================================================================================================
    // VOTE OPERATIONS
    // ================================================================================================

    /**
     * Create or update a vote on content.
     *
     * @param userId The user ID
     * @param itemId The content ID (PromoCodeId.value, PostId.value, or CommentId.value)
     * @param itemType The type of content (PROMO_CODE, POST, or COMMENT)
     * @param isUpvote true for upvote, false for downvote
     * @return Flow that emits [UserVote] on successful vote
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun createOrUpdateVote(
        userId: UserId,
        itemId: String,
        itemType: VoteType,
        isUpvote: Boolean
    ): Flow<UserVote>

    /**
     * Remove a user's vote from content.
     *
     * @param userId The user ID
     * @param itemId The content ID
     * @return Flow that emits [Unit] on successful removal
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun removeVote(
        userId: UserId,
        itemId: String
    ): Flow<Unit>

    /**
     * Get user's vote on specific content.
     *
     * @param userId The user ID
     * @param itemId The content ID
     * @return Flow that emits [UserVote] or null if no vote
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getUserVote(
        userId: UserId,
        itemId: String
    ): Flow<UserVote?>

    /**
     * Get all votes by a user with filtering.
     *
     * @param userId The user ID
     * @param itemType Filter by content type (optional)
     * @param isUpvote Filter by vote type (optional) - true for upvotes, false for downvotes
     * @param limit Maximum number of results
     * @param offset Pagination offset
     * @return Flow that emits List<[UserVote]> ordered by creation date (newest first)
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getUserVotes(
        userId: UserId,
        itemType: VoteType? = null,
        isUpvote: Boolean? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<UserVote>>

    // ================================================================================================
    // ACTIVITY TRACKING
    // ================================================================================================

    /**
     * Record user activity for analytics and feed generation.
     *
     * @param userId The user ID
     * @param type The type of activity
     * @param targetId The ID of the affected content
     * @param targetType The type of target content ("promo_code", "post", "comment")
     * @param targetTitle The title of the target content (optional, for display)
     * @param metadata Additional context data
     * @return Flow that emits [UserActivity] on successful recording
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun recordActivity(
        userId: UserId,
        type: ActivityType,
        targetId: String,
        targetType: String,
        targetTitle: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): Flow<UserActivity>

    /**
     * Get user activity history with filtering.
     *
     * @param userId The user ID
     * @param activityType Filter by activity type (optional)
     * @param targetType Filter by target content type (optional)
     * @param limit Maximum number of results
     * @param offset Pagination offset
     * @return Flow that emits List<[UserActivity]> ordered by creation date (newest first)
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getUserActivity(
        userId: UserId,
        activityType: ActivityType? = null,
        targetType: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<UserActivity>>

    /**
     * Get recent activities across all users for community feed.
     * Useful for generating "recent activity" feeds and trending content.
     *
     * @param activityTypes Filter by activity types (optional)
     * @param timeWindow Hours to look back for activities (default 24h)
     * @param limit Maximum number of results
     * @return Flow that emits List<[UserActivity]> ordered by creation date (newest first)
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getRecentCommunityActivity(
        activityTypes: List<ActivityType>? = null,
        timeWindow: Int = 24,
        limit: Int = 50
    ): Flow<List<UserActivity>>

    /**
     * Get user engagement statistics.
     *
     * @param userId The user ID
     * @param timeWindow Days to look back for statistics (default 30 days)
     * @return Flow that emits [UserEngagementStats] with activity counts
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getUserEngagementStats(
        userId: UserId,
        timeWindow: Int = 30
    ): Flow<UserEngagementStats>

    // ================================================================================================
    // BATCH OPERATIONS
    // ================================================================================================

    /**
     * Get multiple bookmark statuses at once.
     * Useful for checking bookmark status of multiple items in a feed.
     *
     * @param userId The user ID
     * @param itemIds List of content IDs to check
     * @return Flow that emits Map<String, Boolean> where key is itemId and value is bookmark status
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getBookmarkStatuses(
        userId: UserId,
        itemIds: List<String>
    ): Flow<Map<String, Boolean>>

    /**
     * Get multiple vote statuses at once.
     * Useful for showing vote states of multiple items in a feed.
     *
     * @param userId The user ID
     * @param itemIds List of content IDs to check
     * @return Flow that emits Map<String, UserVote?> where key is itemId and value is vote (or null)
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getVoteStatuses(
        userId: UserId,
        itemIds: List<String>
    ): Flow<Map<String, UserVote?>>
}

/**
 * User engagement statistics model.
 */
data class UserEngagementStats(
    val totalActivities: Int = 0,
    val totalUpvotes: Int = 0,
    val totalDownvotes: Int = 0,
    val totalBookmarks: Int = 0,
    val totalShares: Int = 0,
    val totalComments: Int = 0,
    val totalPromoCodesCreated: Int = 0,
    val totalPostsCreated: Int = 0,
    val karmaEarned: Int = 0,
    val mostActiveDay: String? = null, // ISO date string
    val favoriteCategories: List<String> = emptyList()
)
