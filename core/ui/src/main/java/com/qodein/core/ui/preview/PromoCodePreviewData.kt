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
object PromoCodePreviewData {

    /**
     * Sample percentage promo code with typical values
     */
    val percentagePromoCode = PromoCode(
        id = PromocodeId("glovo_save25"),
        code = "SAVE25",
        discount = Discount.Percentage(25.0),
        serviceId = ServiceId("glovo_kz"),
        serviceName = "Glovo",
        category = "Food Delivery",
        description = "Get 25% off your next order with minimum 5000 KZT purchaseGet 25% off your next order with mini" +
            "mum 5000 KZT purchaseGet 25% off your next order with minimum 5000 KZT purchase",
        minimumOrderAmount = 5000.0,
        startDate = Clock.System.now().minus(1.days),
        endDate = Clock.System.now().plus(7.days),
        isFirstUserOnly = false,
        upvotes = 125,
        downvotes = 12,
        shares = 23,
        targetCountries = listOf("KZ"),
        isVerified = true,
        createdAt = Clock.System.now().minus(2.days),
        createdBy = UserId("user123"),
        createdByUsername = "Алина Жунусова",
        createdByAvatarUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/play_store_512_tvjckr.png",
        serviceLogoUrl = "https://logo.clearbit.com/glovo.com",
    )

    /**
     * Sample fixed amount promo code with typical values
     */
    val fixedAmountPromoCode = PromoCode(
        id = PromocodeId("kaspi_new1000"),
        code = "NEW1000",
        discount = Discount.FixedAmount(1000.0),
        serviceId = ServiceId("kaspi_kz"),
        serviceName = "Kaspi.kz",
        category = "Marketplace",
        description = "1000 KZT discount for new users on first purchase above 10000 KZT",
        minimumOrderAmount = 10000.0,
        startDate = Clock.System.now(),
        endDate = Clock.System.now().plus(30.days),
        isFirstUserOnly = true,
        upvotes = 89,
        downvotes = 5,
        shares = 45,
        targetCountries = listOf("KZ"),
        isVerified = true,
        createdAt = Clock.System.now().minus(5.days),
        createdBy = UserId("user456"),
        createdByUsername = "Арман Нурланов",
        createdByAvatarUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/play_store_512_tvjckr.png",
        serviceLogoUrl = "https://logo.clearbit.com/kaspi.kz",
    )

    /**
     * Sample promo code that's expiring soon (within 24 hours)
     */
    val expiringSoonPromoCode = PromoCode(
        id = PromocodeId("wolt_flash15"),
        code = "FLASH15",
        discount = Discount.Percentage(15.0),
        serviceId = ServiceId("wolt_kz"),
        serviceName = "Wolt",
        category = "Food Delivery",
        description = "Flash sale! 15% off all restaurants for limited time",
        minimumOrderAmount = 2000.0,
        startDate = Clock.System.now().minus(2.days),
        endDate = Clock.System.now().plus(12.hours), // Expiring in 12 hours
        isFirstUserOnly = false,
        upvotes = 45,
        downvotes = 3,
        shares = 12,
        targetCountries = listOf("KZ"),
        isVerified = false,
        createdAt = Clock.System.now().minus(2.days),
        createdBy = UserId("user789"),
        createdByUsername = "Жанар Токтарбекова",
        createdByAvatarUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/play_store_512_tvjckr.png",
        serviceLogoUrl = "https://logo.clearbit.com/wolt.com",
    )

    /**
     * Sample promo code that's not active yet (starts in future)
     */
    val notYetActivePromoCode = PromoCode(
        id = PromocodeId("technodom_summer500"),
        code = "SUMMER500",
        discount = Discount.FixedAmount(500.0),
        serviceId = ServiceId("technodom_kz"),
        serviceName = "Technodom",
        category = "Electronics",
        description = "Summer sale! 500 KZT off electronics purchases over 50000 KZT",
        minimumOrderAmount = 50000.0,
        startDate = Clock.System.now().plus(3.days), // Starts in 3 days
        endDate = Clock.System.now().plus(33.days),
        isFirstUserOnly = false,
        upvotes = 0,
        downvotes = 0,
        shares = 0,
        targetCountries = listOf("KZ"),
        isVerified = false,
        createdAt = Clock.System.now().minus(1.days),
        createdBy = UserId("user101"),
        createdByUsername = "Ерлан Жұмабайұлы",
        createdByAvatarUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/play_store_512_tvjckr.png",
        serviceLogoUrl = "https://logo.clearbit.com/technodom.kz",
    )

    /**
     * Sample high-value promo code with many votes
     */
    val highValuePromoCode = PromoCode(
        id = PromocodeId("sulpak_mega50"),
        code = "MEGA50",
        discount = Discount.Percentage(50.0),
        serviceId = ServiceId("sulpak_kz"),
        serviceName = "Sulpak",
        category = "Electronics",
        description = "Mega discount! 50% off selected electronics and appliances",
        minimumOrderAmount = 20000.0,
        startDate = Clock.System.now().minus(3.days),
        endDate = Clock.System.now().plus(4.days),
        isFirstUserOnly = true,
        upvotes = 892,
        downvotes = 23,
        shares = 234,
        targetCountries = listOf("KZ"),
        isVerified = true,
        createdAt = Clock.System.now().minus(4.days),
        createdBy = UserId("user202"),
        createdByUsername = "Айгерим Смағұлова",
        createdByAvatarUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/play_store_512_tvjckr.png",
        serviceLogoUrl = "https://logo.clearbit.com/sulpak.kz",
    )

    /**
     * Sample promo code with minimal engagement
     */
    val lowEngagementPromoCode = PromoCode(
        id = PromocodeId("small_shop_save100"),
        code = "SAVE100",
        discount = Discount.FixedAmount(100.0),
        serviceId = null, // No service association
        serviceName = "Local Coffee Shop",
        category = "Food & Beverage",
        description = "100 KZT off your coffee order",
        minimumOrderAmount = 500.0,
        startDate = Clock.System.now().minus(1.days),
        endDate = Clock.System.now().plus(14.days),
        isFirstUserOnly = false,
        upvotes = 2,
        downvotes = 0,
        shares = 1,
        targetCountries = listOf("KZ"),
        isVerified = false,
        createdAt = Clock.System.now().minus(1.days),
        createdBy = UserId("user303"),
        createdByUsername = "Дарын Көшербаев",
        createdByAvatarUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/play_store_512_tvjckr.png",
        serviceLogoUrl = null, // No logo available
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

    /**
     * Get a sample promo code by type for specific testing scenarios
     */
    fun getSampleByType(type: PromoCodeType): PromoCode =
        when (type) {
            PromoCodeType.PERCENTAGE -> percentagePromoCode
            PromoCodeType.FIXED_AMOUNT -> fixedAmountPromoCode
            PromoCodeType.EXPIRING_SOON -> expiringSoonPromoCode
            PromoCodeType.NOT_YET_ACTIVE -> notYetActivePromoCode
            PromoCodeType.HIGH_VALUE -> highValuePromoCode
            PromoCodeType.LOW_ENGAGEMENT -> lowEngagementPromoCode
        }

    enum class PromoCodeType {
        PERCENTAGE,
        FIXED_AMOUNT,
        EXPIRING_SOON,
        NOT_YET_ACTIVE,
        HIGH_VALUE,
        LOW_ENGAGEMENT
    }
}
