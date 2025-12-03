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
import kotlinx.serialization.SerializationException
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ServiceRepositoryImpl(private val dataSource: FirestoreServiceDataSource) : ServiceRepository {
    override suspend fun getById(id: ServiceId): Result<Service, OperationError> =
        try {
            Timber.d("Fetching service: id=%s", id)
            val dto = dataSource.getById(id.value)
            if (dto == null) {
                Timber.w("Service not found: id=%s", id)
                Result.Error(ServiceError.RetrievalFailure.NotFound)
            } else {
                Result.Success(ServiceMapper.toDomain(dto))
            }
        } catch (e: FirebaseFirestoreException) {
            Timber.w(e, "Firestore error fetching service id=%s", id)
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: SerializationException) {
            Timber.e(e, "Serialization error mapping service id=%s", id)
            Result.Error(ServiceError.RetrievalFailure.DataCorrupted)
        } catch (e: UnknownHostException) {
            Timber.w(e, "No internet connection")
            Result.Error(SystemError.Offline)
        } catch (e: SocketTimeoutException) {
            Timber.w(e, "Request timeout")
            Result.Error(SystemError.Offline)
        } catch (e: IOException) {
            Timber.w(e, "Network I/O error")
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error fetching service id=%s", id)
            Result.Error(SystemError.Unknown)
        }

    override suspend fun findByName(name: String): Result<Service, OperationError> =
        try {
            Timber.d("Finding service by name: %s", name)
            val dto = dataSource.findByName(name)
            if (dto == null) {
                Timber.w("Service not found by name: %s", name)
                Result.Error(ServiceError.RetrievalFailure.NotFound)
            } else {
                Result.Success(ServiceMapper.toDomain(dto))
            }
        } catch (e: FirebaseFirestoreException) {
            Timber.w(e, "Firestore error finding service name=%s", name)
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: SerializationException) {
            Timber.e(e, "Serialization error mapping service name=%s", name)
            Result.Error(ServiceError.RetrievalFailure.DataCorrupted)
        } catch (e: UnknownHostException) {
            Timber.w(e, "No internet connection")
            Result.Error(SystemError.Offline)
        } catch (e: SocketTimeoutException) {
            Timber.w(e, "Request timeout")
            Result.Error(SystemError.Offline)
        } catch (e: IOException) {
            Timber.w(e, "Network I/O error")
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error finding service name=%s", name)
            Result.Error(SystemError.Unknown)
        }

    override suspend fun create(service: Service): Result<Service, OperationError> =
        try {
            Timber.d("Creating service: id=%s", service.id)
            val dto = ServiceMapper.toDto(service)
            val createdDto = dataSource.create(dto)
            Result.Success(ServiceMapper.toDomain(createdDto))
        } catch (e: FirebaseFirestoreException) {
            Timber.w(e, "Firestore error creating service id=%s", service.id)
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: SerializationException) {
            Timber.e(e, "Serialization error creating service id=%s", service.id)
            Result.Error(ServiceError.SubmissionFailure.InvalidData)
        } catch (e: IllegalArgumentException) {
            Timber.w(e, "Invalid service data for id=%s", service.id)
            Result.Error(ServiceError.SubmissionFailure.InvalidData)
        } catch (e: SecurityException) {
            Timber.w(e, "Not authorized to create service id=%s", service.id)
            Result.Error(ServiceError.SubmissionFailure.NotAuthorized)
        } catch (e: UnknownHostException) {
            Timber.w(e, "No internet connection")
            Result.Error(SystemError.Offline)
        } catch (e: SocketTimeoutException) {
            Timber.w(e, "Request timeout")
            Result.Error(SystemError.Offline)
        } catch (e: IOException) {
            Timber.w(e, "Network I/O error")
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error creating service id=%s", service.id)
            Result.Error(SystemError.Unknown)
        }

    override suspend fun searchServices(
        query: String,
        limit: Int
    ): Result<List<Service>, OperationError> =
        try {
            Timber.d("Searching services: query='%s', limit=%d", query, limit)
            val dtos = dataSource.searchServices(query, limit)
            val services = dtos.map { ServiceMapper.toDomain(it) }
            Timber.d("Found %d services", services.size)
            Result.Success(services)
        } catch (e: AlgoliaRuntimeException) {
            Timber.w(e, "Algolia error searching services")
            Result.Error(ErrorMapper.mapAlgoliaException(e))
        } catch (e: FirebaseFirestoreException) {
            Timber.w(e, "Firestore error searching services")
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: SerializationException) {
            Timber.e(e, "Serialization error mapping service data")
            Result.Error(ServiceError.RetrievalFailure.DataCorrupted)
        } catch (e: UnknownHostException) {
            Timber.w(e, "No internet connection")
            Result.Error(SystemError.Offline)
        } catch (e: SocketTimeoutException) {
            Timber.w(e, "Request timeout")
            Result.Error(SystemError.Offline)
        } catch (e: IOException) {
            Timber.w(e, "Network I/O error")
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error searching services")
            Result.Error(ServiceError.SearchFailure.NoResults)
        }

    override suspend fun getPopularServices(limit: Long): Result<List<Service>, OperationError> =
        try {
            Timber.d("Fetching popular services: limit=%d", limit)
            val dtos = dataSource.getPopularServices(limit)
            val services = dtos.map { ServiceMapper.toDomain(it) }
            Timber.d("Retrieved %d popular services", services.size)
            Result.Success(services)
        } catch (e: FirebaseFirestoreException) {
            Timber.w(e, "Firestore error fetching popular services")
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: SerializationException) {
            Timber.e(e, "Serialization error mapping service data")
            Result.Error(ServiceError.RetrievalFailure.DataCorrupted)
        } catch (e: UnknownHostException) {
            Timber.w(e, "No internet connection")
            Result.Error(SystemError.Offline)
        } catch (e: SocketTimeoutException) {
            Timber.w(e, "Request timeout")
            Result.Error(SystemError.Offline)
        } catch (e: IOException) {
            Timber.w(e, "Network I/O error")
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error fetching popular services")
            Result.Error(ServiceError.RetrievalFailure.NotFound)
        }
}
