package com.qodein.core.ui.preview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.ui.graphics.Color
import com.qodein.core.ui.model.Category
import com.qodein.core.ui.model.Comment
import com.qodein.core.ui.model.PromoCode
import com.qodein.core.ui.model.Store
import com.qodein.core.ui.model.StoreCategory
import com.qodein.core.ui.model.User
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Preview data factory for consistent sample data across components
 */
object PreviewData {

    // Sample Users
    val sampleUsers = listOf(
        User(
            id = "user1",
            username = "AkezhanB",
            email = "akezhan@example.com",
            avatarUrl = null,
            totalUpvotes = 156,
            submittedPromoCodes = 23,
            followedStores = listOf("kaspi", "arbuz", "magnum"),
            followedCategories = listOf("electronics", "food"),
            joinedAt = LocalDateTime.now().minusMonths(3),
        ),
        User(
            id = "user2",
            username = "MaratK",
            email = "marat@example.com",
            avatarUrl = null,
            totalUpvotes = 89,
            submittedPromoCodes = 12,
            followedStores = listOf("kaspi", "technodom"),
            followedCategories = listOf("electronics", "sports"),
            joinedAt = LocalDateTime.now().minusMonths(6),
        ),
        User(
            id = "user3",
            username = "DinaraS",
            email = "dinara@example.com",
            avatarUrl = null,
            totalUpvotes = 234,
            submittedPromoCodes = 45,
            followedStores = listOf("arbuz", "magnum", "sulpak"),
            followedCategories = listOf("food", "beauty", "home"),
            joinedAt = LocalDateTime.now().minusMonths(8),
        ),
        User(
            id = "user4",
            username = "AslanN",
            email = "aslan@example.com",
            avatarUrl = null,
            totalUpvotes = 67,
            submittedPromoCodes = 8,
            followedStores = listOf("wildberries", "ozon"),
            followedCategories = listOf("fashion", "electronics"),
            joinedAt = LocalDateTime.now().minusWeeks(3),
        ),
    )

    // Sample Stores
    val sampleStores = listOf(
        Store(
            id = "kaspi",
            name = "Kaspi Bank",
            logo = null,
            website = "https://kaspi.kz",
            category = StoreCategory.Electronics,
            followersCount = 15420,
            isFollowed = false,
        ),
        Store(
            id = "arbuz",
            name = "Arbuz.kz",
            logo = null,
            website = "https://arbuz.kz",
            category = StoreCategory.Food,
            followersCount = 8630,
            isFollowed = true,
        ),
        Store(
            id = "magnum",
            name = "Magnum",
            logo = null,
            website = "https://magnum.kz",
            category = StoreCategory.Food,
            followersCount = 12100,
            isFollowed = false,
        ),
        Store(
            id = "mechta",
            name = "Mechta",
            logo = null,
            website = "https://mechta.kz",
            category = StoreCategory.Electronics,
            followersCount = 7890,
            isFollowed = true,
        ),
        Store(
            id = "sulpak",
            name = "Sulpak",
            logo = null,
            website = "https://sulpak.kz",
            category = StoreCategory.Electronics,
            followersCount = 9540,
            isFollowed = false,
        ),
        Store(
            id = "technodom",
            name = "Technodom",
            logo = null,
            website = "https://technodom.kz",
            category = StoreCategory.Electronics,
            followersCount = 6720,
            isFollowed = false,
        ),
        Store(
            id = "wildberries",
            name = "Wildberries",
            logo = null,
            website = "https://wildberries.kz",
            category = StoreCategory.Fashion,
            followersCount = 11230,
            isFollowed = true,
        ),
        Store(
            id = "ozon",
            name = "Ozon",
            logo = null,
            website = "https://ozon.kz",
            category = StoreCategory.Other,
            followersCount = 5890,
            isFollowed = false,
        ),
    )

