
@file:UseContextualSerialization(Instant::class) // This line is conceptual, you typically add @Contextual per property

package com.qodein.shared.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlin.jvm.JvmInline
import kotlin.time.Clock
import kotlin.time.Instant

// MARK:

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
    PRIVATE
}

// MARK:

data class NotificationSettings(
    val newCodesFromFollowedStores: Boolean = true,
    val codeExpiringSoon: Boolean = false,
    val weeklyDigest: Boolean = true,
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
                comments = false,
                marketing = false,
            )
    }
}

data class PrivacySettings(
    val profileVisibility: ProfileVisibility = ProfileVisibility.PUBLIC,
    val showEmail: Boolean = false,
    val allowAnalytics: Boolean = true
) {
    companion object {
        fun default() = PrivacySettings()
        fun private() =
            PrivacySettings(
                profileVisibility = ProfileVisibility.PRIVATE,
                showEmail = false,
                allowAnalytics = false,
            )
    }
}

data class UserPreferences(
    val userId: UserId,
    val privacy: PrivacySettings = PrivacySettings.default(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    init {
        require(updatedAt > 0) { "UpdatedAt must be positive" }
    }

    companion object {
        fun default(userId: UserId): UserPreferences = UserPreferences(userId = userId)
    }
}

// MARK:

data class UserProfile(
    val firstName: String,
    val lastName: String?,
    val bio: String?,
    val photoUrl: String?,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    init {
        validateFirstName(firstName)
        lastName?.let { validateLastName(it) }
        bio?.let { validateBio(it) }
        photoUrl?.let { validatePhotoUrl(it) }
        require(createdAt > 0) { "CreatedAt must be positive" }
        require(updatedAt >= createdAt) { "UpdatedAt cannot be before createdAt" }
    }
    val displayName: String get() = listOfNotNull(firstName, lastName).joinToString(" ")
    val initials: String get() = listOfNotNull(
        firstName.firstOrNull()?.toString(),
        lastName?.firstOrNull()?.toString(),
    ).joinToString("")

    companion object {
        const val MIN_FIRST_NAME_LENGTH = 1
        const val MAX_FIRST_NAME_LENGTH = 50
        const val MAX_LAST_NAME_LENGTH = 50
        const val MAX_BIO_LENGTH = 500
        private val NAME_REGEX = "^[a-zA-ZÀ-ÿА-я\\s'-]+$".toRegex()
        private val URL_REGEX = "^https?://.*".toRegex()

        fun createSafe(
            firstName: String,
            lastName: String? = null,
            bio: String? = null,
            photoUrl: String? = null,
            isGenerated: Boolean = false
        ): Result<UserProfile> =
            runCatching {
                UserProfile(
                    firstName = firstName.trim(),
                    lastName = lastName?.trim()?.takeIf { it.isNotBlank() },
                    bio = bio?.trim()?.takeIf { it.isNotBlank() },
                    photoUrl = photoUrl?.trim()?.takeIf { it.isNotBlank() },
                )
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
    }
}

// MARK:

data class UserStats(
    val userId: UserId,
    val followedStores: List<String> = emptyList(),
    val submittedCodes: Int = 0,
    val upvotesReceived: Int = 0,
    val downvotesReceived: Int = 0,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    init {
        require(upvotesReceived >= 0) { "Upvotes received cannot be negative" }
        require(downvotesReceived >= 0) { "Downvotes received cannot be negative" }
        require(submittedCodes >= 0) { "Submitted codes cannot be negative" }
        require(createdAt > 0) { "Created time must be positive" }
        require(followedStores.size <= MAX_FOLLOWED_STORES) {
            "Too many followed stores (max $MAX_FOLLOWED_STORES)"
        }
    }

    companion object {
        const val MAX_FOLLOWED_STORES = 100

        fun initial(userId: UserId): UserStats = UserStats(userId = userId)

        fun createSafe(
            userId: UserId,
            followedStores: List<String> = emptyList(),
            submittedCodes: Int = 0,
            upvotesReceived: Int = 0,
            downvotesReceived: Int = 0
        ): Result<UserStats> =
            runCatching {
                UserStats(
                    userId = userId,
                    followedStores = followedStores.distinct(),
                    submittedCodes = submittedCodes,
                    upvotesReceived = upvotesReceived,
                    downvotesReceived = downvotesReceived,
                )
            }
    }
}

// MARK:

data class User(
    val id: UserId,
    val email: Email,
    val profile: UserProfile,
    val stats: UserStats,
    val country: String? = null // ISO country code for content filtering
) {
    init {
        require(stats.userId == id) { "UserStats userId must match User id" }
        country?.let { require(it.length == 2) { "Country must be 2-letter ISO code" } }
    }

    val displayName: String get() = profile.displayName

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
                    country = country?.uppercase()?.trim(),
                )
            }

        fun createWithStats(
            id: UserId,
            email: Email,
            profile: UserProfile,
            stats: UserStats
        ): Result<User> =
            runCatching {
                User(
                    id = id,
                    email = email,
                    profile = profile,
                    stats = stats,
                )
            }
    }
}
