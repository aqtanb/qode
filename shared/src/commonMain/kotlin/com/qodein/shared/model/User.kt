package com.qodein.shared.model

data class User(val id: UserId, val email: Email, val profile: UserProfile, val stats: UserStats, val preferences: UserPreferences) {
    init {
        require(stats.userId == id) { "UserStats userId must match User id" }
        require(preferences.userId == id) { "UserPreferences userId must match User id" }
    }

    val displayName: String get() = profile.displayName
    val username: String get() = profile.username
    val isActive: Boolean get() = stats.isActive
    val level: UserLevel get() = stats.level
    val reputation: Int get() = stats.reputation

    fun isGuest(): Boolean = false // Regular users are never guests

    companion object {
        fun create(
            id: String,
            email: String,
            profile: UserProfile
        ): Result<User> =
            runCatching {
                val userId = UserId(id)
                User(
                    id = userId,
                    email = Email(email),
                    profile = profile,
                    stats = UserStats.initial(userId),
                    preferences = UserPreferences.default(userId),
                )
            }

        fun createWithStats(
            id: UserId,
            email: Email,
            profile: UserProfile,
            stats: UserStats,
            preferences: UserPreferences = UserPreferences.default(id)
        ): Result<User> =
            runCatching {
                User(
                    id = id,
                    email = email,
                    profile = profile,
                    stats = stats,
                    preferences = preferences,
                )
            }
    }
}
