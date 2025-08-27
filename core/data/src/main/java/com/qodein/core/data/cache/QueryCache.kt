package com.qodein.core.data.cache

import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PromoCode
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple in-memory cache for promo code queries to reduce Firebase read costs.
 * Caches first page results for popular query patterns.
 */
@Singleton
class QueryCache @Inject constructor() {
    companion object {
        private const val TAG = "QueryCache"
        private const val CACHE_TTL_MS = 5 * 60 * 1000L // 5 minutes
        private const val MAX_CACHE_ENTRIES = 50 // Prevent memory leaks
    }

    private val cache = mutableMapOf<String, CacheEntry>()
    private val mutex = Mutex()

    data class CacheEntry(val result: PaginatedResult<PromoCode>, val timestamp: Long)

    /**
     * Generate cache key for a query
     */
    private fun generateCacheKey(
        query: String?,
        sortBy: String,
        filterByService: String?,
        filterByCategory: String?,
        isFirstPage: Boolean
    ): String {
        // Only cache first page queries (most common)
        if (!isFirstPage) return ""

        return listOf(
            "q=${query ?: ""}",
            "sort=$sortBy",
            "service=${filterByService ?: ""}",
            "category=${filterByCategory ?: ""}",
        ).joinToString("|")
    }

    /**
     * Get cached result if available and not expired
     */
    suspend fun get(
        query: String?,
        sortBy: String,
        filterByService: String?,
        filterByCategory: String?,
        isFirstPage: Boolean
    ): PaginatedResult<PromoCode>? {
        val key = generateCacheKey(query, sortBy, filterByService, filterByCategory, isFirstPage)
        if (key.isEmpty()) return null

        return mutex.withLock {
            val entry = cache[key]
            if (entry != null) {
                val age = System.currentTimeMillis() - entry.timestamp
                if (age <= CACHE_TTL_MS) {
                    Timber.tag(TAG).d("Cache HIT for key: $key (age: ${age}ms)")
                    return@withLock entry.result
                } else {
                    // Remove expired entry
                    cache.remove(key)
                    Timber.tag(TAG).d("Cache EXPIRED for key: $key (age: ${age}ms)")
                }
            }
            null
        }
    }

    /**
     * Cache a query result
     */
    suspend fun put(
        query: String?,
        sortBy: String,
        filterByService: String?,
        filterByCategory: String?,
        isFirstPage: Boolean,
        result: PaginatedResult<PromoCode>
    ) {
        val key = generateCacheKey(query, sortBy, filterByService, filterByCategory, isFirstPage)
        if (key.isEmpty()) return

        mutex.withLock {
            // Clean up old entries if cache is getting too large
            if (cache.size >= MAX_CACHE_ENTRIES) {
                val now = System.currentTimeMillis()
                val entriesToRemove = cache.filter { (_, entry) ->
                    now - entry.timestamp > CACHE_TTL_MS
                }.keys

                entriesToRemove.forEach { cache.remove(it) }
                Timber.tag(TAG).d("Cleaned up ${entriesToRemove.size} expired cache entries")

                // If still too many entries, remove oldest ones
                if (cache.size >= MAX_CACHE_ENTRIES) {
                    val oldestEntries = cache.entries
                        .sortedBy { it.value.timestamp }
                        .take(cache.size - MAX_CACHE_ENTRIES + 10)

                    oldestEntries.forEach { cache.remove(it.key) }
                    Timber.tag(TAG).d("Removed ${oldestEntries.size} oldest cache entries")
                }
            }

            cache[key] = CacheEntry(result, System.currentTimeMillis())
            Timber.tag(TAG).d("Cache PUT for key: $key (${result.data.size} items)")
        }
    }

    /**
     * Clear all cached entries
     */
    suspend fun clear() {
        mutex.withLock {
            val size = cache.size
            cache.clear()
            Timber.tag(TAG).d("Cache cleared ($size entries)")
        }
    }

    /**
     * Get cache statistics for debugging
     */
    suspend fun getStats(): CacheStats =
        mutex.withLock {
            val now = System.currentTimeMillis()
            val validEntries = cache.count { (_, entry) ->
                now - entry.timestamp <= CACHE_TTL_MS
            }
            CacheStats(
                totalEntries = cache.size,
                validEntries = validEntries,
                expiredEntries = cache.size - validEntries,
            )
        }

    data class CacheStats(val totalEntries: Int, val validEntries: Int, val expiredEntries: Int)
}
