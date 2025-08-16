package com.qodein.core.data.model

import com.google.firebase.firestore.PropertyName

data class ServiceDto(
    @PropertyName("id") val id: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("category") val category: String = "",
    @PropertyName("logoUrl") val logoUrl: String? = null,
    @PropertyName("isPopular") val isPopular: Boolean = false,
    @PropertyName("createdAt") val createdAt: Long = 0L // Unix timestamp for Firestore compatibility
) {
    // No-args constructor required for Firestore deserialization
    constructor() : this("", "", "", null, false, 0L)
}
