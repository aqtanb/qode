package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.ContentSortBy
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.UserId

/**
 * Repository interface for Promocode operations.
 * Returns Result<D, OperationError> for type-safe error handling.
 * Repository implementations handle exception translation to domain errors.
 */
interface PromocodeRepository {

    /**
     * Create a new promocode.
     */
    suspend fun createPromocode(promocode: Promocode): Result<Unit, OperationError>

    /**
     * Get promocodes with filtering and sorting using cursor-based pagination.
     */
    suspend fun getPromocodes(
        sortBy: ContentSortBy = ContentSortBy.POPULARITY,
        filterByServices: List<String>? = null,
        paginationRequest: PaginationRequest<ContentSortBy>
    ): Result<PaginatedResult<Promocode, ContentSortBy>, OperationError>

    /**
     * Get a specific promocode by ID.
     * Returns NotFound error if promocode doesn't exist.
     */
    suspend fun getPromocodeById(id: PromocodeId): Result<Promocode, OperationError>

    /**
     * Get promocodes by a specific user with cursor-based pagination.
     * Returns promocodes sorted by creation date (most recent first).
     */
    suspend fun getPromocodesByUser(
        userId: UserId,
        cursor: Any?,
        limit: Int
    ): Result<PaginatedResult<Promocode, ContentSortBy>, OperationError>
}
