package com.qodein.core.ui.preview

import com.qodein.shared.model.Discount
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.ServiceId
import com.qodein.shared.model.UserId
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

/**
 * Centralized sample data for PromoCode previews.
 * Provides consistent test data across all preview functions.
 */
object PromocodePreviewData {

    /**
     * Sample percentage promo code with typical values
     */
    val percentagePromoCode = PromoCode.fromDto(
        id = PromocodeId("glovo_save25"),
        code = "SAVE25",
        discount = Discount.Percentage(25.0),
        minimumOrderAmount = 5000.0,
        startDate = Clock.System.now().minus(1.days),
        endDate = Clock.System.now().plus(7.days),
        authorId = UserId("user123"),
        serviceName = "Glovo",
        description = "Get 25% off your next order with minimum 5000 KZT purchaseGet 25% off your next order with mini" +
            "mum 5000 KZT purchaseGet 25% off your next order with minimum 5000 KZT purchase",
        isFirstUserOnly = false,
        upvotes = 125,
        downvotes = 12,
        isVerified = true,
        serviceId = ServiceId("glovo_kz"),
        serviceLogoUrl = "https://logo.clearbit.com/glovo.com",
        authorUsername = "Алина Жунусова",
        authorAvatarUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/play_store_512_tvjckr.png",
        createdAt = Clock.System.now().minus(2.days),
    )

    /**
     * Sample fixed amount promo code with typical values
     */
    val fixedAmountPromoCode = PromoCode.fromDto(
        id = PromocodeId("kaspi_new1000"),
        code = "NEW1000",
        discount = Discount.FixedAmount(1000.0),
        minimumOrderAmount = 10000.0,
        startDate = Clock.System.now(),
        endDate = Clock.System.now().plus(30.days),
        authorId = UserId("user456"),
        serviceName = "Kaspi.kz",
        description = "1000 KZT discount for new users on first purchase above 10000 KZT",
        isFirstUserOnly = true,
        upvotes = 89,
        downvotes = 5,
        isVerified = true,
        serviceId = ServiceId("kaspi_kz"),
        serviceLogoUrl = "https://logo.clearbit.com/kaspi.kz",
        authorUsername = "Арман Нурланов",
        authorAvatarUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/play_store_512_tvjckr.png",
        createdAt = Clock.System.now().minus(5.days),
    )

    /**
     * Sample promo code that's expiring soon (within 24 hours)
     */
    val expiringSoonPromoCode = PromoCode.fromDto(
        id = PromocodeId("wolt_flash15"),
        code = "FLASH15",
        discount = Discount.Percentage(15.0),
        minimumOrderAmount = 2000.0,
        startDate = Clock.System.now().minus(2.days),
        endDate = Clock.System.now().plus(12.hours), // Expiring in 12 hours
        authorId = UserId("user789"),
        serviceName = "Wolt",
        description = "Flash sale! 15% off all restaurants for limited time",
        isFirstUserOnly = false,
        upvotes = 45,
        downvotes = 3,
        isVerified = false,
        serviceId = ServiceId("wolt_kz"),
        serviceLogoUrl = "https://logo.clearbit.com/wolt.com",
        authorUsername = "Жанар Токтарбекова",
        authorAvatarUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/play_store_512_tvjckr.png",
        createdAt = Clock.System.now().minus(2.days),
    )

    /**
     * Sample promo code that's not active yet (starts in future)
     */
    val notYetActivePromoCode = PromoCode.fromDto(
        id = PromocodeId("technodom_summer500"),
        code = "SUMMER500",
        discount = Discount.FixedAmount(500.0),
        minimumOrderAmount = 50000.0,
        startDate = Clock.System.now().plus(3.days), // Starts in 3 days
        endDate = Clock.System.now().plus(33.days),
        authorId = UserId("user101"),
        serviceName = "Technodom",
        description = "Summer sale! 500 KZT off electronics purchases over 50000 KZT",
        isFirstUserOnly = false,
        upvotes = 0,
        downvotes = 0,
        isVerified = false,
        serviceId = ServiceId("technodom_kz"),
        serviceLogoUrl = "https://logo.clearbit.com/technodom.kz",
        authorUsername = "Ерлан Жұмабайұлы",
        authorAvatarUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/play_store_512_tvjckr.png",
        createdAt = Clock.System.now().minus(1.days),
    )

    /**
     * Sample high-value promo code with many votes
     */
    val highValuePromoCode = PromoCode.fromDto(
        id = PromocodeId("sulpak_mega50"),
        code = "MEGA50",
        discount = Discount.Percentage(50.0),
        minimumOrderAmount = 20000.0,
        startDate = Clock.System.now().minus(3.days),
        endDate = Clock.System.now().plus(4.days),
        authorId = UserId("user202"),
        serviceName = "Sulpak",
        description = "Mega discount! 50% off selected electronics and appliances",
        isFirstUserOnly = true,
        upvotes = 892,
        downvotes = 23,
        isVerified = true,
        serviceId = ServiceId("sulpak_kz"),
        serviceLogoUrl = "https://logo.clearbit.com/sulpak.kz",
        authorUsername = "Айгерим Смағұлова",
        authorAvatarUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/play_store_512_tvjckr.png",
        createdAt = Clock.System.now().minus(4.days),
    )

    /**
     * Sample promo code with minimal engagement
     */
    val lowEngagementPromoCode = PromoCode.fromDto(
        id = PromocodeId("small_shop_save100"),
        code = "SAVE100",
        discount = Discount.FixedAmount(100.0),
        minimumOrderAmount = 500.0,
        startDate = Clock.System.now().minus(1.days),
        endDate = Clock.System.now().plus(14.days),
        authorId = UserId("user303"),
        serviceName = "Local Coffee Shop",
        description = "100 KZT off your coffee order",
        isFirstUserOnly = false,
        upvotes = 2,
        downvotes = 0,
        isVerified = false,
        serviceId = null, // No service association
        serviceLogoUrl = null, // No logo available
        authorUsername = "Дарын Көшербаев",
        authorAvatarUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/play_store_512_tvjckr.png",
        createdAt = Clock.System.now().minus(1.days),
    )

    /**
     * List of all sample promo codes for testing various scenarios
     */
    val allSamples = listOf(
        percentagePromoCode,
        fixedAmountPromoCode,
        expiringSoonPromoCode,
        notYetActivePromoCode,
        highValuePromoCode,
        lowEngagementPromoCode,
    )
}
