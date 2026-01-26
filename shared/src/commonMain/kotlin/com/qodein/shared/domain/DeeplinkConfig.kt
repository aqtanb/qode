package com.qodein.shared.domain

/**
 * Configuration for deeplink URLs
 * Centralized management of base URLs
 */
data class DeeplinkConfig(val webBaseUrl: String = "https://qodein.web.app", val appScheme: String = "qodein") {
    fun getPostWebUrl(postId: String) = "$webBaseUrl/posts/$postId"
    fun getPostAppUrl(postId: String) = "$appScheme://post/$postId"

    fun getPromocodeWebUrl(promocodeId: String) = "$webBaseUrl/promocodes/$promocodeId"
    fun getPromocodeAppUrl(promocodeId: String) = "$appScheme://promocode/$promocodeId"
}
