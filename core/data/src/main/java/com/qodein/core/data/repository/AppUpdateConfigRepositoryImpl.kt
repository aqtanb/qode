package com.qodein.core.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.qodein.core.data.datasource.FirestoreAppUpdateConfigDataSource
import com.qodein.core.data.mapper.AppUpdateConfigMapper
import com.qodein.core.data.util.ErrorMapper
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.FirestoreError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.repository.AppUpdateConfigRepository
import com.qodein.shared.model.AppUpdateConfig
import timber.log.Timber

class AppUpdateConfigRepositoryImpl(private val dataSource: FirestoreAppUpdateConfigDataSource) : AppUpdateConfigRepository {

    override suspend fun getAppUpdateConfig(): Result<AppUpdateConfig, OperationError> =
        try {
            val dto = dataSource.getAppUpdateConfig()
            if (dto != null) {
                val config = AppUpdateConfigMapper.toDomain(dto)
                Timber.d("Successfully fetched app update config: minimumVersionCode=%d", config.minimumVersionCode)
                Result.Success(config)
            } else {
                Timber.e("App update config document not found in Firestore")
                Result.Error(FirestoreError.NotFound)
            }
        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "Firestore error fetching app update config")
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: FirebaseNetworkException) {
            Timber.e(e, "Network error - no internet connection")
            Result.Error(FirestoreError.Unavailable)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error fetching app update config: %s", e.javaClass.simpleName)
            Result.Error(SystemError.Unknown)
        }
}
