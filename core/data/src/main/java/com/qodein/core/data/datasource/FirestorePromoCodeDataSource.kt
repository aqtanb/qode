package com.qodein.core.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.cache.QueryCache
import com.qodein.core.data.mapper.PromoCodeMapper
import com.qodein.core.data.mapper.ServiceMapper
import com.qodein.core.data.model.PromoCodeDto
import com.qodein.core.data.model.PromoCodeVoteDto
import com.qodein.core.data.model.ServiceDto
import com.qodein.shared.domain.repository.PromoCodeSortBy
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationCursor
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.PromoCodeVote
import com.qodein.shared.model.Service
import com.qodein.shared.model.UserId
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestorePromoCodeDataSource @Inject constructor(private val firestore: FirebaseFirestore, private val queryCache: QueryCache) {
    companion object {
        private const val TAG = "FirestorePromoCodeDS"
        private const val PROMOCODES_COLLECTION = "promocodes"
        private const val VOTES_COLLECTION = "votes"
        private const val SERVICES_COLLECTION = "services"
    }

    suspend fun createPromoCode(promoCode: PromoCode): PromoCode {
        val dto = PromoCodeMapper.toDto(promoCode)

        try {
            // Use add() with specific document ID to ensure document doesn't already exist
            // This will fail if a document with this ID already exists
            firestore.collection(PROMOCODES_COLLECTION)
                .document(dto.documentId)
                .set(dto)
                .await()
        } catch (e: Exception) {
            // Log error at data layer boundary following NIA patterns
            Timber.tag(TAG).e(e, "Error in createPromoCode")

            // Provide specific error context for extension function parsing
            when {
                e.message?.contains("ALREADY_EXISTS", ignoreCase = true) == true ||
                    e.message?.contains("already exists", ignoreCase = true) == true -> {
                    throw IllegalArgumentException("promo code already exists for service '${promoCode.serviceName}': ${promoCode.code}", e)
                }
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true -> {
                    throw SecurityException("permission denied: cannot create promo code", e)
                }
                e.message?.contains("network", ignoreCase = true) == true ||
                    e.message?.contains("timeout", ignoreCase = true) == true -> {
                    throw IOException("connection error while creating promo code", e)
                }
                else -> {
                    throw IllegalStateException("service unavailable: failed to create promo code", e)
                }
            }
        }

        return promoCode
    }

    suspend fun getPromoCodes(
        query: String?,
        sortBy: PromoCodeSortBy,
        filterByType: String?,
        filterByService: String?,
        filterByCategory: String?,
        isFirstUserOnly: Boolean?,
        paginationRequest: PaginationRequest
    ): PaginatedResult<PromoCode> {
        val isFirstPage = paginationRequest.cursor == null

        // Check cache for first page queries
        if (isFirstPage) {
            queryCache.get(
                query = query,
                sortBy = sortBy.name,
                filterByType = filterByType,
                filterByService = filterByService,
                filterByCategory = filterByCategory,
                isFirstUserOnly = isFirstUserOnly,
                isFirstPage = true,
            )?.let { cachedResult ->
                Timber.tag(TAG).d("getPromoCodes: Returning cached result (${cachedResult.data.size} items)")
                return cachedResult
            }
        }

        Timber.tag(
            TAG,
        ).d(
            "getPromoCodes: Starting query with sortBy=$sortBy, limit=${paginationRequest.limit}, cursor=${paginationRequest.cursor?.documentId}",
        )
        var firestoreQuery: Query = firestore.collection(PROMOCODES_COLLECTION)

        // Apply filters
        query?.let { searchQuery ->
            if (searchQuery.isNotBlank()) {
                // Firestore text search is limited, this is a basic implementation
                // For better search, consider using Algolia or similar
                firestoreQuery = firestoreQuery
                    .whereGreaterThanOrEqualTo("code", searchQuery.uppercase())
                    .whereLessThanOrEqualTo("code", searchQuery.uppercase() + "\uf8ff")
            }
        }

        filterByType?.let { type ->
            firestoreQuery = firestoreQuery.whereEqualTo("type", type)
        }

        filterByService?.let { service ->
            firestoreQuery = firestoreQuery.whereEqualTo("serviceName", service)
        }

        filterByCategory?.let { category ->
            firestoreQuery = firestoreQuery.whereEqualTo("category", category)
        }

        isFirstUserOnly?.let { firstUserOnly ->
            firestoreQuery = firestoreQuery.whereEqualTo("isFirstUserOnly", firstUserOnly)
        }

        // Apply sorting
        firestoreQuery = when (sortBy) {
            PromoCodeSortBy.POPULARITY -> {
                // Sort by vote score (upvotes - downvotes) - requires composite index
                firestoreQuery.orderBy("voteScore", Query.Direction.DESCENDING)
            }
            PromoCodeSortBy.NEWEST -> {
                firestoreQuery.orderBy("createdAt", Query.Direction.DESCENDING)
            }
            PromoCodeSortBy.OLDEST -> {
                firestoreQuery.orderBy("createdAt", Query.Direction.ASCENDING)
            }
            PromoCodeSortBy.EXPIRING_SOON -> {
                firestoreQuery.orderBy("endDate", Query.Direction.ASCENDING)
            }
            PromoCodeSortBy.ALPHABETICAL -> {
                firestoreQuery.orderBy("serviceName", Query.Direction.ASCENDING)
            }
        }

        // Apply cursor-based pagination (Firebase best practice)
        paginationRequest.cursor?.let { cursor ->
            // Use startAfter with field values to avoid extra document read
            cursor.sortFieldValue?.let { sortValue ->
                firestoreQuery = when (sortBy) {
                    PromoCodeSortBy.POPULARITY -> {
                        firestoreQuery.startAfter(sortValue, cursor.documentId)
                    }
                    PromoCodeSortBy.NEWEST, PromoCodeSortBy.OLDEST -> {
                        firestoreQuery.startAfter(sortValue, cursor.documentId)
                    }
                    PromoCodeSortBy.EXPIRING_SOON -> {
                        firestoreQuery.startAfter(sortValue, cursor.documentId)
                    }
                    PromoCodeSortBy.ALPHABETICAL -> {
                        firestoreQuery.startAfter(sortValue, cursor.documentId)
                    }
                }
                Timber.tag(TAG).d("getPromoCodes: Applied cursor startAfter with sortValue=$sortValue, docId=${cursor.documentId}")
            } ?: run {
                Timber.tag(TAG).w("getPromoCodes: Cursor missing sortFieldValue, skipping pagination")
            }
        }

        // Apply limit
        firestoreQuery = firestoreQuery.limit(paginationRequest.limit.toLong())

        Timber.tag(TAG).d("getPromoCodes: Executing Firestore query...")
        val querySnapshot = firestoreQuery.get().await()
        Timber.tag(TAG).d("getPromoCodes: Query returned ${querySnapshot.documents.size} documents")

        val documents = querySnapshot.documents

        val results = documents.mapNotNull { document ->
            try {
                Timber.tag(TAG).d("getPromoCodes: Processing document ${document.id}")
                Timber.tag(TAG).d("  Document data: ${document.data}")

                val dto = document.toObject<PromoCodeDto>()
                if (dto == null) {
                    Timber.tag(TAG).w("getPromoCodes: Document ${document.id} failed to convert to DTO")
                    return@mapNotNull null
                }

                Timber.tag(TAG).d("  DTO: code=${dto.code}, type=${dto.type}, title=${dto.title}")
                Timber.tag(TAG).d("  Dates: start=${dto.startDate}, end=${dto.endDate}")

                val domainModel = PromoCodeMapper.toDomain(dto)
                Timber.tag(TAG).d("  Successfully converted to domain model: ${domainModel.code}")
                domainModel
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "getPromoCodes: Failed to parse document ${document.id}")
                Timber.tag(TAG).e("  Document data: ${document.data}")
                null
            }
        }

        // Create next cursor from the last document if available
        val nextCursor = if (documents.isNotEmpty() && results.size == paginationRequest.limit) {
            val lastDoc = documents.last()
            val sortFieldValue = when (sortBy) {
                PromoCodeSortBy.POPULARITY -> lastDoc.getLong("voteScore")
                PromoCodeSortBy.NEWEST, PromoCodeSortBy.OLDEST -> lastDoc.getTimestamp("createdAt")
                PromoCodeSortBy.EXPIRING_SOON -> lastDoc.getTimestamp("endDate")
                PromoCodeSortBy.ALPHABETICAL -> lastDoc.getString("serviceName")
            }
            PaginationCursor.fromDocumentSnapshot(
                documentId = lastDoc.id,
                sortFieldValue = sortFieldValue,
            )
        } else {
            null
        }

        val hasMore = results.size == paginationRequest.limit

        Timber.tag(TAG).d("getPromoCodes: Successfully parsed ${results.size} promo codes")
        results.forEachIndexed { index, promoCode ->
            Timber.tag(TAG).d("  [$index] ${promoCode.code} for ${promoCode.serviceName} (upvotes: ${promoCode.upvotes})")
        }
        Timber.tag(TAG).d("getPromoCodes: hasMore=$hasMore, nextCursor=${nextCursor?.documentId}")

        val result = PaginatedResult.of(
            data = results,
            nextCursor = nextCursor,
            hasMore = hasMore,
        )

        // Cache first page results for popular queries
        if (isFirstPage && results.isNotEmpty()) {
            queryCache.put(
                query = query,
                sortBy = sortBy.name,
                filterByType = filterByType,
                filterByService = filterByService,
                filterByCategory = filterByCategory,
                isFirstUserOnly = isFirstUserOnly,
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

        return document.toObject<PromoCodeDto>()?.let { dto ->
            PromoCodeMapper.toDomain(dto)
        }
    }

    suspend fun getPromoCodeByCode(code: String): PromoCode? {
        val querySnapshot = firestore.collection(PROMOCODES_COLLECTION)
            .whereEqualTo("code", code.uppercase())
            .limit(1)
            .get()
            .await()

        return querySnapshot.documents.firstOrNull()?.toObject<PromoCodeDto>()?.let { dto ->
            PromoCodeMapper.toDomain(dto)
        }
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

        return document.toObject<PromoCodeDto>()?.let { dto ->
            PromoCodeMapper.toDomain(dto)
        }
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

    suspend fun voteOnPromoCode(
        promoCodeId: PromoCodeId,
        userId: UserId,
        isUpvote: Boolean
    ): PromoCodeVote {
        val voteDto = PromoCodeVoteDto(
            id = "${promoCodeId.value}_${userId.value}",
            promoCodeId = promoCodeId.value,
            userId = userId.value,
            isUpvote = isUpvote,
        )

        // Use batch write to ensure atomicity
        val batch = firestore.batch()

        // Add/update vote
        val voteRef = firestore.collection(VOTES_COLLECTION).document(voteDto.id)
        batch.set(voteRef, voteDto)

        // Update vote counts on promocode
        val promoCodeRef = firestore.collection(PROMOCODES_COLLECTION).document(promoCodeId.value)
        if (isUpvote) {
            batch.update(promoCodeRef, "upvotes", FieldValue.increment(1))
        } else {
            batch.update(promoCodeRef, "downvotes", FieldValue.increment(1))
        }

        batch.commit().await()

        return PromoCodeMapper.voteToDomain(voteDto)
    }

    suspend fun removeVote(
        promoCodeId: PromoCodeId,
        userId: UserId
    ) {
        val voteId = "${promoCodeId.value}_${userId.value}"

        // Get existing vote to know which counter to decrement
        val existingVoteSnapshot = firestore.collection(VOTES_COLLECTION)
            .document(voteId)
            .get()
            .await()

        val existingVote = existingVoteSnapshot.toObject<PromoCodeVoteDto>()

        if (existingVote != null) {
            val batch = firestore.batch()

            // Remove vote
            val voteRef = firestore.collection(VOTES_COLLECTION).document(voteId)
            batch.delete(voteRef)

            // Update vote counts on promocode
            val promoCodeRef = firestore.collection(PROMOCODES_COLLECTION).document(promoCodeId.value)
            if (existingVote.isUpvote) {
                batch.update(promoCodeRef, "upvotes", FieldValue.increment(-1))
            } else {
                batch.update(promoCodeRef, "downvotes", FieldValue.increment(-1))
            }

            batch.commit().await()
        }
    }

    suspend fun getUserVote(
        promoCodeId: PromoCodeId,
        userId: UserId
    ): PromoCodeVote? {
        val voteId = "${promoCodeId.value}_${userId.value}"

        val document = firestore.collection(VOTES_COLLECTION)
            .document(voteId)
            .get()
            .await()

        return document.toObject<PromoCodeVoteDto>()?.let { dto ->
            PromoCodeMapper.voteToDomain(dto)
        }
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
                        val wrappedException = when {
                            error.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                                SecurityException("permission denied: cannot observe promo codes", error)
                            error.message?.contains("network", ignoreCase = true) == true ->
                                IOException("connection error while observing promo codes", error)
                            else -> IllegalStateException("service unavailable: failed to observe promo codes", error)
                        }
                        close(wrappedException)
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

    // Service-related methods
    suspend fun createService(service: Service): Service {
        val dto = ServiceMapper.toDto(service)

        firestore.collection(SERVICES_COLLECTION)
            .document(dto.id)
            .set(dto)
            .await()

        return service
    }

    suspend fun searchServices(
        query: String,
        limit: Int = 20
    ): List<Service> {
        if (query.isBlank()) {
            return getPopularServices(limit)
        }

        val queryLower = query.lowercase().trim()

        // Search by name (prefix match)
        val nameQuery = firestore.collection(SERVICES_COLLECTION)
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + "\uf8ff")
            .limit(limit.toLong())
            .get()
            .await()

        val nameResults = nameQuery.documents.mapNotNull { document ->
            try {
                document.toObject<ServiceDto>()?.let { dto ->
                    ServiceMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }

        // If we have enough results, return them
        if (nameResults.size >= limit) {
            return nameResults.take(limit)
        }

        // Otherwise, search by category as well
        val categoryQuery = firestore.collection(SERVICES_COLLECTION)
            .whereGreaterThanOrEqualTo("category", query)
            .whereLessThanOrEqualTo("category", query + "\uf8ff")
            .limit((limit - nameResults.size).toLong())
            .get()
            .await()

        val categoryResults = categoryQuery.documents.mapNotNull { document ->
            try {
                document.toObject<ServiceDto>()?.let { dto ->
                    ServiceMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }

        // Combine results and remove duplicates
        val allResults = (nameResults + categoryResults).distinctBy { it.id.value }

        // Sort by relevance: exact matches first, then popular services
        return allResults.sortedWith(
            compareBy<Service> { service ->
                when {
                    service.name.equals(query, ignoreCase = true) -> 0
                    service.name.startsWith(query, ignoreCase = true) -> 1
                    service.category.equals(query, ignoreCase = true) -> 2
                    service.isPopular -> 3
                    else -> 4
                }
            }.thenBy { it.name },
        )
            .take(limit)
    }

    suspend fun getPopularServices(limit: Int = 10): List<Service> {
        val querySnapshot = firestore.collection(SERVICES_COLLECTION)
            .whereEqualTo("isPopular", true)
            .orderBy("name", Query.Direction.ASCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<ServiceDto>()?.let { dto ->
                    ServiceMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getServicesByCategory(
        category: String,
        limit: Int = 20
    ): List<Service> {
        val querySnapshot = firestore.collection(SERVICES_COLLECTION)
            .whereEqualTo("category", category)
            .orderBy("name", Query.Direction.ASCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<ServiceDto>()?.let { dto ->
                    ServiceMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getAllServices(limit: Int = 100): List<Service> {
        val querySnapshot = firestore.collection(SERVICES_COLLECTION)
            .orderBy("name", Query.Direction.ASCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<ServiceDto>()?.let { dto ->
                    ServiceMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}
