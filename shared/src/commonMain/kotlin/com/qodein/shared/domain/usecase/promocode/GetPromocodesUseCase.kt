package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.model.ContentSortBy
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.Promocode

class GetPromocodesUseCase(private val promoCodeRepository: PromocodeRepository) {
    companion object {
        const val DEFAULT_LIMIT = 5
    }
    suspend operator fun invoke(
        sortBy: ContentSortBy = ContentSortBy.POPULARITY,
        filterByServices: List<String>? = null,
        paginationRequest: PaginationRequest<ContentSortBy> = PaginationRequest.firstPage(DEFAULT_LIMIT)
    ): Result<PaginatedResult<Promocode, ContentSortBy>, OperationError> =
        promoCodeRepository.getPromocodes(
            sortBy = sortBy,
            filterByServices = filterByServices,
            paginationRequest = paginationRequest,
        )
}
