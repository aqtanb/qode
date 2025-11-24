package com.qodein.core.data.repository

import com.algolia.client.exception.AlgoliaRuntimeException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.qodein.core.data.datasource.FirestoreServiceDataSource
import com.qodein.core.data.mapper.ServiceMapper
import com.qodein.core.data.util.ErrorMapper
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.ServiceError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.repository.ServiceRepository
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerializationException
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ServiceRepositoryImpl(private val serviceDataSource: FirestoreServiceDataSource) : ServiceRepository {
    override suspend fun getById(id: ServiceId): Result<Service, OperationError> {
        TODO("Not yet implemented")
    }

    override suspend fun findByName(name: String): Result<Service, OperationError> {
        TODO("Not yet implemented")
    }

    override suspend fun create(service: Service): Result<Service, OperationError> {
        TODO("Not yet implemented")
    }

    companion object {
        private const val TAG = "ServiceRepository"
    }

    override fun searchServices(
        query: String,
        limit: Int
    ): Flow<Result<List<Service>, OperationError>> =
        flow {
            try {
                Timber.tag(TAG).i("Searching services: query='%s', limit=%d", query, limit)
                val dtos = serviceDataSource.searchServices(query, limit)
                val services = dtos.map { ServiceMapper.toDomain(it) }
                Timber.tag(TAG).i("Found %d services", services.size)
                emit(Result.Success(services))
            } catch (e: AlgoliaRuntimeException) {
                emit(Result.Error(ErrorMapper.mapAlgoliaException(e)))
            } catch (e: FirebaseFirestoreException) {
                emit(Result.Error(ErrorMapper.mapFirestoreException(e)))
            } catch (e: SerializationException) {
                Timber.tag(TAG).e(e, "Serialization error mapping service data: %s", e.message)
                emit(Result.Error(ServiceError.RetrievalFailure.DataCorrupted))
            } catch (e: UnknownHostException) {
                Timber.tag(TAG).e(e, "No internet connection: %s", e.message)
                emit(Result.Error(SystemError.Offline))
            } catch (e: SocketTimeoutException) {
                Timber.tag(TAG).e(e, "Request timeout: %s", e.message)
                emit(Result.Error(SystemError.Offline))
            } catch (e: IOException) {
                Timber.tag(TAG).e(e, "Network I/O error: %s", e.message)
                emit(Result.Error(SystemError.Offline))
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Unexpected error searching services: %s - %s", e::class.simpleName, e.message)
                emit(Result.Error(ServiceError.SearchFailure.NoResults))
            }
        }

    override fun getPopularServices(limit: Long): Flow<Result<List<Service>, OperationError>> =
        flow {
            try {
                Timber.tag(TAG).i("Fetching popular services: limit=%d", limit)
                val dtos = serviceDataSource.getPopularServices(limit)
                val services = dtos.map { ServiceMapper.toDomain(it) }
                Timber.tag(TAG).i("Retrieved %d popular services", services.size)
                emit(Result.Success(services))
            } catch (e: FirebaseFirestoreException) {
                emit(Result.Error(ErrorMapper.mapFirestoreException(e)))
            } catch (e: SerializationException) {
                Timber.tag(TAG).e(e, "Serialization error mapping service data: %s", e.message)
                emit(Result.Error(ServiceError.RetrievalFailure.DataCorrupted))
            } catch (e: UnknownHostException) {
                Timber.tag(TAG).e(e, "No internet connection: %s", e.message)
                emit(Result.Error(SystemError.Offline))
            } catch (e: SocketTimeoutException) {
                Timber.tag(TAG).e(e, "Request timeout: %s", e.message)
                emit(Result.Error(SystemError.Offline))
            } catch (e: IOException) {
                Timber.tag(TAG).e(e, "Network I/O error: %s", e.message)
                emit(Result.Error(SystemError.Offline))
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Unexpected error fetching popular services: %s - %s", e::class.simpleName, e.message)
                emit(Result.Error(ServiceError.RetrievalFailure.NotFound))
            }
        }
}
