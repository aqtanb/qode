package com.qodein.core.data.repository

import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.qodein.core.data.datasource.FirestorePromocodeDataSource
import com.qodein.core.data.datasource.FirestoreUserDataSource
import com.qodein.core.data.dto.PromocodeDto
import com.qodein.core.data.mapper.PromocodeMapper
import com.qodein.core.data.util.ErrorMapper
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.FirestoreError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.model.ContentSortBy
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationCursor
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import timber.log.Timber
import java.io.IOException

class PromocodeRepositoryImpl(private val dataSource: FirestorePromocodeDataSource, private val userDataSource: FirestoreUserDataSource) :
    PromocodeRepository {
    override suspend fun createPromocode(promocode: Promocode): Result<Unit, OperationError> =
        try {
            Timber.i("Creating promocode: %s", promocode.code.value)

            val dto = PromocodeMapper.toDto(promocode)
            dataSource.createPromocode(dto)

            Timber.i("Successfully created promocode: %s", promocode.id.value)

            try {
                userDataSource.incrementPromocodeCount(promocode.authorId.value)
                Timber.d("Incremented promocode count for user: %s", promocode.authorId.value)
            } catch (e: Exception) {
                Timber.w(e, "Failed to increment promocode count: %s", e.message)
            }

            Result.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: SecurityException) {
            Timber.e(e, "Security error creating promocode: %s", promocode.code.value)
            Result.Error(FirestoreError.PermissionDenied)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Invalid data creating promocode: %s", promocode.code.value)
            Result.Error(FirestoreError.InvalidArgument)
        } catch (e: IOException) {
            Timber.e(e, "Network error creating promocode: %s", promocode.code.value)
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unknown error creating promocode: %s", e::class.simpleName)
            Result.Error(SystemError.Unknown)
        }

    override suspend fun getPromocodes(
        sortBy: ContentSortBy,
        filterByServices: List<String>?,
        paginationRequest: PaginationRequest<ContentSortBy>
    ): Result<PaginatedResult<Promocode, ContentSortBy>, OperationError> =
        try {
            Timber.d("Getting promocodes: sortBy=%s, services=%s", sortBy, filterByServices?.size)

            val (sortByField, sortDirection) = mapSortBy(sortBy)
            val cursor = paginationRequest.cursor?.documentSnapshot

            val pagedDto = dataSource.getPromoCodes(
                sortByField = sortByField,
                sortDirection = sortDirection,
                filterByServices = filterByServices,
                limit = paginationRequest.limit,
                startAfter = cursor,
            )

            val promocodes = pagedDto.items.map { PromocodeMapper.toDomain(it) }

            val nextCursor = if (pagedDto.hasMore && pagedDto.lastDocument != null) {
                PaginationCursor(
                    documentSnapshot = pagedDto.lastDocument,
                    sortBy = sortBy,
                )
            } else {
                null
            }

            val result = PaginatedResult.of(
                data = promocodes,
                nextCursor = nextCursor,
                hasMore = pagedDto.hasMore,
            )

            Timber.i("Retrieved %d promocodes, hasMore=%s", promocodes.size, pagedDto.hasMore)
            Result.Success(result)
        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "Firestore error getting promocodes [%s]", e.code.name)
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: IOException) {
            Timber.e(e, "Network error getting promocodes")
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unknown error getting promocodes: %s", e::class.simpleName)
            Result.Error(SystemError.Unknown)
        }

    private fun mapSortBy(sortBy: ContentSortBy): Pair<String, Query.Direction> =
        when (sortBy) {
            ContentSortBy.POPULARITY -> PromocodeDto.FIELD_UPVOTES to Query.Direction.DESCENDING
            ContentSortBy.NEWEST -> PromocodeDto.FIELD_CREATED_AT to Query.Direction.DESCENDING
            ContentSortBy.EXPIRING_SOON -> PromocodeDto.FIELD_END_DATE to Query.Direction.ASCENDING
        }

    override suspend fun getPromocodeById(id: PromocodeId): Result<Promocode, OperationError> =
        try {
            val dto = dataSource.getPromocodeById(id.value)
            if (dto != null) {
                val promocode = PromocodeMapper.toDomain(dto)
                Result.Success(promocode)
            } else {
                Timber.w("Promocode not found: %s", id.value)
                Result.Error(FirestoreError.NotFound)
            }
        } catch (e: FirebaseFirestoreException) {
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Invalid Promocode data for id: %s", id.value)
            Result.Error(FirestoreError.NotFound)
        } catch (e: IOException) {
            Timber.e(e, "Network error getting Promocode: %s", id.value)
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unknown error getting Promocode: %s - %s", id.value, e::class.simpleName)
            Result.Error(SystemError.Unknown)
        }
}