    // Sample Categories
    val sampleCategories = listOf(
        Category(
            id = "electronics",
            name = "Electronics & Tech",
            icon = Icons.Default.ElectricBolt,
            color = Color(0xFF2196F3),
            followersCount = 5420,
            isFollowed = true,
        ),
        Category(
            id = "fashion",
            name = "Fashion & Clothing",
            icon = Icons.Default.Face,
            color = Color(0xFFE91E63),
            followersCount = 3210,
            isFollowed = false,
        ),
        Category(
            id = "food",
            name = "Food & Drinks",
            icon = Icons.Default.LocalDining,
            color = Color(0xFF4CAF50),
            followersCount = 4560,
            isFollowed = true,
        ),
        Category(
            id = "beauty",
            name = "Beauty & Health",
            icon = Icons.Default.Face,
            color = Color(0xFF9C27B0),
            followersCount = 2840,
            isFollowed = false,
        ),
        Category(
            id = "sports",
            name = "Sports & Outdoor",
            icon = Icons.Default.FitnessCenter,
            color = Color(0xFFFF9800),
            followersCount = 1950,
            isFollowed = false,
        ),
        Category(
            id = "home",
            name = "Home & Garden",
            icon = Icons.Default.Home,
            color = Color(0xFF795548),
            followersCount = 1420,
            isFollowed = true,
        ),
        Category(
            id = "books",
            name = "Books & Education",
            icon = Icons.Default.MenuBook,
            color = Color(0xFF607D8B),
            followersCount = 890,
            isFollowed = false,
        ),
        Category(
            id = "travel",
            name = "Travel & Tourism",
            icon = Icons.Default.Flight,
            color = Color(0xFF00BCD4),
            followersCount = 1230,
            isFollowed = false,
        ),
    )

    // Sample Promo Codes
    val samplePromoCodes = listOf(
        PromoCode(
            id = "promo1",
            code = "SAVE20KZT",
            title = "20% off all electronics",
            description = "Get amazing discounts on laptops, phones,",
            store = sampleStores[0], // Kaspi Bank
            category = sampleCategories[0], // Electronics
            discountPercentage = 20,
            minimumOrderAmount = 50000,
            isFirstOrderOnly = false,
            isSingleUse = false,
            expiryDate = LocalDate.now().plusDays(5),
            upvotes = 15,
            isVerified = true,
            submittedBy = sampleUsers[0],
            createdAt = LocalDateTime.now().minusHours(2),
            isUpvoted = true,
            isUsed = false,
        ),
        PromoCode(
            id = "promo2",
            code = "FIRST50",
            title = "50% off first order",
            description = "Special discount for new customers. Perfect for trying ",
            store = sampleStores[1], // Arbuz.kz
            category = sampleCategories[2], // Food
            discountPercentage = 50,
            minimumOrderAmount = 15000,
            isFirstOrderOnly = true,
            isSingleUse = true,
            expiryDate = LocalDate.now().plusDays(15),
            upvotes = 8,
            isVerified = false,
            submittedBy = sampleUsers[1],
            createdAt = LocalDateTime.now().minusDays(1),
            isUpvoted = false,
            isUsed = false,
        ),
        PromoCode(
            id = "promo3",
            code = "MEGA10K",
            title = "10,000 KZT off orders above 100,000 KZT",
            description = "Huge savings on big orders. Perfect for stocking up on g.",
            store = sampleStores[2], // Magnum
            category = sampleCategories[2], // Food
            discountAmount = 10000,
            minimumOrderAmount = 100000,
            isFirstOrderOnly = false,
            isSingleUse = false,
            expiryDate = LocalDate.now().plusDays(10),
            upvotes = 25,
            isVerified = true,
            submittedBy = sampleUsers[2],
            createdAt = LocalDateTime.now().minusHours(6),
            isUpvoted = false,
            isUsed = false,
        ),
        PromoCode(
            id = "promo4",
            code = "WELCOME25",
            title = "25% off welcome bonus",
            description = "New to our platform? Get 25% off your first tech purchase!",
            store = sampleStores[3], // Mechta
            category = sampleCategories[0], // Electronics
            discountPercentage = 25,
            minimumOrderAmount = 30000,
            isFirstOrderOnly = true,
            isSingleUse = true,
            expiryDate = null, // No expiry
            upvotes = 12,
            isVerified = true,
            submittedBy = sampleUsers[0],
            createdAt = LocalDateTime.now().minusHours(12),
            isUpvoted = true,
            isUsed = true,
        ),
        PromoCode(
            id = "promo5",
            code = "FASHION15",
            title = "15% off fashion items",
            description = "Trendy clothes at unbeatable prices. From casual wear to fo",
            store = sampleStores[6], // Wildberries
            category = sampleCategories[1], // Fashion
            discountPercentage = 15,
            minimumOrderAmount = 20000,
            isFirstOrderOnly = false,
            isSingleUse = false,
            expiryDate = LocalDate.now().plusDays(30),
            upvotes = 19,
            isVerified = true,
            submittedBy = sampleUsers[3],
            createdAt = LocalDateTime.now().minusDays(3),
            isUpvoted = false,
            isUsed = false,
        ),
        PromoCode(
            id = "promo6",
            code = "EXPIRED50",
            title = "50% off summer sale",
            description = "This was a great deal but unfortunately it has expired. Keep an eye o",
            store = sampleStores[4], // Sulpak
            category = sampleCategories[0], // Electronics
            discountPercentage = 50,
            minimumOrderAmount = 25000,
            isFirstOrderOnly = false,
            isSingleUse = false,
            expiryDate = LocalDate.now().minusDays(2), // Expired
            upvotes = 45,
            isVerified = true,
            submittedBy = sampleUsers[1],
            createdAt = LocalDateTime.now().minusDays(10),
            isUpvoted = false,
            isUsed = false,
        ),
    )

