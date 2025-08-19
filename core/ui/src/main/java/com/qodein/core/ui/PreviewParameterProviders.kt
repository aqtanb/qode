@file:Suppress("ktlint:standard:max-line-length")

package com.qodein.core.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.qodein.core.model.Banner
import com.qodein.core.model.BannerId
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
 * Preview parameter provider for [Banner] data.
 * Provides various banner scenarios for component testing.
 */
class BannerPreviewParameterProvider : PreviewParameterProvider<Banner> {
    override val values: Sequence<Banner> = sequenceOf(
        PreviewParameterData.sampleBanner,
        PreviewParameterData.flashSaleBanner,
        PreviewParameterData.expiredBanner,
        PreviewParameterData.noImageBanner,
        PreviewParameterData.globalBanner,
        PreviewParameterData.darkModeBanner,
    )
}

/**
 * Preview parameter provider for [List<Banner>] data.
 * Provides various banner list scenarios for section testing.
 */
class BannerListPreviewParameterProvider : PreviewParameterProvider<List<Banner>> {
    override val values: Sequence<List<Banner>> = sequenceOf(
        listOf(PreviewParameterData.sampleBanner, PreviewParameterData.flashSaleBanner),
        listOf(PreviewParameterData.sampleBanner),
        emptyList(),
        PreviewParameterData.allSampleBanners,
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

    // Banner sample data
    val sampleBanner by lazy {
        Banner(
            id = BannerId("sample-banner-1"),
            title = "Summer Sale 2024",
            description = "Get up to 70% off on all summer collections",
            imageUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755436194/main-sample.png",
            targetCountries = listOf("KZ", "US"),
            brandName = "Fashion Store",
            ctaTitle = mapOf(
                "default" to "Shop Now",
                "en" to "Shop Now",
                "kk" to "Сатып алу",
                "ru" to "Купить сейчас",
            ),
            ctaDescription = mapOf(
                "default" to "Great deals await",
                "en" to "Great deals await",
                "kk" to "Керемет ұсыныстар күтуде",
                "ru" to "Отличные предложения ждут",
            ),
            ctaUrl = "https://example.com/summer-sale",
            isActive = true,
            priority = 1,
            createdAt = System.currentTimeMillis() - ONE_HOUR_AGO,
            updatedAt = System.currentTimeMillis() - THIRTY_MINUTES_AGO,
            expiresAt = System.currentTimeMillis() + (7 * ONE_DAY_AGO), // Expires in 7 days
        )
    }

    val flashSaleBanner by lazy {
        Banner(
            id = BannerId("flash-sale-banner"),
            title = "Flash Sale",
            description = "Limited time offer - 24 hours only!",
            imageUrl = "https://example.com/banner-flash.jpg",
            targetCountries = listOf("KZ"),
            brandName = "Electronics Hub",
            ctaTitle = mapOf(
                "default" to "Grab Now",
                "en" to "Grab Now",
                "kk" to "Қазір алыңыз",
                "ru" to "Хватайте сейчас",
            ),
            ctaDescription = mapOf(
                "default" to "Limited time only",
                "en" to "Limited time only",
                "kk" to "Шектеулі уақыт ғана",
                "ru" to "Ограниченное время",
            ),
            ctaUrl = "https://example.com/flash-sale",
            isActive = true,
            priority = 10,
            createdAt = System.currentTimeMillis() - THIRTY_MINUTES_AGO,
            updatedAt = System.currentTimeMillis() - THIRTY_MINUTES_AGO,
            expiresAt = System.currentTimeMillis() + ONE_DAY_AGO, // Expires in 1 day
        )
    }

    val expiredBanner by lazy {
        Banner(
            id = BannerId("expired-banner"),
            title = "Black Friday Sale",
            description = "This amazing deal has already ended",
            imageUrl = "https://example.com/banner-expired.jpg",
            targetCountries = listOf("KZ", "RU"),
            brandName = "Mega Store",
            ctaTitle = mapOf(
                "default" to "View Deal",
                "en" to "View Deal",
                "kk" to "Акцияны қарау",
                "ru" to "Смотреть акцию",
            ),
            ctaDescription = mapOf(
                "default" to "Check for similar deals",
                "en" to "Check for similar deals",
                "kk" to "Ұқсас акцияларды тексеріңіз",
                "ru" to "Проверьте похожие акции",
            ),
            ctaUrl = "https://example.com/deals",
            isActive = true,
            priority = 1,
            createdAt = System.currentTimeMillis() - (7 * ONE_DAY_AGO),
            updatedAt = System.currentTimeMillis() - (5 * ONE_DAY_AGO),
            expiresAt = System.currentTimeMillis() - ONE_DAY_AGO, // Expired 1 day ago
        )
    }

    val noImageBanner by lazy {
        Banner(
            id = BannerId("no-image-banner"),
            title = "Special Offer",
            description = "Don't miss out on this amazing deal",
            imageUrl = "", // No image URL
            targetCountries = listOf("KZ", "RU"),
            brandName = "Winter Fashion",
            ctaTitle = mapOf(
                "default" to "Explore Collection",
                "en" to "Explore Collection",
                "kk" to "Жинақты қарау",
                "ru" to "Изучить коллекцию",
            ),
            ctaDescription = mapOf(
                "default" to "Discover amazing styles",
                "en" to "Discover amazing styles",
                "kk" to "Керемет стильдерді табыңыз",
                "ru" to "Откройте удивительные стили",
            ),
            ctaUrl = "https://example.com/winter",
            isActive = true,
            priority = 5,
            createdAt = System.currentTimeMillis() - (3 * ONE_DAY_AGO),
            updatedAt = System.currentTimeMillis() - (2 * ONE_DAY_AGO),
            expiresAt = null, // No expiration
        )
    }

    val globalBanner by lazy {
        Banner(
            id = BannerId("global-banner"),
            title = "Worldwide Free Shipping",
            description = "Free shipping to all countries - no minimum order",
            imageUrl = "https://example.com/banner-global.jpg",
            targetCountries = emptyList(), // Global - empty means worldwide
            brandName = "Global Store",
            ctaTitle = mapOf(
                "default" to "Shop Worldwide",
                "en" to "Shop Worldwide",
                "kk" to "Дүние жүзінде сатып алу",
                "ru" to "Покупки по всему миру",
            ),
            ctaDescription = mapOf(
                "default" to "Available everywhere",
                "en" to "Available everywhere",
                "kk" to "Барлық жерде қолжетімді",
                "ru" to "Доступно везде",
            ),
            ctaUrl = "https://example.com/global-shipping",
            isActive = true,
            priority = 3,
            createdAt = System.currentTimeMillis() - (2 * ONE_DAY_AGO),
            updatedAt = System.currentTimeMillis() - ONE_DAY_AGO,
            expiresAt = System.currentTimeMillis() + (30 * ONE_DAY_AGO), // Expires in 30 days
        )
    }

    val darkModeBanner by lazy {
        Banner(
            id = BannerId("dark-mode-banner"),
            title = "Midnight Sale",
            description = "Exclusive dark mode deals just for you",
            imageUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755543893/gmail-background-xntgf4y7772j0g6i_bbgr2w.jpg",
            targetCountries = listOf("KZ", "RU"),
            brandName = "Night Store",
            ctaTitle = mapOf(
                "default" to "Explore Collection",
                "en" to "Explore Collection",
                "kk" to "Жинақты қарау",
                "ru" to "Изучить коллекцию",
            ),
            ctaDescription = mapOf(
                "default" to "Dark mode exclusive",
                "en" to "Dark mode exclusive",
                "kk" to "Қараңғы режим эксклюзиві",
                "ru" to "Эксклюзив тёмной темы",
            ),
            ctaUrl = "https://example.com/midnight-deals",
            isActive = true,
            priority = 7,
            createdAt = System.currentTimeMillis() - (6 * ONE_HOUR_AGO),
            updatedAt = System.currentTimeMillis() - (3 * ONE_HOUR_AGO),
            expiresAt = System.currentTimeMillis() + (14 * ONE_DAY_AGO), // Expires in 14 days
        )
    }

    val allSampleBanners by lazy {
        listOf(sampleBanner, flashSaleBanner, noImageBanner, globalBanner)
    }
}
