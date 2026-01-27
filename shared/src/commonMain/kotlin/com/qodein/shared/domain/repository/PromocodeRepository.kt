package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.PromocodeSortBy
import com.qodein.shared.model.UserId

interface PromocodeRepository {
    suspend fun createPromocode(promocode: Promocode): Result<Unit, OperationError>

    suspend fun getPromocodes(
        sortBy: PromocodeSortBy = PromocodeSortBy.POPULARITY,
        filterByServices: List<String>? = null,
        paginationRequest: PaginationRequest<PromocodeSortBy>,
        blockedUserIds: Set<UserId>
    ): Result<PaginatedResult<Promocode, PromocodeSortBy>, OperationError>

    suspend fun getPromocodeById(id: PromocodeId): Result<Promocode, OperationError>

    suspend fun getPromocodesByUser(
        userId: UserId,
        cursor: Any?,
        limit: Int
    ): Result<PaginatedResult<Promocode, PromocodeSortBy>, OperationError>
}
