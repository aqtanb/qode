package com.qodein.core.ui.preview

import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId

/**
 * Centralized sample data for Service previews.
 * Provides consistent test data across all preview functions.
 */
object ServicePreviewData {

    /**
     * Sample Netflix service
     */
    val netflix = Service(
        id = ServiceId("netflix_entertainment"),
        name = "Netflix",
        category = "Entertainment",
        logoUrl = "https://logo.clearbit.com/netflix.com",
        promoCodeCount = 45,
    )

    /**
     * Sample Kaspi.kz service
     */
    val kaspi = Service(
        id = ServiceId("kaspi_shopping"),
        name = "Kaspi.kz",
        category = "Shopping",
        logoUrl = "https://logo.clearbit.com/kaspi.kz",
        promoCodeCount = 128,
    )

    /**
     * Sample Glovo service
     */
    val glovo = Service(
        id = ServiceId("glovo_food"),
        name = "Glovo",
        category = "Food",
        logoUrl = "https://logo.clearbit.com/glovo.com",
        promoCodeCount = 32,
    )

    /**
     * Sample Yandex service
     */
    val yandex = Service(
        id = ServiceId("yandex_transport"),
        name = "Яндекс Go",
        category = "Transport",
        logoUrl = "https://logo.clearbit.com/go.yandex.kz",
        promoCodeCount = 67,
    )

    /**
     * Sample Technodom service
     */
    val technodom = Service(
        id = ServiceId("technodom_electronics"),
        name = "Technodom",
        category = "Electronics",
        logoUrl = "https://logo.clearbit.com/technodom.kz",
        promoCodeCount = 89,
    )

    /**
     * Sample service with no logo
     */
    val localCoffeeShop = Service(
        id = ServiceId("local_coffee_food"),
        name = "Local Coffee Shop",
        category = "Food",
        logoUrl = null,
        promoCodeCount = 3,
    )

    /**
     * List of all sample services for testing various scenarios
     */
    val allSamples = listOf(
        netflix,
        kaspi,
        glovo,
        yandex,
        technodom,
        localCoffeeShop,
    )

    /**
     * Get a sample service by category for specific testing scenarios
     */
    fun getSampleByCategory(category: String): Service =
        when (category.lowercase()) {
            "entertainment" -> netflix
            "shopping" -> kaspi
            "food" -> glovo
            "transport" -> yandex
            "electronics" -> technodom
            else -> localCoffeeShop
        }

    /**
     * Get a sample service with high promo code count
     */
    val highVolumeService = kaspi

    /**
     * Get a sample service with low promo code count
     */
    val lowVolumeService = localCoffeeShop
}
