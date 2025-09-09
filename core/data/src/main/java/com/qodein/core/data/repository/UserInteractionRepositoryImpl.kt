package com.qodein.core.data.repository

import com.qodein.core.data.datasource.FirestoreUserInteractionDataSource
import com.qodein.shared.domain.repository.UserEngagementStats
import com.qodein.shared.domain.repository.UserInteractionRepository
import com.qodein.shared.model.ActivityType
import com.qodein.shared.model.BookmarkType
import com.qodein.shared.model.UserActivity
import com.qodein.shared.model.UserBookmark
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserInteractionRepositoryImpl @Inject constructor(private val dataSource: FirestoreUserInteractionDataSource) :
    UserInteractionRepository {

    // ================================================================================================
    // BOOKMARK OPERATIONS
    // ================================================================================================

    override fun createBookmark(
        userId: UserId,
        itemId: String,
        itemType: BookmarkType,
        itemTitle: String,
        itemCategory: String?
    ): Flow<UserBookmark> =
        flow {
            emit(dataSource.createBookmark(userId, itemId, itemType, itemTitle, itemCategory))
        }

    override fun removeBookmark(
        userId: UserId,
        itemId: String
    ): Flow<Unit> =
        flow {
            dataSource.removeBookmark(userId, itemId)
            emit(Unit)
        }

    override fun getUserBookmarks(
        userId: UserId,
        itemType: BookmarkType?,
        category: String?,
        limit: Int,
        offset: Int
    ): Flow<List<UserBookmark>> =
        flow {
            emit(dataSource.getUserBookmarks(userId, itemType, category, limit, offset))
        }

    override fun isBookmarked(
        userId: UserId,
        itemId: String
    ): Flow<Boolean> =
        flow {
            emit(dataSource.isBookmarked(userId, itemId))
        }

    override fun getBookmarkStatuses(
        userId: UserId,
        itemIds: List<String>
    ): Flow<Map<String, Boolean>> =
        flow {
            emit(dataSource.getBookmarkStatuses(userId, itemIds))
        }

    // ================================================================================================
    // ACTIVITY TRACKING
    // ================================================================================================

    override fun recordActivity(
        userId: UserId,
        type: ActivityType,
        targetId: String,
        targetType: String,
        targetTitle: String?,
        metadata: Map<String, String>
    ): Flow<UserActivity> =
        flow {
            emit(
                dataSource.recordActivity(
                    userId = userId,
                    type = type,
                    targetId = targetId,
                    targetType = targetType,
                    targetTitle = targetTitle,
                    metadata = metadata,
                ),
            )
        }

    override fun getUserActivity(
        userId: UserId,
        activityType: ActivityType?,
        targetType: String?,
        limit: Int,
        offset: Int
    ): Flow<List<UserActivity>> =
        flow {
            emit(dataSource.getUserActivity(userId, activityType, targetType, limit, offset))
        }

    override fun getRecentCommunityActivity(
        activityTypes: List<ActivityType>?,
        timeWindow: Int,
        limit: Int
    ): Flow<List<UserActivity>> =
        flow {
            emit(dataSource.getRecentCommunityActivity(activityTypes, timeWindow, limit))
        }

    override fun getUserEngagementStats(
        userId: UserId,
        timeWindow: Int
    ): Flow<UserEngagementStats> =
        flow {
            emit(dataSource.getUserEngagementStats(userId, timeWindow))
        }
}
