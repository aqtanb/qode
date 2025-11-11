package com.qodein.shared.model

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class ServiceId(val value: String) {
    init {
        require(value.isNotBlank()) { "Service ID cannot be blank" }
    }
    override fun toString(): String = value
}

@Serializable
data class Service(
    val id: ServiceId, // Document ID format: servicename_category, lowercase, sanitized
    val name: String,
    val logoUrl: String? = null,
    val promoCodeCount: Int = 0 // Denormalized counter for display
) {
    init {
        require(name.isNotBlank()) { "Service name cannot be blank" }
        require(promoCodeCount >= 0) { "Promo code count cannot be negative" }
    }
}
