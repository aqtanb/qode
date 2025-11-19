package com.qodein.core.data.repository

import co.touchlab.kermit.Logger
import com.google.firebase.firestore.DocumentSnapshot
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
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromocodeId
import kotlinx.io.IOException

class PromocodeRepositoryImpl(
    private val promoCodeDataSource: FirestorePromocodeDataSource,
    private val userDataSource: FirestoreUserDataSource
) : PromocodeRepository {

    companion object {
        private const val TAG = "PromocodeRepository"
    }

    override suspend fun createPromocode(promocode: PromoCode): Result<Unit, OperationError> =
        try {
            Logger.i(TAG) { "Creating promocode: ${promocode.code}" }

            val dto = PromocodeMapper.toDto(promocode)
            promoCodeDataSource.createPromocode(dto)

            Logger.i(TAG) { "Successfully created promocode: ${promocode.id.value}" }

            try {
                userDataSource.incrementPromocodeCount(promocode.authorId.value)
                Logger.d(TAG) { "Incremented promocode count for user: ${promocode.authorId.value}" }
            } catch (e: Exception) {
                Logger.w(TAG, e) { "Failed to increment promocode count: ${e.message}" }
            }

            Result.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Result.Error(ErrorMapper.mapFirestoreException(e, TAG))
        } catch (e: SecurityException) {
            Logger.e(TAG, e) { "Security error creating promocode: ${promocode.code}" }
            Result.Error(FirestoreError.PermissionDenied)
        } catch (e: IllegalArgumentException) {
            Logger.e(TAG, e) { "Invalid data creating promocode: ${promocode.code}" }
            Result.Error(FirestoreError.InvalidArgument)
        } catch (e: IOException) {
            Logger.e(TAG, e) { "Network error creating promocode: ${promocode.code}" }
            Result.Error(SystemError.Offline)
        } catch (e: IllegalStateException) {
            Logger.e(TAG, e) { "Service down creating promocode: ${promocode.code}" }
            Result.Error(SystemError.ServiceDown)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Unknown error creating promocode: ${e::class.simpleName}" }
            Result.Error(SystemError.Unknown)
        }

    override suspend fun getPromocodes(
        sortBy: ContentSortBy,
        filterByServices: List<String>?,
        paginationRequest: PaginationRequest<ContentSortBy>
    ): Result<PaginatedResult<PromoCode, ContentSortBy>, OperationError> =
        try {
            Logger.d(TAG) { "Getting promocodes: sortBy=$sortBy, services=${filterByServices?.size}" }

            val (sortByField, sortDirection) = mapSortBy(sortBy)
            val cursor = paginationRequest.cursor?.documentSnapshot as? DocumentSnapshot

            val pagedDto = promoCodeDataSource.getPromoCodes(
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

            Logger.i(TAG) { "Retrieved ${promocodes.size} promocodes, hasMore=${pagedDto.hasMore}" }
            Result.Success(result)
        } catch (e: FirebaseFirestoreException) {
            Logger.e(TAG, e) { "Firestore error getting promocodes [${e.code.name}]" }
            Result.Error(ErrorMapper.mapFirestoreException(e, TAG))
        } catch (e: IOException) {
            Logger.e(TAG, e) { "Network error getting promocodes" }
            Result.Error(SystemError.Offline)
        } catch (e: IllegalStateException) {
            Logger.e(TAG, e) { "Service down getting promocodes" }
            Result.Error(SystemError.ServiceDown)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Unknown error getting promocodes: ${e::class.simpleName}" }
            Result.Error(SystemError.Unknown)
        }

    private fun mapSortBy(sortBy: ContentSortBy): Pair<String, Query.Direction> =
        when (sortBy) {
            ContentSortBy.POPULARITY -> PromocodeDto.FIELD_UPVOTES to Query.Direction.DESCENDING
            ContentSortBy.NEWEST -> PromocodeDto.FIELD_CREATED_AT to Query.Direction.DESCENDING
            ContentSortBy.EXPIRING_SOON -> PromocodeDto.FIELD_END_DATE to Query.Direction.ASCENDING
        }

    override suspend fun getPromocodeById(id: PromocodeId): Result<PromoCode, OperationError> =
        try {
            val dto = promoCodeDataSource.getPromocodeById(id.value)
            if (dto != null) {
                val promocode = PromocodeMapper.toDomain(dto)
                Result.Success(promocode)
            } else {
                Logger.w(TAG) { "PromoCode not found: ${id.value}" }
                Result.Error(FirestoreError.NotFound)
            }
        } catch (e: FirebaseFirestoreException) {
            Result.Error(ErrorMapper.mapFirestoreException(e, TAG))
        } catch (e: IllegalArgumentException) {
            Logger.e(TAG, e) { "Invalid PromoCode data for id: ${id.value}" }
            Result.Error(FirestoreError.NotFound)
        } catch (e: IOException) {
            Logger.e(TAG, e) { "Network error getting PromoCode: ${id.value}" }
            Result.Error(SystemError.Offline)
        } catch (e: IllegalStateException) {
            Logger.e(TAG, e) { "Service down getting PromoCode: ${id.value}" }
            Result.Error(SystemError.ServiceDown)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Unknown error getting PromoCode: ${id.value} - ${e::class.simpleName}" }
            Result.Error(SystemError.Unknown)
        }
}
