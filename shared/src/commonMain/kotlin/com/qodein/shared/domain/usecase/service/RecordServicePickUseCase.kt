package com.qodein.shared.domain.usecase.service

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.ServiceRepository
import com.qodein.shared.model.ServiceId

/**
 * Counts services the user just picked as a filter, so the popular list follows demand.
 */
class RecordServicePickUseCase(private val repository: ServiceRepository) {
    suspend operator fun invoke(ids: Collection<ServiceId>): Result<Unit, OperationError> {
        ids.forEach { id ->
            val result = repository.recordServicePick(id)
            if (result is Result.Error) return result
        }
        return Result.Success(Unit)
    }
}
