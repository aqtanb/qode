package com.qodein.core.testing.fake

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.DatabaseError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.ServiceRepository
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId

class FakeServiceRepository : ServiceRepository {
    val services = mutableListOf<Service>()

    /** When set, every call fails with this error. */
    var error: OperationError? = null

    override suspend fun getById(id: ServiceId): Result<Service, OperationError> {
        error?.let { return Result.Error(it) }
        return services.firstOrNull { it.id == id }?.let { Result.Success(it) } ?: Result.Error(DatabaseError.NotFound)
    }

    override suspend fun findByName(name: String): Result<Service, OperationError> {
        error?.let { return Result.Error(it) }
        return services.firstOrNull { it.name.equals(name, ignoreCase = true) }?.let { Result.Success(it) }
            ?: Result.Error(DatabaseError.NotFound)
    }

    override suspend fun create(service: Service): Result<Service, OperationError> {
        error?.let { return Result.Error(it) }
        services += service
        return Result.Success(service)
    }

    override suspend fun searchServices(
        query: String,
        limit: Int
    ): Result<List<Service>, OperationError> = Result.Success(services.filter { it.name.contains(query, ignoreCase = true) })

    override suspend fun getPopularServices(limit: Long): Result<List<Service>, OperationError> =
        Result.Success(services.take(limit.toInt()))
}
