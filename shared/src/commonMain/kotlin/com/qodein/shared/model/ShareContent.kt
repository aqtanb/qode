package com.qodein.shared.model

/**
 * Domain model representing content to be shared
 * Platform-agnostic, can be used in any layer
 */
data class ShareContent(val title: String, val text: String, val url: String)
