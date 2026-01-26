package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.domain.usecase.report.GetReportedContentIdsUseCase
import com.qodein.shared.domain.usecase.user.GetBlockedUserIdsUseCase
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeSortBy

class GetPromocodesUseCase(
    private val promocodeRepository: PromocodeRepository,
    private val getReportedContentIdsUseCase: GetReportedContentIdsUseCase,
    private val getBlockedUserIdsUseCase: GetBlockedUserIdsUseCase
) {
    companion object {
        const val DEFAULT_LIMIT = 5
    }
    suspend operator fun invoke(
        sortBy: PromocodeSortBy = PromocodeSortBy.POPULARITY,
        filterByServices: List<String>? = null,
        paginationRequest: PaginationRequest<PromocodeSortBy> = PaginationRequest.firstPage(DEFAULT_LIMIT)
    ): Result<PaginatedResult<Promocode, PromocodeSortBy>, OperationError> {
        val promocodes = promocodeRepository.getPromocodes(
            sortBy = sortBy,
            filterByServices = filterByServices,
            paginationRequest = paginationRequest,
        )
        return when (promocodes) {
            is Result.Error -> promocodes
            is Result.Success -> {
                val hiddenIds = getReportedContentIdsUseCase(ContentType.PROMOCODE)
                val blockedUserIds = getBlockedUserIdsUseCase()

                val filteredPromocodes = promocodes.data.data.filterNot { promocode ->
                    promocode.id.value in hiddenIds ||
                        promocode.authorId in blockedUserIds
                }

                Result.Success(
                    PaginatedResult(
                        data = filteredPromocodes,
                        nextCursor = promocodes.data.nextCursor,
                    ),
                )
            }
        }
    }
}
