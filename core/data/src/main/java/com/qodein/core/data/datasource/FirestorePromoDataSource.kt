package com.qodein.core.data.datasource

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.mapper.PromoMapper
import com.qodein.core.data.model.PromoDto
import com.qodein.shared.domain.repository.PromoSortBy
import com.qodein.shared.model.Promo
import com.qodein.shared.model.PromoId
import com.qodein.shared.model.UserId
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestorePromoDataSource @Inject constructor(private val firestore: FirebaseFirestore) {
    companion object {
        private const val TAG = "FirestorePromoDS"
        private const val PROMOS_COLLECTION = "promos"
        private const val VOTES_SUBCOLLECTION = "votes"
        private const val BOOKMARKS_SUBCOLLECTION = "bookmarks"
    }

    suspend fun createPromo(promo: Promo): Promo {
        val dto = PromoMapper.toDto(promo)

        try {
            firestore.collection(PROMOS_COLLECTION)
                .document(dto.id)
                .set(dto)
                .await()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error in createPromo")

            when {
                e.message?.contains("ALREADY_EXISTS", ignoreCase = true) == true ||
                    e.message?.contains("already exists", ignoreCase = true) == true -> {
                    throw IllegalArgumentException("promo already exists with ID: ${promo.id.value}", e)
                }
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true -> {
                    throw SecurityException("permission denied: cannot create promo", e)
                }
                e.message?.contains("network", ignoreCase = true) == true ||
                    e.message?.contains("timeout", ignoreCase = true) == true -> {
                    throw IOException("connection error while creating promo", e)
                }
                else -> {
                    throw IllegalStateException("service unavailable: failed to create promo", e)
                }
            }
        }

        return promo
    }

    suspend fun getPromos(
        query: String?,
        sortBy: PromoSortBy,
        filterByService: String?,
        filterByCategory: String?,
        filterByCountry: String?,
        includeExpired: Boolean,
        limit: Int,
        offset: Int
    ): List<Promo> {
        Timber.tag(TAG).d("getPromos: Starting query with sortBy=$sortBy, limit=$limit")
        var firestoreQuery: Query = firestore.collection(PROMOS_COLLECTION)

        // Apply filters
        query?.let { searchQuery ->
            if (searchQuery.isNotBlank()) {
                // Simple text search on title - for better search, consider using Algolia
                firestoreQuery = firestoreQuery
                    .whereGreaterThanOrEqualTo("title", searchQuery)
                    .whereLessThanOrEqualTo("title", searchQuery + "\uf8ff")
            }
        }

        filterByService?.let { service ->
            firestoreQuery = firestoreQuery.whereEqualTo("serviceName", service)
        }

        filterByCategory?.let { category ->
            firestoreQuery = firestoreQuery.whereEqualTo("category", category)
        }

        filterByCountry?.let { country ->
            // Filter by target countries - either global (empty array) or containing the country
            firestoreQuery = firestoreQuery.whereArrayContains("targetCountries", country.uppercase())
        }

        // Filter expired promos unless explicitly included
        if (!includeExpired) {
            val now = Timestamp.now()
            firestoreQuery = firestoreQuery.whereGreaterThan("expiresAt", now)
        }

        // Apply sorting
        firestoreQuery = when (sortBy) {
            PromoSortBy.POPULARITY -> {
                // Sort by vote score - may need composite index
                firestoreQuery.orderBy("upvotes", Query.Direction.DESCENDING)
            }
            PromoSortBy.NEWEST -> {
                firestoreQuery.orderBy("createdAt", Query.Direction.DESCENDING)
            }
            PromoSortBy.OLDEST -> {
                firestoreQuery.orderBy("createdAt", Query.Direction.ASCENDING)
            }
            PromoSortBy.EXPIRING_SOON -> {
                firestoreQuery.orderBy("expiresAt", Query.Direction.ASCENDING)
            }
            PromoSortBy.MOST_VIEWED -> {
                firestoreQuery.orderBy("views", Query.Direction.DESCENDING)
            }
            PromoSortBy.MOST_SHARED -> {
                firestoreQuery.orderBy("shares", Query.Direction.DESCENDING)
            }
            PromoSortBy.TRENDING -> {
                // For trending, use a combination - for now, fallback to newest
                firestoreQuery.orderBy("createdAt", Query.Direction.DESCENDING)
            }
        }

        // Apply pagination
        firestoreQuery = firestoreQuery.limit(limit.toLong())

        val querySnapshot = firestoreQuery.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<PromoDto>()?.let { dto ->
                    PromoMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to parse promo ${document.id}")
                null
            }
        }
    }

    suspend fun getPromoById(id: PromoId): Promo? {
        val document = firestore.collection(PROMOS_COLLECTION)
            .document(id.value)
            .get()
            .await()

        return document.toObject<PromoDto>()?.let { dto ->
            PromoMapper.toDomain(dto)
        }
    }

    suspend fun updatePromo(promo: Promo): Promo {
        val dto = PromoMapper.toDto(promo)

        firestore.collection(PROMOS_COLLECTION)
            .document(dto.id)
            .set(dto)
            .await()

        return promo
    }

    suspend fun deletePromo(
        id: PromoId,
        createdBy: UserId
    ) {
        val promoRef = firestore.collection(PROMOS_COLLECTION).document(id.value)

        // Check if user owns the promo
        val promoDoc = promoRef.get().await()
        val promoDto = promoDoc.toObject<PromoDto>()
            ?: throw IllegalArgumentException("promo not found: ${id.value}")

        if (promoDto.createdBy != createdBy.value) {
            throw SecurityException("permission denied: cannot delete promo owned by another user")
        }

        promoRef.delete().await()
    }

    suspend fun voteOnPromo(
        promoId: PromoId,
        userId: UserId,
        isUpvote: Boolean
    ): Promo {
        val promoRef = firestore.collection(PROMOS_COLLECTION).document(promoId.value)
        val voteId = "${userId.value}_${promoId.value}"
        val voteRef = promoRef.collection(VOTES_SUBCOLLECTION).document(voteId)

        // Use batch write to ensure atomicity
        val batch = firestore.batch()

        // Add/update vote
        val voteData = mapOf(
            "userId" to userId.value,
            "promoId" to promoId.value,
            "isUpvote" to isUpvote,
            "createdAt" to FieldValue.serverTimestamp(),
        )
        batch.set(voteRef, voteData)

        // Update vote counts on promo
        if (isUpvote) {
            batch.update(promoRef, "upvotes", FieldValue.increment(1))
        } else {
            batch.update(promoRef, "downvotes", FieldValue.increment(1))
        }

        // Auto-verify promo if it reaches 10+ upvotes
        val currentDoc = promoRef.get().await()
        val currentDto = currentDoc.toObject<PromoDto>()
        if (currentDto != null && !currentDto.isVerified) {
            val newUpvotes = if (isUpvote) currentDto.upvotes + 1 else currentDto.upvotes
            if (newUpvotes >= 10) {
                batch.update(promoRef, "isVerified", true)
            }
        }

        batch.commit().await()

        // Return updated promo
        val updatedDoc = promoRef.get().await()
        val updatedDto = updatedDoc.toObject<PromoDto>()
            ?: throw IllegalStateException("service unavailable: failed to retrieve updated promo")

        return PromoMapper.toDomain(updatedDto)
    }

    suspend fun removePromoVote(
        promoId: PromoId,
        userId: UserId
    ): Promo {
        val promoRef = firestore.collection(PROMOS_COLLECTION).document(promoId.value)
        val voteId = "${userId.value}_${promoId.value}"
        val voteRef = promoRef.collection(VOTES_SUBCOLLECTION).document(voteId)

        // Get existing vote to know which counter to decrement
        val existingVoteSnapshot = voteRef.get().await()
        val existingVote = existingVoteSnapshot.data

        if (existingVote != null) {
            val isUpvote = existingVote["isUpvote"] as Boolean
            val batch = firestore.batch()

            // Remove vote
            batch.delete(voteRef)

            // Update vote counts on promo
            if (isUpvote) {
                batch.update(promoRef, "upvotes", FieldValue.increment(-1))
            } else {
                batch.update(promoRef, "downvotes", FieldValue.increment(-1))
            }

            batch.commit().await()
        }

        // Return updated promo
        val updatedDoc = promoRef.get().await()
        val updatedDto = updatedDoc.toObject<PromoDto>()
            ?: throw IllegalStateException("service unavailable: failed to retrieve updated promo")

        return PromoMapper.toDomain(updatedDto)
    }

    suspend fun bookmarkPromo(
        promoId: PromoId,
        userId: UserId,
        isBookmarked: Boolean
    ): Promo {
        val promoRef = firestore.collection(PROMOS_COLLECTION).document(promoId.value)
        val bookmarkId = "${userId.value}_${promoId.value}"
        val bookmarkRef = promoRef.collection(BOOKMARKS_SUBCOLLECTION).document(bookmarkId)

        if (isBookmarked) {
            // Add bookmark
            val bookmarkData = mapOf(
                "userId" to userId.value,
                "promoId" to promoId.value,
                "createdAt" to FieldValue.serverTimestamp(),
            )
            bookmarkRef.set(bookmarkData).await()
        } else {
            // Remove bookmark
            bookmarkRef.delete().await()
        }

        // Return updated promo (bookmark status would be computed at query time)
        val updatedDoc = promoRef.get().await()
        val updatedDto = updatedDoc.toObject<PromoDto>()
            ?: throw IllegalStateException("service unavailable: failed to retrieve updated promo")

        return PromoMapper.toDomain(updatedDto)
    }

    suspend fun incrementViewCount(id: PromoId) {
        firestore.collection(PROMOS_COLLECTION)
            .document(id.value)
            .update("views", FieldValue.increment(1))
            .await()
    }

    suspend fun incrementShareCount(id: PromoId) {
        firestore.collection(PROMOS_COLLECTION)
            .document(id.value)
            .update("shares", FieldValue.increment(1))
            .await()
    }

    suspend fun getPromosByUser(
        userId: UserId,
        limit: Int,
        offset: Int
    ): List<Promo> {
        val query = firestore.collection(PROMOS_COLLECTION)
            .whereEqualTo("createdBy", userId.value)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        val querySnapshot = query.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<PromoDto>()?.let { dto ->
                    PromoMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getPromosByService(
        serviceName: String,
        limit: Int,
        offset: Int
    ): List<Promo> {
        val query = firestore.collection(PROMOS_COLLECTION)
            .whereEqualTo("serviceName", serviceName)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        val querySnapshot = query.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<PromoDto>()?.let { dto ->
                    PromoMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getPromosByCategory(
        category: String,
        limit: Int,
        offset: Int
    ): List<Promo> {
        val query = firestore.collection(PROMOS_COLLECTION)
            .whereEqualTo("category", category)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        val querySnapshot = query.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<PromoDto>()?.let { dto ->
                    PromoMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getPromosByCountry(
        countryCode: String,
        limit: Int,
        offset: Int
    ): List<Promo> {
        val upperCountryCode = countryCode.uppercase()

        // Get global promos (empty targetCountries) and country-specific promos
        val globalQuery = firestore.collection(PROMOS_COLLECTION)
            .whereEqualTo("targetCountries", emptyList<String>())
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit((limit / 2).toLong())
            .get()
            .await()

        val countryQuery = firestore.collection(PROMOS_COLLECTION)
            .whereArrayContains("targetCountries", upperCountryCode)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit((limit / 2).toLong())
            .get()
            .await()

        val globalPromos = globalQuery.documents.mapNotNull { document ->
            try {
                document.toObject<PromoDto>()?.let { dto ->
                    PromoMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }

        val countryPromos = countryQuery.documents.mapNotNull { document ->
            try {
                document.toObject<PromoDto>()?.let { dto ->
                    PromoMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }

        return (globalPromos + countryPromos)
            .distinctBy { it.id.value }
            .sortedByDescending { it.createdAt }
            .take(limit)
    }

    suspend fun getBookmarkedPromos(
        userId: UserId,
        limit: Int,
        offset: Int
    ): List<Promo> {
        // This requires a more complex query - in production, consider maintaining
        // a user bookmarks collection for better performance

        // For now, return empty list - implement user bookmarks collection separately
        return emptyList()
    }

    suspend fun getExpiringPromos(
        daysAhead: Int,
        limit: Int
    ): List<Promo> {
        val now = System.currentTimeMillis()
        val futureTime = now + TimeUnit.DAYS.toMillis(daysAhead.toLong())

        val query = firestore.collection(PROMOS_COLLECTION)
            .whereGreaterThan("expiresAt", Timestamp(now / 1000, 0))
            .whereLessThanOrEqualTo("expiresAt", Timestamp(futureTime / 1000, 0))
            .orderBy("expiresAt", Query.Direction.ASCENDING)
            .limit(limit.toLong())

        val querySnapshot = query.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<PromoDto>()?.let { dto ->
                    PromoMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getTrendingPromos(
        timeWindow: Int,
        limit: Int
    ): List<Promo> {
        // Simplified trending: promos created in the last timeWindow hours, sorted by vote score
        val cutoffTime = System.currentTimeMillis() - (timeWindow * 60 * 60 * 1000)

        val query = firestore.collection(PROMOS_COLLECTION)
            .whereGreaterThan("createdAt", Timestamp(cutoffTime / 1000, 0))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .orderBy("upvotes", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        val querySnapshot = query.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<PromoDto>()?.let { dto ->
                    PromoMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getVerifiedPromos(
        limit: Int,
        offset: Int
    ): List<Promo> {
        val query = firestore.collection(PROMOS_COLLECTION)
            .whereEqualTo("isVerified", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        val querySnapshot = query.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<PromoDto>()?.let { dto ->
                    PromoMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun observePromos(ids: List<PromoId>): Flow<List<Promo>> =
        callbackFlow {
            if (ids.isEmpty()) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }

            val listener = firestore.collection(PROMOS_COLLECTION)
                .whereIn("__name__", ids.map { it.value })
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        val wrappedException = when {
                            error.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                                SecurityException("permission denied: cannot observe promos", error)
                            error.message?.contains("network", ignoreCase = true) == true ->
                                IOException("connection error while observing promos", error)
                            else -> IllegalStateException("service unavailable: failed to observe promos", error)
                        }
                        close(wrappedException)
                        return@addSnapshotListener
                    }

                    val promos = snapshot?.documents?.mapNotNull { document ->
                        try {
                            document.toObject<PromoDto>()?.let { dto ->
                                PromoMapper.toDomain(dto)
                            }
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()

                    trySend(promos)
                }

            awaitClose {
                listener.remove()
            }
        }
}