    // Sample Comments
    val sampleComments = listOf(
        Comment(
            id = "comment1",
            promoCodeId = "promo1",
            userId = "user1",
            username = "AkezhanB",
            userAvatarUrl = null,
            parentCommentId = null,
            content = "This code worked perfectly! Saved 15,000 KZT on my laptop pu",
            upvotes = 12,
            isUpvoted = true,
            createdAt = LocalDateTime.now().minusHours(2),
            replies = listOf(
                Comment(
                    id = "comment2",
                    promoCodeId = "promo1",
                    userId = "user2",
                    username = "MaratK",
                    userAvatarUrl = null,
                    parentCommentId = "comment1",
                    content = "Which laptop did you get? I'm looking for a good deal too.",
                    upvotes = 3,
                    isUpvoted = false,
                    createdAt = LocalDateTime.now().minusHours(1),
                    replies = listOf(
                        Comment(
                            id = "comment3",
                            promoCodeId = "promo1",
                            userId = "user1",
                            username = "AkezhanB",
                            userAvatarUrl = null,
                            parentCommentId = "comment2",
                            content = "Got the MacBook Air M2. Check their electronics section!",
                            upvotes = 1,
                            isUpvoted = false,
                            createdAt = LocalDateTime.now().minusMinutes(30),
                        ),
                    ),
                ),
            ),
        ),
        Comment(
            id = "comment4",
            promoCodeId = "promo1",
            userId = "user3",
            username = "DinaraS",
            userAvatarUrl = null,
            parentCommentId = null,
            content = "Code expired yesterday, but the store support helped me get the di",
            upvotes = 8,
            isUpvoted = false,
            createdAt = LocalDateTime.now().minusHours(6),
        ),
        Comment(
            id = "comment5",
            promoCodeId = "promo2",
            userId = "user4",
            username = "AslanN",
            userAvatarUrl = null,
            parentCommentId = null,
            content = "Perfect for trying their grocery delivery. Fresh vegetable",
            upvotes = 5,
            isUpvoted = true,
            createdAt = LocalDateTime.now().minusHours(4),
        ),
    )

    // Utility functions for getting sample data
    fun getRandomUser() = sampleUsers.random()

    fun getRandomStore() = sampleStores.random()

    fun getRandomCategory() = sampleCategories.random()

    fun getRandomPromoCode() = samplePromoCodes.random()

