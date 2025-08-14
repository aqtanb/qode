package com.qodein.core.data.util

import com.qodein.core.model.PromoCode
import com.qodein.core.model.UserId
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.days

/**
 * Helper class to generate sample PromoCode data for Firestore initialization
 */
@Singleton
class SampleDataHelper @Inject constructor() {

    /**
     * Creates sample promocodes for various services and categories
     */
    fun createSamplePromoCodes(): List<PromoCode> {
        val now = Clock.System.now()

        return listOf(
            // Netflix promocodes
            PromoCode.createPercentage(
                code = "NETFLIX30",
                serviceName = "Netflix",
                discountPercentage = 30.0,
                maximumDiscount = 15.0,
                category = "Streaming",
                title = "30% off Netflix Premium",
                description = "Get 30% discount on your first 3 months of Netflix Premium subscription",
                createdBy = UserId("user1"),
            ).getOrThrow().copy(
                createdAt = now.toJavaInstant(),
                endDate = now.plus(30.days).toJavaInstant(),
                upvotes = 45,
                downvotes = 3,
                views = 234,
            ),

            PromoCode.createPercentage(
                code = "NETFLIXFREE",
                serviceName = "Netflix",
                discountPercentage = 50.0,
                maximumDiscount = 25.0,
                category = "Streaming",
                title = "Free Netflix for Students",
                description = "Students get 50% off Netflix for 6 months",
                createdBy = UserId("user2"),
            ).getOrThrow().copy(
                createdAt = now.minus(5.days).toJavaInstant(),
                endDate = now.plus(60.days).toJavaInstant(),
                upvotes = 89,
                downvotes = 7,
                views = 567,
            ),

            // Kaspi promocodes
            PromoCode.createFixedAmount(
                code = "KASPI500",
                serviceName = "Kaspi",
                discountAmount = 500.0,
                category = "Shopping",
                title = "500 KZT off Kaspi Shopping",
                description = "Get 500 KZT discount on orders above 5000 KZT",
                createdBy = UserId("user3"),
            ).getOrThrow().copy(
                createdAt = now.minus(2.days).toJavaInstant(),
                endDate = now.plus(14.days).toJavaInstant(),
                upvotes = 156,
                downvotes = 12,
                views = 892,
            ),

            PromoCode.createPercentage(
                code = "KASPIGOLD",
                serviceName = "Kaspi",
                discountPercentage = 15.0,
                maximumDiscount = 5000.0,
                category = "Electronics",
                title = "Kaspi Gold 15% Cashback",
                description = "Special Kaspi Gold members get 15% cashback on electronics",
                createdBy = UserId("user4"),
            ).getOrThrow().copy(
                createdAt = now.minus(7.days).toJavaInstant(),
                endDate = now.plus(21.days).toJavaInstant(),
                upvotes = 203,
                downvotes = 8,
                views = 1024,
            ),

            // Glovo promocodes
            PromoCode.createFixedAmount(
                code = "GLOVO200",
                serviceName = "Glovo",
                discountAmount = 200.0,
                category = "Food Delivery",
                title = "200 KZT off Food Delivery",
                description = "Free delivery + 200 KZT discount on your first order",
                createdBy = UserId("user5"),
            ).getOrThrow().copy(
                createdAt = now.minus(1.days).toJavaInstant(),
                endDate = now.plus(7.days).toJavaInstant(),
                upvotes = 78,
                downvotes = 5,
                views = 345,
            ),

            PromoCode.createPromo(
                code = "GLOVOFREE",
                serviceName = "Glovo",
                description = "Get Glovo Prime membership free for 30 days with unlimited free delivery",
                category = "Food Delivery",
                title = "Free Glovo Prime for 1 Month",
                createdBy = UserId("user6"),
            ).getOrThrow().copy(
                createdAt = now.minus(3.days).toJavaInstant(),
                endDate = now.plus(10.days).toJavaInstant(),
                upvotes = 124,
                downvotes = 9,
                views = 678,
            ),

            // Spotify promocodes
            PromoCode.createPercentage(
                code = "SPOTIFY50",
                serviceName = "Spotify",
                discountPercentage = 50.0,
                maximumDiscount = 10.0,
                category = "Music",
                title = "50% off Spotify Premium",
                description = "Students and new users get 50% off for 3 months",
                createdBy = UserId("user7"),
            ).getOrThrow().copy(
                createdAt = now.minus(4.days).toJavaInstant(),
                endDate = now.plus(45.days).toJavaInstant(),
                upvotes = 267,
                downvotes = 14,
                views = 1456,
            ),

            PromoCode.createPromo(
                code = "SPOTIFYFAMILY",
                serviceName = "Spotify",
                description = "Upgrade to Spotify Family plan for free for 2 months",
                category = "Music",
                title = "Free Spotify Family Upgrade",
                createdBy = UserId("user8"),
            ).getOrThrow().copy(
                createdAt = now.minus(6.days).toJavaInstant(),
                endDate = now.plus(30.days).toJavaInstant(),
                upvotes = 189,
                downvotes = 11,
                views = 987,
            ),

            // McDonald's promocodes
            PromoCode.createFixedAmount(
                code = "MCDONALDS100",
                serviceName = "McDonald's",
                discountAmount = 100.0,
                category = "Fast Food",
                title = "100 KZT off McDonald's",
                description = "Get 100 KZT discount on orders above 1000 KZT",
                createdBy = UserId("user9"),
            ).getOrThrow().copy(
                createdAt = now.minus(1.days).toJavaInstant(),
                endDate = now.plus(5.days).toJavaInstant(),
                upvotes = 92,
                downvotes = 6,
                views = 456,
            ),

            PromoCode.createPromo(
                code = "MCFREEBURGER",
                serviceName = "McDonald's",
                description = "Buy any meal and get a free Big Mac burger",
                category = "Fast Food",
                title = "Free Big Mac with Purchase",
                createdBy = UserId("user10"),
            ).getOrThrow().copy(
                createdAt = now.minus(2.days).toJavaInstant(),
                endDate = now.plus(3.days).toJavaInstant(),
                upvotes = 156,
                downvotes = 8,
                views = 723,
            ),
        )
    }
}
