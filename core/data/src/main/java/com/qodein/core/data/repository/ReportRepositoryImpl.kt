package com.qodein.core.data.repository

import com.qodein.core.data.datasource.LocalReportDataSource
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.data.datasource.SupabaseReportDataSource
import com.qodein.shared.data.mapper.toInsertDto
import com.qodein.shared.data.util.remoteCall
import com.qodein.shared.domain.repository.ReportRepository
import com.qodein.shared.model.ContentReport
import com.qodein.shared.model.ContentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReportRepositoryImpl(private val remoteDataSource: SupabaseReportDataSource, private val localDataSource: LocalReportDataSource) :
    ReportRepository {
    override suspend fun submitReport(report: ContentReport): Result<Unit, OperationError> =
        remoteCall("Submit report") {
            remoteDataSource.submit(report.toInsertDto())
            localDataSource.addReportedContentId(report.reportedItemId, type = report.reportedItemType)
        }

    override fun getHiddenContentIds(type: ContentType): Flow<Set<String>> =
        localDataSource.reportedContentIds.map { compositeKeys ->
            compositeKeys
                .filter { it.startsWith("${type.name}:") }
                .map { it.substringAfter(":") }
                .toSet()
        }
}
