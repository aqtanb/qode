package com.qodein.core.data.repository

import co.touchlab.kermit.Logger
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ServiceRepositoryImpl(private val serviceDataSource: FirestoreServiceDataSource) : ServiceRepository {

    companion object {
        private const val TAG = "ServiceRepository"
    }

    override fun searchServices(
        query: String,
        limit: Int
    ): Flow<Result<List<Service>, OperationError>> =
        flow {
            try {
                Logger.i(TAG) { "Searching services: query='$query', limit=$limit" }
                val dtos = serviceDataSource.searchServices(query, limit)
                val services = dtos.map { ServiceMapper.toDomain(it) }
                Logger.i(TAG) { "Found ${services.size} services" }
                emit(Result.Success(services))
            } catch (e: AlgoliaRuntimeException) {
                emit(Result.Error(ErrorMapper.mapAlgoliaException(e, TAG)))
            } catch (e: FirebaseFirestoreException) {
                emit(Result.Error(ErrorMapper.mapFirestoreException(e, TAG)))
            } catch (e: SerializationException) {
                Logger.e(TAG, e) { "Serialization error mapping service data: ${e.message}" }
                emit(Result.Error(ServiceError.RetrievalFailure.DataCorrupted))
            } catch (e: UnknownHostException) {
                Logger.e(TAG, e) { "No internet connection: ${e.message}" }
                emit(Result.Error(SystemError.Offline))
            } catch (e: SocketTimeoutException) {
                Logger.e(TAG, e) { "Request timeout: ${e.message}" }
                emit(Result.Error(SystemError.Offline))
            } catch (e: IOException) {
                Logger.e(TAG, e) { "Network I/O error: ${e.message}" }
                emit(Result.Error(SystemError.Offline))
            } catch (e: Exception) {
                Logger.e(TAG, e) { "Unexpected error searching services: ${e::class.simpleName} - ${e.message}" }
                emit(Result.Error(ServiceError.SearchFailure.NoResults))
            }
        }

    override fun getPopularServices(limit: Long): Flow<Result<List<Service>, OperationError>> =
        flow {
            try {
                Logger.i(TAG) { "Fetching popular services: limit=$limit" }
                val dtos = serviceDataSource.getPopularServices(limit)
                val services = dtos.map { ServiceMapper.toDomain(it) }
                Logger.i(TAG) { "Retrieved ${services.size} popular services" }
                emit(Result.Success(services))
            } catch (e: FirebaseFirestoreException) {
                emit(Result.Error(ErrorMapper.mapFirestoreException(e, TAG)))
            } catch (e: SerializationException) {
                Logger.e(TAG, e) { "Serialization error mapping service data: ${e.message}" }
                emit(Result.Error(ServiceError.RetrievalFailure.DataCorrupted))
            } catch (e: UnknownHostException) {
                Logger.e(TAG, e) { "No internet connection: ${e.message}" }
                emit(Result.Error(SystemError.Offline))
            } catch (e: SocketTimeoutException) {
                Logger.e(TAG, e) { "Request timeout: ${e.message}" }
                emit(Result.Error(SystemError.Offline))
            } catch (e: IOException) {
                Logger.e(TAG, e) { "Network I/O error: ${e.message}" }
                emit(Result.Error(SystemError.Offline))
            } catch (e: Exception) {
                Logger.e(TAG, e) { "Unexpected error fetching popular services: ${e::class.simpleName} - ${e.message}" }
                emit(Result.Error(ServiceError.RetrievalFailure.NotFound))
            }
        }
}
