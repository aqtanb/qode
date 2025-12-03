package com.qodein.core.ui.preview

import com.qodein.shared.common.Result
import com.qodein.shared.model.Service

/**
 * Centralized sample data for Service previews.
 * Provides consistent test data across all preview functions.
 */
object ServicePreviewData {

    /**
     * Sample Netflix service
     */
    val netflix by lazy {
        (Service.create(name = "Netflix", siteUrl = "netflix.com") as Result.Success).data
    }

    /**
     * Sample Kaspi.kz service
     */
    val kaspi by lazy {
        (Service.create(name = "Kaspi.kz", siteUrl = "kaspi.kz") as Result.Success).data
    }

    /**
     * Sample Glovo service
     */
    val glovo by lazy {
        (Service.create(name = "Glovo", siteUrl = "glovoapp.com") as Result.Success).data
    }

    /**
     * Sample Yandex service
     */
    val yandex by lazy {
        (Service.create(name = "Яндекс Go", siteUrl = "go.yandex") as Result.Success).data
    }

    /**
     * Sample Technodom service
     */
    val technodom by lazy {
        (Service.create(name = "Technodom", siteUrl = "technodom.kz") as Result.Success).data
    }

    /**
     * Sample service with no logo
     */
    val localCoffeeShop by lazy {
        (Service.create(name = "Local Coffee Shop", siteUrl = "localcoffee.test") as Result.Success).data
    }

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
}
