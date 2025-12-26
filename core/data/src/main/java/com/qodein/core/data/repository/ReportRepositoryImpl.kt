package com.qodein.core.data.repository

import com.google.firebase.firestore.FirebaseFirestoreException
import com.qodein.core.data.datasource.FirestoreReportDataSource
import com.qodein.core.data.datasource.LocalReportDataSource
import com.qodein.core.data.mapper.ContentReportMapper
import com.qodein.core.data.util.ErrorMapper
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.FirestoreError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.repository.ReportRepository
import com.qodein.shared.model.ContentReport
import com.qodein.shared.model.ContentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException

class ReportRepositoryImpl(private val firestoreDataSource: FirestoreReportDataSource, private val localDataSource: LocalReportDataSource) :
    ReportRepository {
    override suspend fun submitReport(report: ContentReport): Result<Unit, OperationError> =
        try {
            Timber.i("Submitting report for item: %s, reason: %s", report.reportedItemId, report.reason)

            val dto = ContentReportMapper.toDto(report)
            firestoreDataSource.submitReport(dto)

            localDataSource.addReportedContentId(report.reportedItemId, type = report.reportedItemType)

            Timber.i("Successfully submitted report: %s", report.id)

            Result.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "Firestore error submitting report: %s", e.code.name)
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: SecurityException) {
            Timber.e(e, "Security error submitting report")
            Result.Error(FirestoreError.PermissionDenied)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Invalid data submitting report")
            Result.Error(FirestoreError.InvalidArgument)
        } catch (e: IOException) {
            Timber.e(e, "Network error submitting report")
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unknown error submitting report: %s", e::class.simpleName)
            Result.Error(SystemError.Unknown)
        }

    override fun getHiddenContentIds(type: ContentType): Flow<Set<String>> =
        localDataSource.reportedContentIds.map { compositeKeys ->
            compositeKeys
                .filter { it.startsWith("${type.name}:") }
                .map { it.substringAfter(":") }
                .toSet()
        }
}
