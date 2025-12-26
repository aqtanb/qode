package com.qodein.core.data.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class ContentReportDto(
    @DocumentId val documentId: String = "",
    val reportedItemId: String = "",
    val reportedItemType: String = "",
    val reporterId: String = "",
    val reason: String = "",
    val additionalDetails: String? = null,
    val status: String = "PENDING",
    @ServerTimestamp
    val createdAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION_NAME = "reports"
    }
}
