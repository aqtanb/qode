package com.qodein.shared.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class ServiceId(val value: String)

@Serializable
data class Service(
    val id: ServiceId, // Document ID format: servicename_category, lowercase, sanitized
    val name: String,
    val category: String,
    val logoUrl: String? = null,
    val promoCodeCount: Int = 0, // Denormalized counter for display
    @Contextual val createdAt: Instant = Clock.System.now()
) {
    init {
        require(name.isNotBlank()) { "Service name cannot be blank" }
        require(category.isNotBlank()) { "Service category cannot be blank" }
    }

    companion object {
        fun create(
            name: String,
            category: String,
            logoUrl: String? = null,
            promoCodeCount: Int = 0
        ): Service {
            val id = ServiceId(generateServiceId(name, category))
            return Service(
                id = id,
                name = name.trim(),
                category = category.trim(),
                logoUrl = logoUrl,
                promoCodeCount = promoCodeCount,
            )
        }

        private fun generateServiceId(
            name: String,
            category: String
        ): String {
            val sanitizedName = name.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
            val sanitizedCategory = category.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
            return "${sanitizedName}_$sanitizedCategory"
        }

        // Consolidated categories for Kazakhstan market
        object Categories {
            const val ENTERTAINMENT = "Entertainment" // STREAMING + GAMING + MUSIC + ENTERTAINMENT
            const val FOOD = "Food"
            const val TRANSPORT = "Transport"
            const val SHOPPING = "Shopping" // SHOPPING + MARKETPLACE
            const val EDUCATION = "Education"
            const val FITNESS = "Fitness"
            const val BEAUTY = "Beauty"
            const val CLOTHING = "Clothing"
            const val ELECTRONICS = "Electronics"
            const val TRAVEL = "Travel"
            const val JEWELRY = "Jewelry"
            const val OTHER = "Other"
            const val UNSPECIFIED = "Unspecified"

            val ALL = listOf(
                BEAUTY, CLOTHING, EDUCATION, ELECTRONICS, ENTERTAINMENT, FITNESS, FOOD, JEWELRY,
                SHOPPING, TRANSPORT, TRAVEL, OTHER, UNSPECIFIED,
            )
        }
    }
}
