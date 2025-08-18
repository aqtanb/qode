package com.qodein.core.model

data class UserPreferences(
    val userId: UserId,
    val language: Language = Language.RUSSIAN, // Default to Russian for KZ market
    val theme: Theme = Theme.SYSTEM,
    val notifications: NotificationSettings = NotificationSettings.default(),
    val privacy: PrivacySettings = PrivacySettings.default(),
    val feed: FeedSettings = FeedSettings.default(),
    val location: LocationSettings? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    init {
        require(updatedAt > 0) { "UpdatedAt must be positive" }
    }

    companion object {
        fun default(userId: UserId): UserPreferences = UserPreferences(userId = userId)
    }
}

enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    KAZAKH("kk", "Қазақша"),
    RUSSIAN("ru", "Русский")
}

enum class Theme {
    LIGHT,
    DARK,
    SYSTEM
}

data class NotificationSettings(
    val newCodesFromFollowedStores: Boolean = true,
    val codeExpiringSoon: Boolean = false,
    val weeklyDigest: Boolean = true,
    val achievements: Boolean = true,
    val comments: Boolean = true,
    val marketing: Boolean = false
) {
    companion object {
        fun default() = NotificationSettings()
        fun minimal() =
            NotificationSettings(
                newCodesFromFollowedStores = false,
                codeExpiringSoon = false,
                weeklyDigest = false,
                achievements = false,
                comments = false,
                marketing = false,
            )
    }
}

data class PrivacySettings(
    val profileVisibility: ProfileVisibility = ProfileVisibility.PUBLIC,
    val showEmail: Boolean = false,
    val showBirthday: Boolean = false,
    val allowAnalytics: Boolean = true,
    val allowPersonalization: Boolean = true
) {
    companion object {
        fun default() = PrivacySettings()
        fun private() =
            PrivacySettings(
                profileVisibility = ProfileVisibility.PRIVATE,
                showEmail = false,
                showBirthday = false,
                allowAnalytics = false,
                allowPersonalization = false,
            )
    }
}

enum class ProfileVisibility {
    PUBLIC,
    FRIENDS_ONLY,
    PRIVATE
}

data class FeedSettings(
    val showExpiredCodes: Boolean = false,
    val showVerifiedOnly: Boolean = false,
    val showOnlyFollowed: Boolean = false,
    val defaultSortOrder: SortOrder = SortOrder.NEWEST
) {
    companion object {
        fun default() = FeedSettings()
    }
}

enum class SortOrder {
    NEWEST,
    POPULAR,
    EXPIRING_SOON,
    HIGHEST_DISCOUNT
}

data class LocationSettings(val allowLocationTracking: Boolean = false, val showLocalDeals: Boolean = true, val radius: Int = 50) {
    init {
        require(radius in 1..1000) { "Radius must be between 1 and 1000 km" }
    }
}
