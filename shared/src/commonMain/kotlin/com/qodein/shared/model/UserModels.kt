
@file:UseContextualSerialization(Instant::class) // This line is conceptual, you typically add @Contextual per property

package com.qodein.shared.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearsUntil
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlin.jvm.JvmInline
import kotlin.time.Clock
import kotlin.time.Instant

// ================================================================================================
// BASIC VALUE CLASSES
// ================================================================================================

@Serializable
@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "UserId cannot be blank" }
        require(value.length <= 128) { "UserId too long (max 128 characters)" }
        require(value.matches(USER_ID_REGEX)) { "UserId contains invalid characters" }
    }

    companion object {
        private val USER_ID_REGEX = "^[a-zA-Z0-9_-]+$".toRegex()

        fun createSafe(value: String): Result<UserId> =
            runCatching {
                UserId(value.trim())
            }
    }
}

@Serializable
@JvmInline
value class Email(val value: String) {
    init {
        val normalized = value.trim().lowercase()
        require(normalized.isNotBlank()) { "Email cannot be blank" }
        require(normalized.length <= 254) { "Email too long (max 254 characters)" }
        require(normalized.matches(EMAIL_REGEX)) { "Invalid email format" }
    }

    val domain: String get() = value.substringAfter("@")
    val localPart: String get() = value.substringBefore("@")
    val normalized: String get() = value.trim().lowercase()

    companion object {
        private val EMAIL_REGEX = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".toRegex()

        fun createSafe(value: String): Result<Email> =
            runCatching {
                Email(value.trim().lowercase())
            }

        fun isValid(value: String): Boolean = value.trim().lowercase().matches(EMAIL_REGEX)
    }
}

// ================================================================================================
// ENUMS
// ================================================================================================

