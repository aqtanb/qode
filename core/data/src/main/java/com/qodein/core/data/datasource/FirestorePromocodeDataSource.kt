package com.qodein.core.data.datasource

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.datasource.util.applyPaginationCursor
import com.qodein.core.data.datasource.util.toPagedResult
import com.qodein.core.data.dto.PagedFirestoreResult
import com.qodein.core.data.dto.PromocodeDto
import kotlinx.coroutines.tasks.await

class FirestorePromocodeDataSource(private val firestore: FirebaseFirestore) {
    suspend fun createPromocode(dto: PromocodeDto) {
        firestore.collection(PromocodeDto.COLLECTION_NAME)
            .document(dto.documentId)
            .set(dto)
            .await()
    }

    suspend fun getPromocodes(
        sortByField: String,
        sortDirection: Query.Direction,
        filterByServices: List<String>?,
        limit: Int,
        startAfter: DocumentSnapshot? = null,
        blockedUserIds: List<String>
    ): PagedFirestoreResult<PromocodeDto> {
        val now = Timestamp.now()
        val fetchLimit = limit + 1
        var query = firestore.collection(PromocodeDto.COLLECTION_NAME)
            .whereGreaterThanOrEqualTo(PromocodeDto.FIELD_END_DATE, now)

        // Apply blocked users filter at server level (Firestore limit: max 10 items in whereNotIn)
        if (blockedUserIds.isNotEmpty()) {
            query = query.whereNotIn(PromocodeDto.FIELD_AUTHOR_ID, blockedUserIds.take(10))
        }

        val documents = query
            .applyServiceFilter(filterByServices)
            .orderBy(sortByField, sortDirection)
            .applyPaginationCursor(startAfter)
            .limit(fetchLimit.toLong())
            .get()
            .await()
            .documents

        return documents.toPagedResult(limit)
    }

    suspend fun getPromocodeById(id: String): PromocodeDto? =
        firestore.collection(PromocodeDto.COLLECTION_NAME)
            .document(id)
            .get()
            .await()
            .toObject<PromocodeDto>()

    suspend fun getPromocodesByUser(
        userId: String,
        limit: Int,
        startAfter: DocumentSnapshot?
    ): PagedFirestoreResult<PromocodeDto> {
        val fetchLimit = limit + 1
        val documents = firestore.collection(PromocodeDto.COLLECTION_NAME)
            .whereEqualTo(PromocodeDto.FIELD_AUTHOR_ID, userId)
            .orderBy(PromocodeDto.FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .applyPaginationCursor(startAfter)
            .limit(fetchLimit.toLong())
            .get()
            .await()
            .documents

        return documents.toPagedResult(limit)
    }

    private fun Query.applyServiceFilter(services: List<String>?): Query {
        if (services.isNullOrEmpty()) return this

        return if (services.size == 1) {
            whereEqualTo(PromocodeDto.FIELD_SERVICE_NAME, services.first())
        } else {
            whereIn(PromocodeDto.FIELD_SERVICE_NAME, services)
        }
    }
}
