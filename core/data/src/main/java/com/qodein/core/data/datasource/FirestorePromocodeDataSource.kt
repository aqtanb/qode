package com.qodein.core.data.datasource

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.dto.PagedPromocodesDto
import com.qodein.core.data.dto.PromocodeDto
import kotlinx.coroutines.tasks.await

class FirestorePromocodeDataSource(private val firestore: FirebaseFirestore) {
    suspend fun createPromocode(dto: PromocodeDto) {
        firestore.collection(PromocodeDto.COLLECTION_NAME)
            .document(dto.documentId)
            .set(dto)
            .await()
    }

    suspend fun getPromoCodes(
        sortByField: String,
        sortDirection: Query.Direction,
        filterByServices: List<String>?,
        limit: Int,
        startAfter: DocumentSnapshot? = null
    ): PagedPromocodesDto {
        val documents = firestore.collection(PromocodeDto.COLLECTION_NAME)
            .applyServiceFilter(filterByServices)
            .orderBy(sortByField, sortDirection)
            .applyPaginationCursor(startAfter)
            .limit(limit.toLong())
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

    private fun Query.applyPaginationCursor(cursor: DocumentSnapshot?): Query = cursor?.let { startAfter(it) } ?: this

    private fun List<DocumentSnapshot>.toPagedResult(limit: Int): PagedPromocodesDto {
        val items = mapNotNull { it.toObject<PromocodeDto>() }
        val lastDocument = lastOrNull()
        val hasMore = size == limit

        return PagedPromocodesDto(
            items = items,
            lastDocument = lastDocument,
            hasMore = hasMore,
        )
    }

    suspend fun getPromocodeById(id: String): PromocodeDto? =
        firestore.collection(PromocodeDto.COLLECTION_NAME)
            .document(id)
            .get()
            .await()
            .toObject<PromocodeDto>()
}