enum class Gender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    companion object {
        fun fromString(value: String?): Gender? =
            when (value?.lowercase()) {
                "male", "m" -> MALE
                "female", "f" -> FEMALE
                "other", "o" -> OTHER
                else -> null
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

enum class ProfileVisibility {
    PUBLIC,
    FRIENDS_ONLY,
    PRIVATE
}

enum class SortOrder {
    NEWEST,
    POPULAR,
    EXPIRING_SOON,
    HIGHEST_DISCOUNT
}

// ================================================================================================
// SUPPORTING DATA CLASSES
// ================================================================================================

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

data class LocationSettings(val allowLocationTracking: Boolean = false, val showLocalDeals: Boolean = true, val radius: Int = 50) {
    init {
        require(radius in 1..1000) { "Radius must be between 1 and 1000 km" }
    }
}

// ================================================================================================
// USER PREFERENCES
// ================================================================================================

data class UserPreferences(
    val userId: UserId,
    val language: Language = Language.RUSSIAN, // Default to Russian for KZ market
    val theme: Theme = Theme.SYSTEM,
    val notifications: NotificationSettings = NotificationSettings.default(),
    val privacy: PrivacySettings = PrivacySettings.default(),
    val feed: FeedSettings = FeedSettings.default(),
    val location: LocationSettings? = null,
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    init {
        require(updatedAt > 0) { "UpdatedAt must be positive" }
    }

    companion object {
        fun default(userId: UserId): UserPreferences = UserPreferences(userId = userId)
    }
}

// ================================================================================================
// USER PROFILE
// ================================================================================================

data class UserProfile(
    val username: String,
    val firstName: String,
    val lastName: String?,
    val bio: String?,
    val photoUrl: String?,
    val birthday: LocalDate?,
    val gender: Gender?,
    val isGenerated: Boolean = false, // Was profile auto-generated?
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    init {
        validateUsername(username)
        validateFirstName(firstName)
        lastName?.let { validateLastName(it) }
        bio?.let { validateBio(it) }
        photoUrl?.let { validatePhotoUrl(it) }
        birthday?.let { validateBirthday(it) }
        require(createdAt > 0) { "CreatedAt must be positive" }
        require(updatedAt >= createdAt) { "UpdatedAt cannot be before createdAt" }
    }

    val displayName: String get() = listOfNotNull(firstName, lastName).joinToString(" ")
    val fullName: String get() = displayName
    val initials: String get() = listOfNotNull(
        firstName.firstOrNull()?.toString(),
        lastName?.firstOrNull()?.toString(),
    ).joinToString("")

    val isComplete: Boolean get() = !isGenerated &&
        firstName.isNotBlank() &&
        username.isNotBlank() &&
        (bio?.isNotBlank() == true || birthday != null)

    val age: Int? get() = birthday?.let {
        it.yearsUntil(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }

    companion object {
        const val MIN_USERNAME_LENGTH = 3
        const val MAX_USERNAME_LENGTH = 30
        const val MIN_FIRST_NAME_LENGTH = 1
        const val MAX_FIRST_NAME_LENGTH = 50
        const val MAX_LAST_NAME_LENGTH = 50
        const val MAX_BIO_LENGTH = 500
        const val MIN_AGE = 13
        const val MAX_AGE = 120

        private val USERNAME_REGEX = "^[a-zA-Z0-9_]+$".toRegex()
        private val NAME_REGEX = "^[a-zA-ZÀ-ÿА-я\\s'-]+$".toRegex()
        private val URL_REGEX = "^https?://.*".toRegex()

        fun createSafe(
            username: String,
            firstName: String,
            lastName: String? = null,
            bio: String? = null,
            photoUrl: String? = null,
            birthday: LocalDate? = null,
            gender: Gender? = null,
            isGenerated: Boolean = false
        ): Result<UserProfile> =
            runCatching {
                UserProfile(
                    username = username.trim(),
                    firstName = firstName.trim(),
                    lastName = lastName?.trim()?.takeIf { it.isNotBlank() },
                    bio = bio?.trim()?.takeIf { it.isNotBlank() },
                    photoUrl = photoUrl?.trim()?.takeIf { it.isNotBlank() },
                    birthday = birthday,
                    gender = gender,
                    isGenerated = isGenerated,
                )
            }

        private fun validateUsername(username: String) {
            require(username.isNotBlank()) { "Username cannot be blank" }
            require(username.length >= MIN_USERNAME_LENGTH) {
                "Username too short (min $MIN_USERNAME_LENGTH characters)"
            }
            require(username.length <= MAX_USERNAME_LENGTH) {
                "Username too long (max $MAX_USERNAME_LENGTH characters)"
            }
            require(username.matches(USERNAME_REGEX)) {
                "Username can only contain letters, numbers, and underscores"
            }
            require(!username.startsWith("_")) { "Username cannot start with underscore" }
            require(!username.endsWith("_")) { "Username cannot end with underscore" }
        }

        private fun validateFirstName(firstName: String) {
            require(firstName.isNotBlank()) { "First name cannot be blank" }
            require(firstName.length >= MIN_FIRST_NAME_LENGTH) {
                "First name too short"
            }
            require(firstName.length <= MAX_FIRST_NAME_LENGTH) {
                "First name too long (max $MAX_FIRST_NAME_LENGTH characters)"
            }
            require(firstName.matches(NAME_REGEX)) {
                "First name contains invalid characters"
            }
        }

        private fun validateLastName(lastName: String) {
            require(lastName.length <= MAX_LAST_NAME_LENGTH) {
                "Last name too long (max $MAX_LAST_NAME_LENGTH characters)"
            }
            require(lastName.matches(NAME_REGEX)) {
                "Last name contains invalid characters"
            }
        }

        private fun validateBio(bio: String) {
            require(bio.length <= MAX_BIO_LENGTH) {
                "Bio too long (max $MAX_BIO_LENGTH characters)"
            }
        }

        private fun validatePhotoUrl(photoUrl: String) {
            require(photoUrl.matches(URL_REGEX)) {
                "Photo URL must be a valid HTTP/HTTPS URL"
            }
            require(photoUrl.length <= 2048) { "Photo URL too long" }
        }

        private fun validateBirthday(birthday: LocalDate) {
            val age = birthday.yearsUntil(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
            require(age >= MIN_AGE) { "User must be at least $MIN_AGE years old" }
            require(age <= MAX_AGE) { "Invalid birth date" }
        }
    }
}

// ================================================================================================
// USER STATS
// ================================================================================================

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

// ================================================================================================
// MAIN USER MODEL
// ================================================================================================

data class User(
    val id: UserId,
    val email: Email,
    val profile: UserProfile,
    val stats: UserStats,
    val preferences: UserPreferences,
    val country: String? = null, // ISO country code for content filtering
    val karma: Int = 0, // Total upvotes - downvotes across all user content
    val lastActivityAt: Instant = Clock.System.now(),
    val totalPromoCodesSubmitted: Int = 0,
    val totalPostsCreated: Int = 0,
    val totalCommentsLeft: Int = 0
) {
    init {
        require(stats.userId == id) { "UserStats userId must match User id" }
        require(preferences.userId == id) { "UserPreferences userId must match User id" }
        require(totalPromoCodesSubmitted >= 0) { "Total promo codes submitted cannot be negative" }
        require(totalPostsCreated >= 0) { "Total posts created cannot be negative" }
        require(totalCommentsLeft >= 0) { "Total comments left cannot be negative" }
        country?.let { require(it.length == 2) { "Country must be 2-letter ISO code" } }
    }

    val displayName: String get() = profile.displayName
    val username: String get() = profile.username
    val isActive: Boolean get() = stats.isActive
    val level: UserLevel get() = stats.level
    val reputation: Int get() = stats.reputation
    val totalContentCreated: Int get() = totalPromoCodesSubmitted + totalPostsCreated + totalCommentsLeft

    fun isGuest(): Boolean = false // Regular users are never guests

    companion object {
        fun create(
            id: String,
            email: String,
            profile: UserProfile,
            country: String? = null
        ): Result<User> =
            runCatching {
                val userId = UserId(id)
                User(
                    id = userId,
                    email = Email(email),
                    profile = profile,
                    stats = UserStats.initial(userId),
                    preferences = UserPreferences.default(userId),
                    country = country?.uppercase()?.trim(),
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

// ================================================================================================
// GUEST USER
// ================================================================================================

/**
 * Represents a guest user who can browse but not interact
 */
object GuestUser {
    val id: UserId = UserId("guest")
    val displayName: String get() = "Guest" // Hardcoded, no need to store
    val level: UserLevel = UserLevel.NEWCOMER

    fun isGuest(): Boolean = true
    fun canInteract(): Boolean = false
    fun canSubmit(): Boolean = false
    fun canVote(): Boolean = false
    fun canComment(): Boolean = false
    fun canFollow(): Boolean = false
}

/**
 * Union type for authenticated and guest users
 */
sealed interface UserSession {
    data class Authenticated(val user: User) : UserSession {
        fun isGuest(): Boolean = false
        fun canInteract(): Boolean = true
    }

    data object Guest : UserSession {
        fun isGuest(): Boolean = true
        fun canInteract(): Boolean = false
    }

    data object Unauthenticated : UserSession {
        fun isGuest(): Boolean = false
        fun canInteract(): Boolean = false
    }
}
