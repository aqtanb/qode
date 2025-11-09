package com.qodein.core.ui.preview

import com.qodein.shared.model.Banner
import com.qodein.shared.model.BannerId

/**
 * Centralized sample data for Banner previews.
 * Provides consistent test data across all preview functions.
 */
object BannerPreviewData {

    /**
     * Sample banner with full multilingual content
     */
    val standardBanner = Banner(
        id = BannerId("banner_promocode_boost"),
        imageUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/banner_promocode.jpg",
        ctaTitle = mapOf(
            "default" to "Discover Hot Deals",
            "en" to "Discover Hot Deals",
            "kk" to "Ыстық ұсыныстарды табыңыз",
            "ru" to "Откройте горячие предложения",
        ),
        ctaDescription = mapOf(
            "default" to "Save big with verified promo codes",
            "en" to "Save big with verified promo codes",
            "kk" to "Тексерілген промокодтармен үнемдеңіз",
            "ru" to "Экономьте с проверенными промокодами",
        ),
        ctaUrl = "qode://promocodes",
        priority = 100,
    )

    /**
     * Sample banner with English-only content
     */
    val englishOnlyBanner = Banner(
        id = BannerId("banner_new_feature"),
        imageUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/banner_feature.jpg",
        ctaTitle = mapOf(
            "default" to "Share Your Codes",
            "en" to "Share Your Codes",
        ),
        ctaDescription = mapOf(
            "default" to "Help others save money by sharing promo codes",
            "en" to "Help others save money by sharing promo codes",
        ),
        ctaUrl = "qode://promocode/submit",
        priority = 90,
    )

    /**
     * Sample banner with no URL (non-clickable)
     */
    val noUrlBanner = Banner(
        id = BannerId("banner_announcement"),
        imageUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/banner_announcement.jpg",
        ctaTitle = mapOf(
            "default" to "Welcome to Qode",
            "en" to "Welcome to Qode",
            "kk" to "Qode-ке қош келдіңіз",
            "ru" to "Добро пожаловать в Qode",
        ),
        ctaDescription = mapOf(
            "default" to "Your source for the best promo codes in Kazakhstan",
            "en" to "Your source for the best promo codes in Kazakhstan",
            "kk" to "Қазақстандағы ең жақсы промокодтар көзі",
            "ru" to "Ваш источник лучших промокодов в Казахстане",
        ),
        ctaUrl = null,
        priority = 80,
    )

    /**
     * Sample banner with high priority
     */
    val highPriorityBanner = Banner(
        id = BannerId("banner_flash_sale"),
        imageUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/banner_flash_sale.jpg",
        ctaTitle = mapOf(
            "default" to "Flash Sale",
            "en" to "Flash Sale",
            "kk" to "Жылдам сату",
            "ru" to "Молниеносная распродажа",
        ),
        ctaDescription = mapOf(
            "default" to "Limited time offer - Act now!",
            "en" to "Limited time offer - Act now!",
            "kk" to "Шектеулі уақыт ұсынысы - Қазір әрекет етіңіз!",
            "ru" to "Предложение ограничено по времени - Действуйте сейчас!",
        ),
        ctaUrl = "qode://promocodes?filter=trending",
        priority = 150,
    )

    /**
     * Sample banner with low priority
     */
    val lowPriorityBanner = Banner(
        id = BannerId("banner_info"),
        imageUrl = "https://res.cloudinary.com/dzbq1jcvr/image/upload/v1755544080/banner_info.jpg",
        ctaTitle = mapOf(
            "default" to "Did You Know?",
            "en" to "Did You Know?",
            "kk" to "Білесіз бе?",
            "ru" to "Знаете ли вы?",
        ),
        ctaDescription = mapOf(
            "default" to "Tips and tricks to maximize your savings",
            "en" to "Tips and tricks to maximize your savings",
            "kk" to "Үнемдеуді арттыру үшін кеңестер",
            "ru" to "Советы по максимальной экономии",
        ),
        ctaUrl = "qode://tips",
        priority = 50,
    )

    /**
     * Sample banner with missing image URL (for error testing)
     */
    val noImageBanner = Banner(
        id = BannerId("banner_no_image"),
        imageUrl = "",
        ctaTitle = mapOf(
            "default" to "Image Coming Soon",
            "en" to "Image Coming Soon",
        ),
        ctaDescription = mapOf(
            "default" to "Check back later for updates",
            "en" to "Check back later for updates",
        ),
        ctaUrl = null,
        priority = 70,
    )

    /**
     * List of all sample banners sorted by priority (highest first)
     */
    val allSamples = listOf(
        highPriorityBanner,
        standardBanner,
        englishOnlyBanner,
        noUrlBanner,
        lowPriorityBanner,
        noImageBanner,
    )
}
