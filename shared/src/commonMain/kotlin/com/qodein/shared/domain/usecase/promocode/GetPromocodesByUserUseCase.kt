package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.SortBy
import com.qodein.shared.model.UserId

class GetPromocodesByUserUseCase(private val promocodeRepository: PromocodeRepository) {
    suspend operator fun invoke(
        userId: UserId,
        cursor: Any? = null
    ): Result<PaginatedResult<Promocode, SortBy>, OperationError> =
        promocodeRepository.getPromocodesByUser(
            userId = userId,
            cursor = cursor,
            limit = PaginationRequest.DEFAULT_PAGE_SIZE,
        )
}
