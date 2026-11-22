package com.qodein.shared.ios

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.data.datasource.SupabaseReportDataSource
import com.qodein.shared.data.mapper.toInsertDto
import com.qodein.shared.data.util.remoteCall
import com.qodein.shared.domain.repository.ReportRepository
import com.qodein.shared.model.ContentReport
import com.qodein.shared.model.ContentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import platform.Foundation.NSUserDefaults

internal class IosReportRepository(private val remoteDataSource: SupabaseReportDataSource) : ReportRepository {
    private val defaults = NSUserDefaults.standardUserDefaults
    private val reportedIds = MutableStateFlow(load())

    override suspend fun submitReport(report: ContentReport): Result<Unit, OperationError> =
        remoteCall("Submit report") {
            remoteDataSource.submit(report.toInsertDto())
            val updated = reportedIds.value + "${report.reportedItemType.name}:${report.reportedItemId}"
            defaults.setObject(updated.toList(), KEY)
            reportedIds.value = updated
        }

    override fun getHiddenContentIds(type: ContentType): Flow<Set<String>> =
        reportedIds.map { keys ->
            keys.filter { it.startsWith("${type.name}:") }.map { it.substringAfter(":") }.toSet()
        }

    private fun load(): Set<String> = defaults.stringArrayForKey(KEY).orEmpty().filterIsInstance<String>().toSet()

    private companion object {
        const val KEY = "reported_content_ids"
    }
}
