package com.qodein.core.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.mapper.PromoCodeMapper
import com.qodein.core.data.model.PromoCodeDto
import com.qodein.core.data.model.PromoCodeUsageDto
import com.qodein.core.data.model.PromoCodeVoteDto
import com.qodein.core.domain.repository.PromoCodeSortBy
import com.qodein.core.model.PromoCode
import com.qodein.core.model.PromoCodeId
import com.qodein.core.model.PromoCodeUsage
import com.qodein.core.model.PromoCodeVote
import com.qodein.core.model.UserId
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestorePromoCodeDataSource @Inject constructor(private val firestore: FirebaseFirestore) {
    companion object {
        private const val PROMOCODES_COLLECTION = "promocodes"
        private const val VOTES_COLLECTION = "votes"
        private const val USAGE_COLLECTION = "usage"
    }

    suspend fun createPromoCode(promoCode: PromoCode): PromoCode {
        val dto = PromoCodeMapper.toDto(promoCode)

        firestore.collection(PROMOCODES_COLLECTION)
            .document(dto.id)
            .set(dto)
            .await()

        return promoCode
    }

    suspend fun getPromoCodes(
        query: String?,
        sortBy: PromoCodeSortBy,
        filterByType: String?,
        filterByService: String?,
        filterByCategory: String?,
        isFirstUserOnly: Boolean?,
        limit: Int,
        offset: Int
    ): List<PromoCode> {
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
                firestoreQuery.orderBy("upvotes", Query.Direction.DESCENDING)
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
            PromoCodeSortBy.MOST_VIEWED -> {
                firestoreQuery.orderBy("views", Query.Direction.DESCENDING)
            }
            PromoCodeSortBy.MOST_USED -> {
                firestoreQuery.orderBy("createdAt", Query.Direction.DESCENDING) // Fallback to newest
            }
            PromoCodeSortBy.ALPHABETICAL -> {
                firestoreQuery.orderBy("code", Query.Direction.ASCENDING)
            }
        }

        // Apply pagination
        // Note: Firestore doesn't have offset(), using limit only for now
        // For proper pagination, implement cursor-based pagination with startAfter()
        firestoreQuery = firestoreQuery.limit(limit.toLong())

        val querySnapshot = firestoreQuery.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<PromoCodeDto>()?.let { dto ->
                    PromoCodeMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                // Log error and skip malformed documents
                null
            }
        }
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

    suspend fun updatePromoCode(promoCode: PromoCode): PromoCode {
        val dto = PromoCodeMapper.toDto(promoCode)

        firestore.collection(PROMOCODES_COLLECTION)
            .document(dto.id)
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

    suspend fun recordUsage(usage: PromoCodeUsage): PromoCodeUsage {
        val dto = PromoCodeMapper.usageToDto(usage)

        firestore.collection(USAGE_COLLECTION)
            .document(dto.id)
            .set(dto)
            .await()

        return usage
    }

    suspend fun getUsageStatistics(promoCodeId: PromoCodeId): List<PromoCodeUsage> {
        val querySnapshot = firestore.collection(USAGE_COLLECTION)
            .whereEqualTo("promoCodeId", promoCodeId.value)
            .orderBy("usedAt", Query.Direction.DESCENDING)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<PromoCodeUsageDto>()?.let { dto ->
                    PromoCodeMapper.usageToDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun addComment(
        promoCodeId: PromoCodeId,
        userId: UserId,
        comment: String
    ): PromoCode {
        val promoCodeRef = firestore.collection(PROMOCODES_COLLECTION).document(promoCodeId.value)

        // Get current promo code
        val currentDoc = promoCodeRef.get().await()
        val currentDto = currentDoc.toObject<PromoCodeDto>()
            ?: throw IllegalStateException("PromoCode not found")

        // Add comment to existing list
        val updatedComments = (currentDto.comments ?: emptyList()) + "$userId: $comment"

        // Update with new comments list
        promoCodeRef.update("comments", updatedComments).await()

        // Return updated promo code
        val updatedDoc = promoCodeRef.get().await()
        val updatedDto = updatedDoc.toObject<PromoCodeDto>()
            ?: throw IllegalStateException("Failed to get updated PromoCode")

        return PromoCodeMapper.toDomain(updatedDto)
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
