package com.qodein.core.data.datasource

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.mapper.UserActivityMapper
import com.qodein.core.data.mapper.UserBookmarkMapper
import com.qodein.core.data.model.UserActivityDto
import com.qodein.core.data.model.UserBookmarkDto
import com.qodein.shared.domain.repository.UserEngagementStats
import com.qodein.shared.model.ActivityType
import com.qodein.shared.model.BookmarkType
import com.qodein.shared.model.UserActivity
import com.qodein.shared.model.UserBookmark
import com.qodein.shared.model.UserId
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserInteractionDataSource @Inject constructor(private val firestore: FirebaseFirestore) {
    companion object {
        private const val TAG = "FirestoreUserInteractionDS"
        private const val USERS_COLLECTION = "users"
        private const val BOOKMARKS_SUBCOLLECTION = "bookmarks"
        private const val ACTIVITIES_COLLECTION = "user_activities"
    }

    // ================================================================================================
    // BOOKMARK OPERATIONS
    // ================================================================================================

    suspend fun createBookmark(
        userId: UserId,
        itemId: String,
        itemType: BookmarkType,
        itemTitle: String,
        itemCategory: String?
    ): UserBookmark {
        val bookmark = UserBookmark.create(
            userId = userId,
            itemId = itemId,
            itemType = itemType,
            itemTitle = itemTitle,
            itemCategory = itemCategory,
        )

        val dto = UserBookmarkMapper.toDto(bookmark)

        try {
            firestore.collection(USERS_COLLECTION)
                .document(userId.value)
                .collection(BOOKMARKS_SUBCOLLECTION)
                .document(bookmark.id)
                .set(dto)
                .await()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error in createBookmark")

            when {
                e.message?.contains("ALREADY_EXISTS", ignoreCase = true) == true -> {
                    throw IllegalArgumentException("item already bookmarked: $itemId", e)
                }
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true -> {
                    throw SecurityException("permission denied: cannot create bookmark", e)
                }
                e.message?.contains("network", ignoreCase = true) == true ||
                    e.message?.contains("timeout", ignoreCase = true) == true -> {
                    throw IOException("connection error while creating bookmark", e)
                }
                else -> {
                    throw IllegalStateException("service unavailable: failed to create bookmark", e)
                }
            }
        }

        return bookmark
    }

    suspend fun removeBookmark(
        userId: UserId,
        itemId: String
    ) {
        val bookmarkId = "${userId.value}_$itemId"

        firestore.collection(USERS_COLLECTION)
            .document(userId.value)
            .collection(BOOKMARKS_SUBCOLLECTION)
            .document(bookmarkId)
            .delete()
            .await()
    }

    suspend fun getUserBookmarks(
        userId: UserId,
        itemType: BookmarkType?,
        category: String?,
        limit: Int,
        offset: Int
    ): List<UserBookmark> {
        var query: Query = firestore.collection(USERS_COLLECTION)
            .document(userId.value)
            .collection(BOOKMARKS_SUBCOLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        itemType?.let { type ->
            query = query.whereEqualTo("itemType", type.name)
        }

        category?.let { cat ->
            query = query.whereEqualTo("itemCategory", cat)
        }

        query = query.limit(limit.toLong())

        val querySnapshot = query.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<UserBookmarkDto>()?.let { dto ->
                    UserBookmarkMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to parse bookmark ${document.id}")
                null
            }
        }
    }

    suspend fun isBookmarked(
        userId: UserId,
        itemId: String
    ): Boolean {
        val bookmarkId = "${userId.value}_$itemId"

        val document = firestore.collection(USERS_COLLECTION)
            .document(userId.value)
            .collection(BOOKMARKS_SUBCOLLECTION)
            .document(bookmarkId)
            .get()
            .await()

        return document.exists()
    }

    suspend fun getBookmarkStatuses(
        userId: UserId,
        itemIds: List<String>
    ): Map<String, Boolean> {
        if (itemIds.isEmpty()) return emptyMap()

        val bookmarkIds = itemIds.map { "${userId.value}_$it" }
        val results = mutableMapOf<String, Boolean>()

        // Firestore doesn't support batch get for subcollections efficiently
        // Process in chunks to avoid rate limits
        val chunkedIds = itemIds.chunked(10)

        for (chunk in chunkedIds) {
            val tasks = chunk.map { itemId ->
                val bookmarkId = "${userId.value}_$itemId"
                firestore.collection(USERS_COLLECTION)
                    .document(userId.value)
                    .collection(BOOKMARKS_SUBCOLLECTION)
                    .document(bookmarkId)
                    .get()
            }

            tasks.forEachIndexed { index, task ->
                val itemId = chunk[index]
                try {
                    val document = task.await()
                    results[itemId] = document.exists()
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Failed to check bookmark status for $itemId")
                    results[itemId] = false
                }
            }
        }

        return results
    }

    // ================================================================================================
    // ACTIVITY TRACKING
    // ================================================================================================

    suspend fun recordActivity(
        userId: UserId,
        type: ActivityType,
        targetId: String,
        targetType: String,
        targetTitle: String?,
        metadata: Map<String, String>
    ): UserActivity {
        val activity = UserActivity.create(
            userId = userId,
            type = type,
            targetId = targetId,
            targetType = targetType,
            targetTitle = targetTitle,
            metadata = metadata,
        )

        val dto = UserActivityMapper.toDto(activity)

        firestore.collection(ACTIVITIES_COLLECTION)
            .document(activity.id)
            .set(dto)
            .await()

        return activity
    }

    suspend fun getUserActivity(
        userId: UserId,
        activityType: ActivityType?,
        targetType: String?,
        limit: Int,
        offset: Int
    ): List<UserActivity> {
        var query: Query = firestore.collection(ACTIVITIES_COLLECTION)
            .whereEqualTo("userId", userId.value)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        activityType?.let { type ->
            query = query.whereEqualTo("type", type.name)
        }

        targetType?.let { target ->
            query = query.whereEqualTo("targetType", target)
        }

        query = query.limit(limit.toLong())

        val querySnapshot = query.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<UserActivityDto>()?.let { dto ->
                    UserActivityMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to parse activity ${document.id}")
                null
            }
        }
    }

    suspend fun getRecentCommunityActivity(
        activityTypes: List<ActivityType>?,
        timeWindow: Int,
        limit: Int
    ): List<UserActivity> {
        val cutoffTime = System.currentTimeMillis() - (timeWindow * 60 * 60 * 1000)

        var query: Query = firestore.collection(ACTIVITIES_COLLECTION)
            .whereGreaterThan("createdAt", Timestamp(cutoffTime / 1000, 0))
            .orderBy("createdAt", Query.Direction.DESCENDING)

        activityTypes?.let { types ->
            if (types.isNotEmpty()) {
                query = query.whereIn("type", types.map { it.name })
            }
        }

        query = query.limit(limit.toLong())

        val querySnapshot = query.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<UserActivityDto>()?.let { dto ->
                    UserActivityMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to parse community activity ${document.id}")
                null
            }
        }
    }

    suspend fun getUserEngagementStats(
        userId: UserId,
        timeWindow: Int
    ): UserEngagementStats {
        val cutoffTime = System.currentTimeMillis() - (timeWindow * 24 * 60 * 60 * 1000L)

        try {
            // Get user activities in the time window
            val activitiesQuery = firestore.collection(ACTIVITIES_COLLECTION)
                .whereEqualTo("userId", userId.value)
                .whereGreaterThan("createdAt", Timestamp(cutoffTime / 1000, 0))
                .get()
                .await()

            // Get user bookmarks in the time window
            val bookmarksQuery = firestore.collection(USERS_COLLECTION)
                .document(userId.value)
                .collection(BOOKMARKS_SUBCOLLECTION)
                .whereGreaterThan("createdAt", Timestamp(cutoffTime / 1000, 0))
                .get()
                .await()

            val activities = activitiesQuery.documents.mapNotNull { doc ->
                try {
                    doc.toObject<UserActivityDto>()?.let { dto ->
                        UserActivityMapper.toDomain(dto)
                    }
                } catch (e: Exception) {
                    null
                }
            }

            val shares = activities.count { it.type == ActivityType.SHARED }
            val comments = activities.count { it.type == ActivityType.COMMENTED }
            val promoCodesCreated = activities.count { it.type == ActivityType.CREATED_PROMO_CODE }
            val postsCreated = activities.count { it.type == ActivityType.CREATED_POST }

            return UserEngagementStats(
                totalActivities = activities.size,
                totalUpvotes = 0, // TODO: Get from FirebaseVoteDataSource
                totalDownvotes = 0, // TODO: Get from FirebaseVoteDataSource
                totalBookmarks = bookmarksQuery.documents.size,
                totalShares = shares,
                totalComments = comments,
                totalPromoCodesCreated = promoCodesCreated,
                totalPostsCreated = postsCreated,
                karmaEarned = 0, // TODO: Recalculate without votes
                mostActiveDay = findMostActiveDay(activities),
                favoriteCategories = findFavoriteCategories(activities),
            )
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to calculate user engagement stats")
            return UserEngagementStats() // Return empty stats on error
        }
    }

    // Helper methods
    private fun findMostActiveDay(activities: List<UserActivity>): String? {
        if (activities.isEmpty()) return null

        return activities
            .groupBy { activity ->
                // Group by day (ISO date format)
                val instant = activity.createdAt
                "${instant.toEpochMilliseconds() / (24 * 60 * 60 * 1000)}" // Day since epoch
            }
            .maxByOrNull { it.value.size }
            ?.key
    }

    private fun findFavoriteCategories(activities: List<UserActivity>): List<String> =
        activities
            .mapNotNull { it.metadata["category"] }
            .groupBy { it }
            .toList()
            .sortedByDescending { it.second.size }
            .take(3)
            .map { it.first }
}
