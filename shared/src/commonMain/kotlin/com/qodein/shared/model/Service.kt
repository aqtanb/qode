package com.qodein.shared.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ServiceId(val value: String) {
    init {
        require(value.isNotBlank()) { "Service ID cannot be blank" }
    }
}

@Serializable
data class Service(
    val id: ServiceId, // We gotta use document id consisting of category_servicename, lowercase, sanitized
    val name: String,
    val category: String,
    val logoUrl: String? = null,
    val isPopular: Boolean = false,
    val promoCodeCount: Int = 0, // Denormalized counter for display
    val createdAt: Instant = Clock.System.now()
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
            isPopular: Boolean = false,
            promoCodeCount: Int = 0
        ): Service {
            val id = ServiceId(generateServiceId(name, category))
            return Service(
                id = id,
                name = name.trim(),
                category = category.trim(),
                logoUrl = logoUrl,
                isPopular = isPopular,
                promoCodeCount = promoCodeCount,
            )
        }

        private fun generateServiceId(
            name: String,
            category: String
        ): String {
            val sanitizedName = name.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
            val sanitizedCategory = category.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
            return "${sanitizedCategory}_$sanitizedName"
        }

        // Predefined categories for consistency - single words only
        object Categories {
            const val STREAMING = "Streaming"
            const val FOOD = "Food"
            const val TRANSPORT = "Transport"
            const val SHOPPING = "Shopping"
            const val GAMING = "Gaming"
            const val MUSIC = "Music"
            const val EDUCATION = "Education"
            const val FITNESS = "Fitness"
            const val FINANCE = "Finance"
            const val BEAUTY = "Beauty"
            const val CLOTHING = "Clothing"
            const val ELECTRONICS = "Electronics"
            const val TRAVEL = "Travel"
            const val PHARMACY = "Pharmacy"
            const val OTHER = "Other"

            val ALL = listOf(
                STREAMING, FOOD, TRANSPORT, SHOPPING, GAMING, MUSIC,
                EDUCATION, FITNESS, FINANCE, BEAUTY, CLOTHING,
                ELECTRONICS, TRAVEL, PHARMACY, OTHER,
            )
        }
    }
}
