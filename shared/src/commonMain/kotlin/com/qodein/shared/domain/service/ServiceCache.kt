package com.qodein.shared.domain.service

import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.Volatile

/**
 * In-memory cache for services that provides fast lookup by ID.
 * Automatically populated by search results and popular services.
 *
 * KMP-compatible singleton implementation without Java dependencies.
 */
class ServiceCache {

    companion object {
        @Volatile
        private var instance: ServiceCache? = null

        @OptIn(InternalCoroutinesApi::class)
        fun getInstance(): ServiceCache =
            instance ?: synchronized(this) {
                instance ?: ServiceCache().also { instance = it }
            }
    }

    private val _services = MutableStateFlow<Map<String, Service>>(emptyMap())
    val services: StateFlow<Map<String, Service>> = _services.asStateFlow()

    /**
     * Add services to the cache
     */
    fun addServices(newServices: List<Service>) {
        val currentServices = _services.value.toMutableMap()
        newServices.forEach { service ->
            currentServices[service.id.value] = service
        }
        _services.value = currentServices
    }

    /**
     * Get a service by ID
     */
    fun getService(id: ServiceId): Service? = _services.value[id.value]

    /**
     * Get multiple services by IDs
     */
    fun getServices(ids: List<ServiceId>): List<Service> = ids.mapNotNull { _services.value[it.value] }

    /**
     * Get all cached services as a map
     */
    fun getAllServices(): Map<String, Service> = _services.value

    /**
     * Clear the cache
     */
    fun clear() {
        _services.value = emptyMap()
    }
}
