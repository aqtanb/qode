package com.qodein.shared.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ContentReport(
    val id: String,
    val reportedItemId: String,
    val reportedItemType: ContentType,
    val reporterId: String,
    val reason: ReportReason,
    val additionalDetails: String? = null,
    val status: ReportStatus = ReportStatus.PENDING,
    val createdAt: Instant
)

@Serializable
enum class ReportReason {
    SPAM,
    SCAM_OR_MISLEADING,
    INAPPROPRIATE_CONTENT,
    MALICIOUS_LINK,
    OTHER
}

@Serializable
enum class ReportStatus {
    PENDING,
    REVIEWED,
    RESOLVED,
    DISMISSED
}
