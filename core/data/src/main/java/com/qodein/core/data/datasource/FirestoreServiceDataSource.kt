package com.qodein.core.data.datasource

import com.algolia.client.api.SearchClient
import com.algolia.client.model.search.SearchParamsObject
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.dto.ServiceDto
import kotlinx.coroutines.tasks.await

class FirestoreServiceDataSource(private val firestore: FirebaseFirestore, private val searchClient: SearchClient) {
    suspend fun searchServices(
        query: String,
        limit: Int
    ): List<ServiceDto> {
        val searchResponse = searchClient.searchSingleIndex(
            indexName = ServiceDto.COLLECTION_NAME,
            searchParams = SearchParamsObject(
                query = query,
                hitsPerPage = limit,
            ),
        )

        val serviceIds = searchResponse.hits.map { it.objectID }

        return serviceIds.mapNotNull { serviceId ->
            firestore.collection(ServiceDto.COLLECTION_NAME)
                .document(serviceId)
                .get()
                .await()
                .toObject<ServiceDto>()
        }
    }

    suspend fun getPopularServices(limit: Long): List<ServiceDto> =
        firestore.collection(ServiceDto.COLLECTION_NAME)
            .orderBy(ServiceDto.FIELD_PROMO_CODE_COUNT, Query.Direction.DESCENDING)
            .orderBy(ServiceDto.FIELD_NAME, Query.Direction.ASCENDING)
            .limit(limit)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject<ServiceDto>() }
}
