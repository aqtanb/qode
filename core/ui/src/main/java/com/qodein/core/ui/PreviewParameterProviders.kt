@file:Suppress("ktlint:standard:max-line-length")

package com.qodein.core.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.qodein.core.model.Email
import com.qodein.core.model.Gender
import com.qodein.core.model.User
import com.qodein.core.model.UserId
import com.qodein.core.model.UserPreferences
import com.qodein.core.model.UserProfile
import com.qodein.core.model.UserStats
import java.time.LocalDate

/**
 * Preview parameter provider for [User] data.
 * Provides various user scenarios for Profile screen testing.
 */
class UserPreviewParameterProvider : PreviewParameterProvider<User> {
    override val values: Sequence<User> = sequenceOf(
        PreviewParameterData.sampleUser,
        PreviewParameterData.newUser,
        PreviewParameterData.powerUser,
        PreviewParameterData.userWithLongBio,
    )
}

/**
 * Preview parameter provider for [UserStats] data.
 */
class UserStatsPreviewParameterProvider : PreviewParameterProvider<UserStats> {
    override val values: Sequence<UserStats> = sequenceOf(
        PreviewParameterData.sampleUserStats,
        PreviewParameterData.newUserStats,
        PreviewParameterData.powerUserStats,
    )
}

/**
 * Central object containing preview data for Profile screen.
 * This follows NIA's pattern of centralizing preview data for consistency.
 * Uses lazy initialization to prevent class loading issues during preview compilation.
 */
object PreviewParameterData {

    private const val THIRTY_MINUTES_AGO = 1800000L
    private const val ONE_HOUR_AGO = 3600000L
    private const val ONE_DAY_AGO = 86400000L
    private const val ONE_YEAR_AGO = 31536000000L
    private const val SIX_MONTHS_AGO = 15552000000L

    val sampleUserStats by lazy {
        UserStats(
            userId = UserId("sample-user-123"),
            followedStores = listOf("Kaspi", "Technodom", "Arbuz"),
            followedCategories = listOf("Electronics", "Fashion", "Food"),
            upvotes = 234,
            downvotes = 8,
            submittedCodes = 42,
            verifiedCodes = 38,
            achievements = listOf(
                "First Code",
                "Code Hunter",
                "Community Helper",
                "Savings Master",
            ),
            commentsCount = 156,
            lastActiveAt = 1700000000000L,
            createdAt = 1699000000000L,
        )
    }

    val newUserStats by lazy {
        UserStats(
            userId = UserId("new-user-456"),
            followedStores = emptyList(),
            followedCategories = emptyList(),
            upvotes = 0,
            downvotes = 0,
            submittedCodes = 0,
            verifiedCodes = 0,
            achievements = emptyList(),
            commentsCount = 0,
            lastActiveAt = 1700000000000L,
            createdAt = 1699000000000L,
        )
    }

    val powerUserStats by lazy {
        UserStats(
            userId = UserId("power-user-789"),
            followedStores = listOf(
                "Kaspi", "Technodom", "Arbuz.kz", "Sulpak", "Mechta",
                "Wildberries", "Ozon", "Magnum", "Small", "Beeline",
            ),
            followedCategories = listOf(
                "Electronics",
                "Fashion",
                "Food & Drinks",
                "Beauty & Health",
                "Sports & Outdoor",
                "Home & Garden",
                "Books & Education",
            ),
            upvotes = 2847,
            downvotes = 23,
            submittedCodes = 567,
            verifiedCodes = 534,
            achievements = listOf(
                "First Code",
                "Code Hunter",
                "Community Helper",
                "Savings Master",
                "Review Champion",
                "Early Adopter",
                "Top Contributor",
                "Legend",
                "Verification Hero",
            ),
            commentsCount = 1204,
            lastActiveAt = 1700000000000L,
            createdAt = 1668000000000L,
        )
    }

    val sampleUser by lazy {
        User(
            id = UserId("sample-user-123"),
            email = Email("john.doe@example.com"),
            profile = UserProfile(
                username = "aqtanb",
                firstName = "John",
                lastName = "Doe",
                bio = "Love finding the best deals! Android developer by day, savings hunter by night 🛒✨",
                photoUrl = "https://i.pravatar.cc/250?u=mail@ashallendesign.co.uk",
                birthday = LocalDate.of(1990, 5, 15),
                gender = null,
                isGenerated = false,
                createdAt = 1699000000000L,
                updatedAt = 1700000000000L,
            ),
            stats = sampleUserStats,
            preferences = UserPreferences(userId = UserId("sample-user-123")),
        )
    }

    val newUser by lazy {
        User(
            id = UserId("new-user-456"),
            email = Email("sarah.wilson@gmail.com"),
            profile = UserProfile(
                username = "sarahw",
                firstName = "Sarah",
                lastName = "Wilson",
                bio = null,
                photoUrl = null,
                birthday = null,
                gender = null,
                isGenerated = false,
                createdAt = 1699900000000L,
                updatedAt = 1700000000000L,
            ),
            stats = newUserStats,
            preferences = UserPreferences.default(UserId("new-user-456")),
        )
    }

    val powerUser by lazy {
        User(
            id = UserId("power-user-789"),
            email = Email("alex.power@qode.kz"),
            profile = UserProfile(
                username = "alexpower",
                firstName = "Alex",
                lastName = "Powerov",
                bio = "Qode enthusiast since day one! 🏆 Top contributor with 500+ verified codes. Always hunting for the best Kazakhstan deals. Follow me for daily savings tips! 💰",
                photoUrl = "https://example.com/avatar/alexpower.jpg",
                birthday = LocalDate.of(1985, 12, 3),
                gender = Gender.MALE,
                isGenerated = false,
                createdAt = 1668000000000L,
                updatedAt = 1699000000000L,
            ),
            stats = powerUserStats,
            preferences = UserPreferences(userId = UserId("power-user-789")),
        )
    }

    val longBioUserStats by lazy {
        UserStats(
            userId = UserId("long-bio-user"),
            followedStores = listOf("Kaspi", "Technodom", "Arbuz"),
            followedCategories = listOf("Electronics", "Fashion", "Food"),
            upvotes = 156,
            downvotes = 3,
            submittedCodes = 28,
            verifiedCodes = 25,
            achievements = listOf(
                "First Code",
                "Community Helper",
                "Savings Master",
            ),
            commentsCount = 89,
            lastActiveAt = 1699900000000L,
            createdAt = 1684000000000L,
        )
    }

    val userWithLongBio by lazy {
        User(
            id = UserId("long-bio-user"),
            email = Email("storyteller@example.com"),
            profile = UserProfile(
                username = "storyteller",
                firstName = "Maria",
                lastName = "Storyteller",
                bio = "🌟 Welcome to my profile! I'm a passionate deal hunter from Almaty who loves sharing amazing savings opportunities with the community. I specialize in finding the best electronics deals, fashion discounts, and food delivery codes. When I'm not hunting for codes, I enjoy photography, hiking in the beautiful Kazakh mountains, and trying new restaurants around the city. Follow me for daily deals and let's save money together! 💫🛍️🏔️",
                photoUrl = "https://example.com/avatar/maria.jpg",
                birthday = LocalDate.of(1992, 8, 20),
                gender = Gender.FEMALE,
                isGenerated = false,
                createdAt = 1684000000000L,
                updatedAt = 1699900000000L,
            ),
            stats = longBioUserStats,
            preferences = UserPreferences(userId = UserId("long-bio-user")),
        )
    }
}
