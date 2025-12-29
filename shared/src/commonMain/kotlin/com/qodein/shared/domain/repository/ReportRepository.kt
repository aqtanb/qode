package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.ContentReport
import com.qodein.shared.model.ContentType
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    suspend fun submitReport(report: ContentReport): Result<Unit, OperationError>
    fun getHiddenContentIds(type: ContentType): Flow<Set<String>>
}
