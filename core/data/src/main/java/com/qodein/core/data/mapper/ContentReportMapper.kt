package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.dto.ContentReportDto
import com.qodein.shared.model.ContentReport
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.ReportReason
import com.qodein.shared.model.ReportStatus
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

object ContentReportMapper {
    fun toDto(domain: ContentReport): ContentReportDto =
        ContentReportDto(
            documentId = domain.id,
            reportedItemId = domain.reportedItemId,
            reportedItemType = domain.reportedItemType.name,
            reporterId = domain.reporterId,
            reason = domain.reason.name,
            additionalDetails = domain.additionalDetails,
            status = domain.status.name,
            createdAt = Timestamp(domain.createdAt.toJavaInstant()),
        )

    fun toDomain(dto: ContentReportDto): ContentReport =
        ContentReport(
            id = dto.documentId,
            reportedItemId = dto.reportedItemId,
            reportedItemType = ContentType.valueOf(dto.reportedItemType),
            reporterId = dto.reporterId,
            reason = ReportReason.valueOf(dto.reason),
            additionalDetails = dto.additionalDetails,
            status = ReportStatus.valueOf(dto.status),
            createdAt = dto.createdAt?.toInstant()?.toKotlinInstant()
                ?: error("ContentReport must have createdAt timestamp"),
        )
}
