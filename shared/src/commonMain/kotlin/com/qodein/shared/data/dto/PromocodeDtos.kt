package com.qodein.shared.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Row of `promocodes_view`: a promocode joined with its service and author. */
@Serializable
data class PromocodeDto(
    val id: String,
    val code: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    @SerialName("discount_type") val discountType: String,
    @SerialName("discount_value") val discountValue: Double? = null,
    @SerialName("free_item_description") val freeItemDescription: String? = null,
    @SerialName("minimum_order_amount") val minimumOrderAmount: Double = 0.0,
    val description: String? = null,
    @SerialName("image_urls") val imageUrls: List<String> = emptyList(),
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    @SerialName("vote_score") val voteScore: Int = 0,
    val verified: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    @SerialName("service_id") val serviceId: String,
    @SerialName("service_name") val serviceName: String,
    @SerialName("service_logo_url") val serviceLogoUrl: String? = null,
    @SerialName("service_site_url") val serviceSiteUrl: String? = null,
    @SerialName("author_id") val authorId: String,
    @SerialName("author_name") val authorName: String? = null,
    @SerialName("author_avatar_url") val authorAvatarUrl: String? = null
) {
    companion object {
        const val VIEW_NAME = "promocodes_view"
        const val TABLE_NAME = "promocodes"
        const val TYPE_PERCENTAGE = "Percentage"
        const val TYPE_FIXED_AMOUNT = "FixedAmount"
        const val TYPE_FREE_ITEM = "FreeItem"
    }
}

/** Only the columns clients may write. Votes and `verified` stay server-owned. */
@Serializable
data class PromocodeInsertDto(
    val id: String,
    val code: String,
    @SerialName("service_id") val serviceId: String,
    @SerialName("author_id") val authorId: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    @SerialName("discount_type") val discountType: String,
    @SerialName("discount_value") val discountValue: Double?,
    @SerialName("free_item_description") val freeItemDescription: String?,
    @SerialName("minimum_order_amount") val minimumOrderAmount: Double,
    val description: String?,
    @SerialName("image_urls") val imageUrls: List<String>
)
