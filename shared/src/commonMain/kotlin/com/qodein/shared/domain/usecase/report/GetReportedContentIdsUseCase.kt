package com.qodein.shared.domain.usecase.report

import com.qodein.shared.domain.repository.ReportRepository
import com.qodein.shared.model.ContentType
import kotlinx.coroutines.flow.first

class GetReportedContentIdsUseCase(private val reportRepository: ReportRepository) {
    suspend operator fun invoke(contentType: ContentType): Set<String> = reportRepository.getHiddenContentIds(contentType).first()
}
