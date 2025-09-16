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
        private const val SERVICES_COLLECTION = "services"
        private const val CACHE_DURATION_MS = 30_000L // 30 seconds
    }

    // Simple in-memory cache for popular services
    private var cachedPopularServices: List<Service>? = null
    private var lastFetchTime: Long = 0L

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
                service.promoCodeCount
            },
        )

        return searchResults.take(limit)
    }

    suspend fun getPopularServices(limit: Int = 20): List<Service> {
        val currentTime = System.currentTimeMillis()

        // Return cached data if it's still fresh
        cachedPopularServices?.let { cached ->
            if (currentTime - lastFetchTime < CACHE_DURATION_MS) {
                Logger.d { "FirestoreServiceDataSource: Returning cached popular services (${cached.size} items)" }
                return cached.take(limit)
            }
        }

        Logger.i { "FirestoreServiceDataSource: Fetching popular services from Firestore (limit=$limit)" }

        val querySnapshot = firestore.collection(SERVICES_COLLECTION)
            .orderBy("promoCodeCount", Query.Direction.DESCENDING)
            .orderBy("name", Query.Direction.ASCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        val services = querySnapshot.documents.mapNotNull { document ->
            document.toObject<ServiceDto>()?.let { dto ->
                ServiceMapper.toDomain(dto)
            }
        }

        // Update cache
        cachedPopularServices = services
        lastFetchTime = currentTime

        Logger.i { "FirestoreServiceDataSource: Retrieved ${services.size} popular services from Firestore" }
        services.forEachIndexed { index, service ->
            Logger.d { "  [$index] ${service.name} (${service.category}) - ${service.promoCodeCount} promos" }
        }

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
