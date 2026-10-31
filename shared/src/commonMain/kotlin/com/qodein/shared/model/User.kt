
@file:UseContextualSerialization(Instant::class) // This line is conceptual, you typically add @Contextual per property

package com.qodein.shared.model

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.UserError
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlin.jvm.JvmInline
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
@JvmInline
value class UserId(val value: String) {
    override fun toString(): String = value
}

enum class Language(val code: String) {
    ENGLISH("en"),
    KAZAKH("kk"),
    RUSSIAN("ru")
}

enum class Theme {
    LIGHT,
    DARK,
    SYSTEM
}

@ConsistentCopyVisibility
data class UserProfile private constructor(val displayName: String?, val photoUrl: String?) {
    companion object {
        const val MAX_DISPLAY_NAME_LENGTH = 100
        private val URL_REGEX = "^https?://.*".toRegex()

        fun create(
            displayName: String?,
            photoUrl: String? = null
        ): Result<UserProfile, UserError.CreationFailure> {
            val cleanDisplayName = displayName?.trim()?.takeIf { it.isNotBlank() }
            val cleanPhotoUrl = photoUrl?.trim()?.takeIf { it.isNotBlank() }

            cleanDisplayName?.let {
                if (it.length > MAX_DISPLAY_NAME_LENGTH) {
                    return Result.Error(UserError.CreationFailure.DisplayNameTooLong)
                }
            }

            cleanPhotoUrl?.let {
                if (!it.matches(URL_REGEX)) {
                    return Result.Error(UserError.CreationFailure.InvalidPhotoUrl)
                }
            }

            return Result.Success(
                UserProfile(
                    displayName = cleanDisplayName,
                    photoUrl = cleanPhotoUrl,
                ),
            )
        }

        fun fromDto(
            displayName: String?,
            photoUrl: String?
        ) = UserProfile(displayName, photoUrl)
    }
}

// MARK:

data class UserStats(
    val userId: UserId,
    val submittedPromocodesCount: Int = 0,
    val submittedPostsCount: Int = 0,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    init {
        require(submittedPromocodesCount >= 0) { "Submitted promocodes count cannot be negative" }
        require(submittedPostsCount >= 0) { "Submitted posts count cannot be negative" }
        require(createdAt > 0) { "Created time must be positive" }
    }

    companion object {

        fun initial(userId: UserId): UserStats = UserStats(userId = userId)
    }
}

data class UserConsent(val legalPoliciesAcceptedAt: Long? = null)

@ConsistentCopyVisibility
data class User private constructor(val id: UserId, val profile: UserProfile, val stats: UserStats, val consent: UserConsent) {
    val displayName: String? get() = profile.displayName

    companion object {
        fun create(
            id: String,
            profile: UserProfile,
            consent: UserConsent
        ): Result<User, UserError.CreationFailure> {
            if (id.isBlank()) return Result.Error(UserError.CreationFailure.InvalidUserId)

            return Result.Success(
                User(
                    id = UserId(id),
                    profile = profile,
                    stats = UserStats.initial(UserId(id)),
                    consent = consent,
                ),
            )
        }

        fun fromDto(
            id: UserId,
            profile: UserProfile,
            stats: UserStats,
            consent: UserConsent
        ): User =
            User(
                id = id,
                profile = profile,
                stats = stats,
                consent = consent,
            )
    }
}
