package com.qodein.shared.model

import kotlinx.datetime.Clock

data class UserStats(
    val userId: UserId,
    val followedStores: List<String> = emptyList(),
    val followedCategories: List<String> = emptyList(),
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val submittedCodes: Int = 0,
    val verifiedCodes: Int = 0, // Codes that got 10+ upvotes
    val achievements: List<String> = emptyList(),
    val commentsCount: Int = 0,
    val lastActiveAt: Long = Clock.System.now().toEpochMilliseconds(),
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    init {
        require(upvotes >= 0) { "Upvotes cannot be negative" }
        require(downvotes >= 0) { "Downvotes cannot be negative" }
        require(submittedCodes >= 0) { "Submitted codes cannot be negative" }
        require(verifiedCodes >= 0) { "Verified codes cannot be negative" }
        require(verifiedCodes <= submittedCodes) { "Verified codes cannot exceed submitted codes" }
        require(commentsCount >= 0) { "Comments count cannot be negative" }
        require(lastActiveAt > 0) { "Last active time must be positive" }
        require(createdAt > 0) { "Created time must be positive" }
        require(followedStores.size <= MAX_FOLLOWED_STORES) {
            "Too many followed stores (max $MAX_FOLLOWED_STORES)"
        }
        require(followedCategories.size <= MAX_FOLLOWED_CATEGORIES) {
            "Too many followed categories (max $MAX_FOLLOWED_CATEGORIES)"
        }
    }

    val totalVotes: Int get() = upvotes + downvotes
    val rating: Double get() = if (totalVotes == 0) 0.0 else upvotes.toDouble() / totalVotes
    val reputation: Int get() = (upvotes * 2) + (verifiedCodes * 10) - downvotes

    val level: UserLevel get() = when {
        reputation < 0 -> UserLevel.RESTRICTED
        submittedCodes == 0 -> UserLevel.NEWCOMER
        submittedCodes < 5 -> UserLevel.BEGINNER
        submittedCodes < 20 && verifiedCodes < 3 -> UserLevel.CONTRIBUTOR
        submittedCodes < 50 && verifiedCodes < 10 -> UserLevel.HUNTER
        verifiedCodes >= 25 -> UserLevel.MASTER
        else -> UserLevel.EXPERT
    }

    val isActive: Boolean get() = submittedCodes > 0 ||
        followedStores.isNotEmpty() ||
        followedCategories.isNotEmpty()

    val isRecentlyActive: Boolean get() =
        Clock.System.now().toEpochMilliseconds() - lastActiveAt < RECENT_ACTIVITY_THRESHOLD

    val nextLevelProgress: Float get() = when (level) {
        UserLevel.NEWCOMER -> submittedCodes / 1f
        UserLevel.BEGINNER -> submittedCodes / 5f
        UserLevel.CONTRIBUTOR -> submittedCodes / 20f
        UserLevel.HUNTER -> submittedCodes / 50f
        UserLevel.EXPERT -> verifiedCodes / 25f
        UserLevel.MASTER -> 1f
        UserLevel.RESTRICTED -> 0f
    }

    companion object {
        const val MAX_FOLLOWED_STORES = 100
        const val MAX_FOLLOWED_CATEGORIES = 20
        private const val RECENT_ACTIVITY_THRESHOLD = 7 * 24 * 60 * 60 * 1000L // 7 days

        fun initial(userId: UserId): UserStats = UserStats(userId = userId)

        fun createSafe(
            userId: UserId,
            followedStores: List<String> = emptyList(),
            followedCategories: List<String> = emptyList(),
            upvotes: Int = 0,
            downvotes: Int = 0,
            submittedCodes: Int = 0,
            verifiedCodes: Int = 0,
            achievements: List<String> = emptyList(),
            commentsCount: Int = 0
        ): Result<UserStats> =
            runCatching {
                UserStats(
                    userId = userId,
                    followedStores = followedStores.distinct(),
                    followedCategories = followedCategories.distinct(),
                    upvotes = upvotes,
                    downvotes = downvotes,
                    submittedCodes = submittedCodes,
                    verifiedCodes = verifiedCodes,
                    achievements = achievements.distinct(),
                    commentsCount = commentsCount,
                )
            }
    }
}

enum class UserLevel(val displayName: String, val color: String) {
    RESTRICTED("Restricted", "#FF5722"),
    NEWCOMER("Newcomer", "#9E9E9E"),
    BEGINNER("Beginner", "#4CAF50"),
    CONTRIBUTOR("Contributor", "#2196F3"),
    HUNTER("Deal Hunter", "#FF9800"),
    EXPERT("Expert", "#9C27B0"),
    MASTER("Deal Master", "#FFD700")
}
