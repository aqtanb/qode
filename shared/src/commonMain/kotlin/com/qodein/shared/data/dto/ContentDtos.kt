package com.qodein.shared.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BannerDto(
    val id: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("cta_title") val ctaTitle: Map<String, String> = emptyMap(),
    @SerialName("cta_description") val ctaDescription: Map<String, String> = emptyMap(),
    @SerialName("cta_url") val ctaUrl: String? = null,
    val priority: Int = 0
) {
    companion object {
        const val TABLE_NAME = "banners"
    }
}

@Serializable
data class AppUpdateConfigDto(
    @SerialName("minimum_version_code") val minimumVersionCode: Int,
    @SerialName("flexible_update_stale_days") val flexibleUpdateStaleDays: Int
) {
    companion object {
        const val TABLE_NAME = "app_config"
        const val UPDATE_POLICY_ID = "update_policy"
    }
}

@Serializable
data class ReportInsertDto(
    @SerialName("reported_item_id") val reportedItemId: String,
    @SerialName("reported_item_type") val reportedItemType: String,
    @SerialName("reporter_id") val reporterId: String,
    val reason: String,
    @SerialName("additional_details") val additionalDetails: String?
) {
    companion object {
        const val TABLE_NAME = "reports"
    }
}