    fun getRandomComment() = sampleComments.random()

    fun getPopularStores() = sampleStores.sortedByDescending { it.followersCount }.take(5)

    fun getFollowedStores() = sampleStores.filter { it.isFollowed }

    fun getFollowedCategories() = sampleCategories.filter { it.isFollowed }

    fun getRecentPromoCodes() = samplePromoCodes.sortedByDescending { it.createdAt }.take(10)

    fun getVerifiedPromoCodes() = samplePromoCodes.filter { it.isVerified }

    fun getExpiredPromoCodes() =
        samplePromoCodes.filter {
            it.expiryDate?.isBefore(LocalDate.now()) == true
        }

    fun getActivePromoCodes() =
        samplePromoCodes.filter {
            it.expiryDate?.isAfter(LocalDate.now()) != false
        }

    fun getPromoCodesByStore(storeId: String) =
        samplePromoCodes.filter {
            it.store.id == storeId
        }

    fun getPromoCodesByCategory(categoryId: String) =
        samplePromoCodes.filter {
            it.category.id == categoryId
        }

    fun getCommentsByPromoCode(promoCodeId: String) =
        sampleComments.filter {
            it.promoCodeId == promoCodeId
        }

    fun getUserStats(userId: String): Map<String, Int> {
        val user = sampleUsers.find { it.id == userId }
        val userPromoCodes = samplePromoCodes.filter { it.submittedBy?.id == userId }
        val totalUpvotes = userPromoCodes.sumOf { it.upvotes }

        return mapOf(
            "submittedCodes" to userPromoCodes.size,
            "totalUpvotes" to totalUpvotes,
            "verifiedCodes" to userPromoCodes.count { it.isVerified },
            "followedStores" to (user?.followedStores?.size ?: 0),
            "followedCategories" to (user?.followedCategories?.size ?: 0),
        )
    }

    // Sample data for specific use cases
    fun getOnboardingStores() =
        listOf(
            sampleStores[0], // Kaspi Bank
            sampleStores[1], // Arbuz.kz
            sampleStores[2], // Magnum
            sampleStores[6], // Wildberries
        )

    fun getOnboardingCategories() =
        listOf(
            sampleCategories[0], // Electronics
            sampleCategories[2], // Food
            sampleCategories[1], // Fashion
            sampleCategories[3], // Beauty
        )

    fun getTrendingData() =
        mapOf(
            "totalCodes" to samplePromoCodes.size,
            "activeUsers" to sampleUsers.size,
            "totalSavings" to "2.5M ₸",
            "thisWeekCodes" to 45,
            "thisWeekUsers" to 156,
            "popularStore" to sampleStores[0].name,
            "popularCategory" to sampleCategories[2].name,
        )

    // Mock network responses
    fun getLoadingPromoCodes(count: Int = 3) =
        List(count) { index ->
            samplePromoCodes[index % samplePromoCodes.size].copy(
                id = "loading_$index",
                title = "Loading...",
            )
        }

    fun getErrorPromoCode() =
        samplePromoCodes[0].copy(
            id = "error",
            title = "Failed to load",
            description = "Please try again",
        )

    // Search and filter helpers
    fun searchPromoCodes(query: String) =
        samplePromoCodes.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.code.contains(query, ignoreCase = true) ||
                it.store.name.contains(query, ignoreCase = true)
        }

    fun filterPromoCodesByCategory(categoryId: String?) =
        when (categoryId) {
            null -> samplePromoCodes
            else -> samplePromoCodes.filter { it.category.id == categoryId }
        }

    fun sortPromoCodes(
        codes: List<PromoCode>,
        sortBy: String
    ) = when (sortBy) {
        "recent" -> codes.sortedByDescending { it.createdAt }
        "popular" -> codes.sortedByDescending { it.upvotes }
        "expiring" -> codes.sortedBy { it.expiryDate }
        "discount" -> codes.sortedByDescending {
            it.discountPercentage ?: (it.discountAmount?.div(1000) ?: 0)
        }
        else -> codes
    }
}
