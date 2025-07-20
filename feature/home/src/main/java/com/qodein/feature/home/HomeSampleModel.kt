package com.qodein.feature.home

import androidx.compose.ui.graphics.Color
import com.qodein.core.ui.component.HeroBannerItem
import com.qodein.core.ui.model.Category
import com.qodein.core.ui.model.PromoCode
import com.qodein.core.ui.model.Store
import com.qodein.core.ui.model.StoreCategory
import java.time.LocalDate
import java.time.LocalDateTime

internal fun getSampleBannerItems(): List<HeroBannerItem> {
    val sampleStore = Store(
        id = "kaspi",
        name = "Kaspi Bank",
        category = StoreCategory.Electronics,
    )

    val sampleCategory = Category(
        id = "electronics",
        name = "Electronics",
    )

    return listOf(
        HeroBannerItem(
            id = "1",
            title = "Weekend Special",
            subtitle = "Up to 50% off electronics",
            description = "Don't miss out on amazing deals this weekend",
            backgroundGradient = listOf(
                Color(0xFF6366F1),
                Color(0xFF8B5CF6),
            ),
            promoCode = PromoCode(
                id = "weekend50",
                code = "WEEKEND50",
                title = "Weekend Special",
                description = "50% off electronics",
                store = sampleStore,
                category = sampleCategory,
                discountPercentage = 50,
                isVerified = true,
                createdAt = LocalDateTime.of(2024, 7, 19, 10, 0),
                expiryDate = LocalDate.of(2024, 7, 25),
            ),
        ),
        HeroBannerItem(
            id = "2",
            title = "New User Bonus",
            subtitle = "20,000 KZT off your first order",
            description = "Welcome to Qode! Start saving immediately",
            backgroundGradient = listOf(
                Color(0xFFEC4899),
                Color(0xFFF97316),
            ),
            actionText = "Get Code",
        ),
        HeroBannerItem(
            id = "3",
            title = "Flash Sale",
            subtitle = "Limited time offers",
            description = "Hurry! These deals won't last long",
            backgroundGradient = listOf(
                Color(0xFF059669),
                Color(0xFF10B981),
            ),
        ),
    )
}

internal fun getSamplePromoCodes(): List<PromoCode> {
    val sampleStore = Store(
        id = "kaspi",
        name = "Kaspi Bank",
        category = StoreCategory.Electronics,
        followersCount = 1250,
    )

    val sampleCategory = Category(
        id = "electronics",
        name = "Electronics",
    )

    return listOf(
        PromoCode(
            id = "1",
            code = "SAVE20",
            title = "20% off all electronics",
            description = "Get amazing discounts on laptops, phones, and more. Limited time offer!",
            store = sampleStore,
            category = sampleCategory,
            discountPercentage = 20,
            minimumOrderAmount = 50000,
            upvotes = 15,
            isVerified = true,
            createdAt = LocalDateTime.now().minusHours(2),
            expiryDate = LocalDate.now().plusDays(5),
        ),
        PromoCode(
            id = "2",
            code = "FIRST50",
            title = "50% off first order",
            description = "Special discount for new customers",
            store = sampleStore.copy(name = "Arbuz.kz", isFollowed = true),
            category = sampleCategory,
            discountPercentage = 50,
            isFirstOrderOnly = true,
            isSingleUse = true,
            upvotes = 8,
            isUpvoted = true,
            createdAt = LocalDateTime.now().minusDays(1),
        ),
        PromoCode(
            id = "3",
            code = "MEGA10K",
            title = "10,000 KZT off orders above 100,000 KZT",
            description = "Huge savings on big orders",
            store = sampleStore.copy(name = "Magnum"),
            category = sampleCategory,
            discountAmount = 10000,
            minimumOrderAmount = 100000,
            upvotes = 25,
            isVerified = true,
            createdAt = LocalDateTime.now().minusHours(6),
        ),
    )
}
