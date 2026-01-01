package com.qodein.shared.domain.usecase.service

import com.qodein.shared.common.Result
import com.qodein.shared.domain.repository.ServiceRepository
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId

class GetServicesByIdsUseCase(private val serviceRepository: ServiceRepository) {
    suspend operator fun invoke(serviceIds: Set<ServiceId>): Set<Service> {
        if (serviceIds.isEmpty()) return emptySet()

        return serviceIds.mapNotNull { serviceId ->
            when (val result = serviceRepository.getById(serviceId)) {
                is Result.Success -> result.data
                is Result.Error -> null
            }
        }.toSet()
    }
}
