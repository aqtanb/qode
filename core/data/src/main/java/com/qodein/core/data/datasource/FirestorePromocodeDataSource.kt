package com.qodein.core.data.datasource

import co.touchlab.kermit.Logger
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.cache.QueryCache
import com.qodein.core.data.mapper.PromoCodeMapper
import com.qodein.core.data.model.PromoCodeDto
import com.qodein.shared.model.ContentSortBy
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationCursor
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private inline fun <reified T : Any, R> DocumentSnapshot.toDomainModel(mapper: (T) -> R): R? = toObject<T>()?.let(mapper)

@Singleton
class FirestorePromocodeDataSource @Inject constructor(private val firestore: FirebaseFirestore, private val queryCache: QueryCache) {
    companion object {
        private const val TAG = "FirestorePromoCodeDS"
        private const val PROMOCODES_COLLECTION = "promocodes"
    }

    suspend fun createPromoCode(promoCode: PromoCode): PromoCode {
        Logger.d(TAG) { "Creating promo code: ${promoCode.code} for service: ${promoCode.serviceName}" }

        try {
            val dto = PromoCodeMapper.toDto(promoCode)
            Logger.d(TAG) { "Mapped to DTO with documentId: ${dto.documentId}" }
            Logger.d(TAG) { "DTO data: serviceId=${dto.serviceId}, serviceName=${dto.serviceName}, type=${dto.type}" }

            firestore.collection(PROMOCODES_COLLECTION)
                .document(dto.documentId)
                .set(dto)
                .await()

            Logger.i(TAG) { "Successfully created promo code document: ${dto.documentId}" }
            return promoCode
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Failed to create promo code: ${promoCode.code}" }
            throw java.io.IOException("Failed to create promo code: ${e.message}", e)
        }
    }

    suspend fun getPromoCodes(
        query: String?,
        sortBy: ContentSortBy,
        filterByServices: List<String>?,
        filterByCategories: List<String>?,
        paginationRequest: PaginationRequest<ContentSortBy>
    ): PaginatedResult<PromoCode, ContentSortBy> {
        val isFirstPage = paginationRequest.cursor == null

        // Check cache for first page queries
        if (isFirstPage) {
            queryCache.get(
                query = query,
                sortBy = sortBy.name,
                filterByService = filterByServices?.joinToString(","),
                filterByCategory = filterByCategories?.joinToString(","),
                isFirstPage = true,
            )?.let { cachedResult ->
                Logger.d { "Returning cached result for promo codes (${cachedResult.data.size} items)" }
                return cachedResult
            }
        }

        var firestoreQuery: Query = firestore.collection(PROMOCODES_COLLECTION)

        // Apply filters
        query?.let { searchQuery ->
            if (searchQuery.isNotBlank()) {
                firestoreQuery = firestoreQuery
                    .whereGreaterThanOrEqualTo("code", searchQuery.uppercase())
                    .whereLessThanOrEqualTo("code", searchQuery.uppercase() + "\uf8ff")
            }
        }

        filterByServices?.let { services ->
            if (services.isNotEmpty()) {
                firestoreQuery = if (services.size == 1) {
                    firestoreQuery.whereEqualTo("serviceName", services.first())
                } else {
                    firestoreQuery.whereIn("serviceName", services)
                }
            }
        }

        filterByCategories?.let { categories ->
            if (categories.isNotEmpty()) {
                firestoreQuery = if (categories.size == 1) {
                    firestoreQuery.whereEqualTo("category", categories.first())
                } else {
                    firestoreQuery.whereIn("category", categories)
                }
            }
        }

        // Apply sorting
        firestoreQuery = when (sortBy) {
            ContentSortBy.POPULARITY -> {
                firestoreQuery.orderBy("voteScore", Query.Direction.DESCENDING)
            }
            ContentSortBy.NEWEST -> {
                firestoreQuery.orderBy("createdAt", Query.Direction.DESCENDING)
            }
            ContentSortBy.EXPIRING_SOON -> {
                firestoreQuery.orderBy("endDate", Query.Direction.ASCENDING)
            }
        }

        // Apply cursor-based pagination
        paginationRequest.cursor?.let { cursor ->
            cursor.documentSnapshot?.let { docSnapshot ->
                firestoreQuery = firestoreQuery.startAfter(docSnapshot as DocumentSnapshot)
            }
        }

        // Apply limit
        firestoreQuery = firestoreQuery.limit(paginationRequest.limit.toLong())

        val querySnapshot = firestoreQuery.get().await()
        val documents = querySnapshot.documents

        val results = documents.mapNotNull { document ->
            try {
                val dto = document.toObject<PromoCodeDto>()
                if (dto == null) {
                    Logger.w { "Document ${document.id} failed to convert to DTO" }
                    return@mapNotNull null
                }
                PromoCodeMapper.toDomain(dto)
            } catch (e: Exception) {
                Logger.e(e) { "Failed to parse document ${document.id}" }
                null
            }
        }

        val nextCursor = if (documents.isNotEmpty() && documents.size == paginationRequest.limit) {
            val lastDoc = documents.last()
            PaginationCursor(
                documentSnapshot = lastDoc,
                sortBy = sortBy,
                documentId = lastDoc.id,
            )
        } else {
            null
        }

        val result = PaginatedResult.of(
            data = results,
            nextCursor = nextCursor,
            hasMore = documents.size == paginationRequest.limit,
        )

        if (isFirstPage && results.isNotEmpty()) {
            queryCache.put(
                query = query,
                sortBy = sortBy.name,
                filterByService = filterByServices?.joinToString(","),
                filterByCategory = filterByCategories?.joinToString(","),
                isFirstPage = true,
                result = result,
            )
        }

        return result
    }

    suspend fun getPromoCodeById(id: PromoCodeId): PromoCode? {
        val document = firestore.collection(PROMOCODES_COLLECTION)
            .document(id.value)
            .get()
            .await()

        return document.toDomainModel<PromoCodeDto, PromoCode>(PromoCodeMapper::toDomain)
    }
}
