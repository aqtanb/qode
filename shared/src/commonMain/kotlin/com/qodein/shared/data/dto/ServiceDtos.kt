package com.qodein.shared.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServiceDto(
    val id: String,
    val name: String,
    @SerialName("site_url") val siteUrl: String = "",
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("promocode_count") val promocodeCount: Int = 0
) {
    companion object {
        const val TABLE_NAME = "services"
    }
}

@Serializable
data class ServiceInsertDto(
    val id: String,
    val name: String,
    @SerialName("site_url") val siteUrl: String,
    @SerialName("logo_url") val logoUrl: String?
)
