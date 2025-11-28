package com.qodein.shared.domain.usecase.service

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.ServiceRepository
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceRef

class GetOrCreateServiceUseCase(private val repo: ServiceRepository) {
    suspend operator fun invoke(ref: ServiceRef): Result<Service, OperationError> =
        when (ref) {
            is ServiceRef.ById -> repo.getById(ref.id)

            is ServiceRef.ByName -> {
                when (val result = repo.findByName(ref.name)) {
                    is Result.Success -> result
                    is Result.Error -> {
                        val newService = when (val createResult = Service.create(name = ref.name)) {
                            is Result.Success -> createResult.data
                            is Result.Error -> return Result.Error(createResult.error)
                        }
                        repo.create(newService)
                    }
                }
            }
        }
}
