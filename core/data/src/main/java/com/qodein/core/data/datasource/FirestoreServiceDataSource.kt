package com.qodein.core.data.datasource

import co.touchlab.kermit.Logger
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.mapper.ServiceMapper
import com.qodein.core.data.model.ServiceDto
import com.qodein.shared.model.Service
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreServiceDataSource @Inject constructor(private val firestore: FirebaseFirestore) {
    companion object {
        private const val TAG = "FirestoreServiceDS"
        private const val SERVICES_COLLECTION = "services"
    }

    suspend fun createService(service: Service): Service {
        val dto = ServiceMapper.toDto(service)

        firestore.collection(SERVICES_COLLECTION)
            .document(dto.documentId)
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

        val allServicesQuery = firestore.collection(SERVICES_COLLECTION)
            .get()
            .await()

        val allServices = allServicesQuery.documents.mapNotNull { document ->
            document.toObject<ServiceDto>()?.let { dto ->
                ServiceMapper.toDomain(dto)
            }
        }

        val searchResults = allServices.filter { service ->
            val serviceName = service.name.lowercase()
            val serviceCategory = service.category.lowercase()

            serviceName.contains(queryLower) || serviceCategory.contains(queryLower)
        }.sortedWith(
            compareByDescending<Service> { service ->
                val serviceName = service.name.lowercase()
                val serviceCategory = service.category.lowercase()

                when {
                    serviceName == queryLower -> 5
                    serviceName.startsWith(queryLower) -> 4
                    serviceName.contains(queryLower) -> 3
                    serviceCategory == queryLower -> 2
                    serviceCategory.contains(queryLower) -> 1
                    else -> 0
                }
            }.thenByDescending { service ->
                if (service.isPopular) 1 else 0
            }.thenByDescending { service ->
                service.promoCodeCount
            },
        )

        return searchResults.take(limit)
    }

    suspend fun getPopularServices(limit: Int = 20): List<Service> {
        Logger.d { "$TAG: getPopularServices called with limit=$limit" }
        val querySnapshot = firestore.collection(SERVICES_COLLECTION)
            .orderBy("promoCodeCount", Query.Direction.DESCENDING)
            .orderBy("name", Query.Direction.ASCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        Logger.d { "$TAG: Query completed, found ${querySnapshot.documents.size} documents" }
        querySnapshot.documents.forEachIndexed { index, document ->
            Logger.d { "$TAG: Document $index: ${document.id} - data: ${document.data}" }
        }

        val services = querySnapshot.documents.mapNotNull { document ->
            document.toObject<ServiceDto>()?.let { dto ->
                Logger.d { "$TAG: Mapping service: ${dto.name} with promoCodeCount=${dto.promoCodeCount}" }
                ServiceMapper.toDomain(dto)
            }
        }

        Logger.d { "$TAG: Returning ${services.size} services" }
        return services
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
            document.toObject<ServiceDto>()?.let { dto ->
                ServiceMapper.toDomain(dto)
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
            document.toObject<ServiceDto>()?.let { dto ->
                ServiceMapper.toDomain(dto)
            }
        }
    }
}
