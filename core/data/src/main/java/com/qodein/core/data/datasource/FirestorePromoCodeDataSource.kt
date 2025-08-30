package com.qodein.core.data.datasource

import co.touchlab.kermit.Logger
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.cache.QueryCache
import com.qodein.core.data.mapper.PromoCodeMapper
import com.qodein.core.data.model.PromoCodeDto
import com.qodein.shared.domain.repository.PromoCodeSortBy
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationCursor
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.UserId
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private inline fun <reified T : Any, R> DocumentSnapshot.toDomainModel(mapper: (T) -> R): R? = toObject<T>()?.let(mapper)

@Singleton
class FirestorePromoCodeDataSource @Inject constructor(private val firestore: FirebaseFirestore, private val queryCache: QueryCache) {
    companion object {
        private const val TAG = "FirestorePromoCodeDS"
        private const val PROMOCODES_COLLECTION = "promocodes"
    }

    suspend fun createPromoCode(promoCode: PromoCode): PromoCode {
        val dto = PromoCodeMapper.toDto(promoCode)

        firestore.collection(PROMOCODES_COLLECTION)
            .document(dto.documentId)
            .set(dto)
            .await()

        return promoCode
    }

    suspend fun getPromoCodes(
        query: String?,
        sortBy: PromoCodeSortBy,
        filterByServices: List<String>?,
        filterByCategories: List<String>?,
        paginationRequest: PaginationRequest
    ): PaginatedResult<PromoCode> {
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
            PromoCodeSortBy.POPULARITY -> {
                firestoreQuery.orderBy("voteScore", Query.Direction.DESCENDING)
            }
            PromoCodeSortBy.NEWEST -> {
                firestoreQuery.orderBy("createdAt", Query.Direction.DESCENDING)
            }
            PromoCodeSortBy.EXPIRING_SOON -> {
                firestoreQuery.orderBy("endDate", Query.Direction.ASCENDING)
            }
        }

        // Apply cursor-based pagination
        paginationRequest.cursor?.let { cursor ->
            cursor.lastDocumentSnapshot?.let { docSnapshot ->
                // Use DocumentSnapshot for proper Firestore cursor pagination
                firestoreQuery = firestoreQuery.startAfter(docSnapshot as DocumentSnapshot)
            } ?: run {
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

                val domainModel = PromoCodeMapper.toDomain(dto)
                domainModel
            } catch (e: Exception) {
                Logger.e(e) { "Failed to parse document ${document.id}" }
                null
            }
        }

        // Add back detailed logging for debugging
        Logger.d { "Successfully loaded ${results.size} promo codes" }
        results.forEachIndexed { index, promoCode ->
            Logger.d { "  [${index + 1}] ${promoCode.code} for ${promoCode.serviceName} (upvotes: ${promoCode.upvotes})" }
        }

        val nextCursor = if (documents.isNotEmpty() && documents.size == paginationRequest.limit) {
            val lastDoc = documents.last()
            val sortFieldValue = when (sortBy) {
                PromoCodeSortBy.POPULARITY -> lastDoc.getLong("voteScore")
                PromoCodeSortBy.NEWEST -> lastDoc.getTimestamp("createdAt")
                PromoCodeSortBy.EXPIRING_SOON -> lastDoc.getTimestamp("endDate")
            }
            PaginationCursor.fromDocumentSnapshot(
                documentId = lastDoc.id,
                sortFieldValue = sortFieldValue,
                lastDocumentSnapshot = lastDoc, // Store the actual DocumentSnapshot
            )
        } else {
            null
        }

        val hasMore = documents.size == paginationRequest.limit

        val result = PaginatedResult.of(
            data = results,
            nextCursor = nextCursor,
            hasMore = hasMore,
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

    suspend fun getPromoCodeByCode(code: String): PromoCode? {
        val querySnapshot = firestore.collection(PROMOCODES_COLLECTION)
            .whereEqualTo("code", code.uppercase())
            .limit(1)
            .get()
            .await()

        return querySnapshot.documents.firstOrNull()?.toDomainModel<PromoCodeDto, PromoCode>(PromoCodeMapper::toDomain)
    }

    suspend fun getPromoCodeByCodeAndService(
        code: String,
        serviceName: String
    ): PromoCode? {
        // With composite IDs, we can directly get the document by constructing the ID
        val compositeId = PromoCode.generateCompositeId(code, serviceName)
        val document = firestore.collection(PROMOCODES_COLLECTION)
            .document(compositeId)
            .get()
            .await()

        return document.toDomainModel<PromoCodeDto, PromoCode>(PromoCodeMapper::toDomain)
    }

    suspend fun updatePromoCode(promoCode: PromoCode): PromoCode {
        val dto = PromoCodeMapper.toDto(promoCode)

        firestore.collection(PROMOCODES_COLLECTION)
            .document(dto.documentId)
            .set(dto)
            .await()

        return promoCode
    }

    suspend fun deletePromoCode(id: PromoCodeId) {
        firestore.collection(PROMOCODES_COLLECTION)
            .document(id.value)
            .delete()
            .await()
    }

    suspend fun incrementViewCount(id: PromoCodeId) {
        firestore.collection(PROMOCODES_COLLECTION)
            .document(id.value)
            .update("views", FieldValue.increment(1))
            .await()
    }

    suspend fun addComment(
        promoCodeId: PromoCodeId,
        userId: UserId,
        comment: String
    ): PromoCode {
        // Comments are now handled through CommentRepository using subcollections
        // This method is deprecated and should be replaced by CommentRepository.createComment()
        throw UnsupportedOperationException(
            "Comments are now handled through CommentRepository. " +
                "Use CommentRepository.createComment() with parentType=PROMO_CODE instead.",
        )
    }

    suspend fun getPromoCodesByUser(
        userId: UserId,
        limit: Int,
        offset: Int
    ): List<PromoCode> {
        val query = firestore.collection(PROMOCODES_COLLECTION)
            .whereEqualTo("createdBy", userId.value)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        // Note: Firestore doesn't have offset(), implement cursor-based pagination if needed

        val querySnapshot = query.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<PromoCodeDto>()?.let { dto ->
                    PromoCodeMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getPromoCodesByService(serviceName: String): List<PromoCode> {
        val querySnapshot = firestore.collection(PROMOCODES_COLLECTION)
            .whereEqualTo("serviceName", serviceName)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<PromoCodeDto>()?.let { dto ->
                    PromoCodeMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun observePromoCodes(ids: List<PromoCodeId>): Flow<List<PromoCode>> =
        callbackFlow {
            if (ids.isEmpty()) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }

            val listener = firestore.collection(PROMOCODES_COLLECTION)
                .whereIn("__name__", ids.map { it.value })
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val promoCodes = snapshot?.documents?.mapNotNull { document ->
                        try {
                            document.toObject<PromoCodeDto>()?.let { dto ->
                                PromoCodeMapper.toDomain(dto)
                            }
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()

                    trySend(promoCodes)
                }

            awaitClose {
                listener.remove()
            }
        }
}
