package com.qodein.core.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.mapper.BannerMapper
import com.qodein.core.data.model.BannerDto
import com.qodein.shared.model.Banner
import com.qodein.shared.model.BannerId
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.IOException

/**
 * Firestore data source for banner operations.
 * Provides real-time data updates and country-based filtering.
 */

class FirestoreBannerDataSource constructor(private val firestore: FirebaseFirestore) {
    companion object {
        private const val TAG = "FirestoreBannerDS"
        private const val BANNERS_COLLECTION = "banners"
    }

    /**
     * Gets active banners for a specific country with real-time updates.
     * Uses Firestore array-contains query for country targeting.
     *
     * @param countryCode ISO 3166-1 alpha-2 country code
     * @param limit Maximum number of banners to return
     * @return Flow of banners that updates in real-time
     */
    fun getBannersForCountry(
        countryCode: String,
        limit: Int = 10
    ): Flow<List<Banner>> =
        callbackFlow {
            try {
                // Use simpler single query for country-specific banners to avoid timing issues
                val query = firestore.collection(BANNERS_COLLECTION)
                    .whereEqualTo("isActive", true)
                    .whereArrayContains("targetCountries", countryCode.uppercase())
                    .orderBy("priority", Query.Direction.DESCENDING)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(limit.toLong())

                val listener = query.addSnapshotListener { snapshot, error ->
                    when {
                        error != null -> {
                            Timber.tag(TAG).e(error, "getBannersForCountry: Query failed")
                            val wrappedException = when {
                                error.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                                    SecurityException("permission denied: cannot access banners", error)
                                error.message?.contains("network", ignoreCase = true) == true ->
                                    IOException("connection error while fetching banners for country $countryCode", error)
                                else -> IllegalStateException("service unavailable: failed to fetch banners", error)
                            }
                            close(wrappedException)
                        }
                        snapshot != null -> {
                            try {
                                val banners = snapshot.documents.mapNotNull { document ->
                                    try {
                                        document.toObject<BannerDto>()?.let { dto ->
                                            BannerMapper.toDomain(dto)
                                        }
                                    } catch (e: Exception) {
                                        Timber.tag(TAG).e(e, "Failed to parse banner document ${document.id}")
                                        null
                                    }
                                }

                                trySend(banners)
                            } catch (e: Exception) {
                                Timber.tag(TAG).e(e, "getBannersForCountry: Failed to process snapshot")
                                close(IllegalStateException("service unavailable: failed to process banner data", e))
                            }
                        }
                    }
                }

                awaitClose {
                    listener.remove()
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "getBannersForCountry: Failed to set up query")
                close(IllegalStateException("service unavailable: failed to initialize banner query", e))
            }
        }

    /**
     * Gets all active banners regardless of country targeting.
     * Used as fallback when country-specific queries fail.
     *
     * @param limit Maximum number of banners to return
     * @return Flow of banners that updates in real-time
     */
    fun getAllActiveBanners(limit: Int = 10): Flow<List<Banner>> =
        callbackFlow {
            try {
                val query = firestore.collection(BANNERS_COLLECTION)
                    .whereEqualTo("isActive", true)
                    .orderBy("priority", Query.Direction.DESCENDING)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(limit.toLong())

                val listener = query.addSnapshotListener { snapshot, error ->
                    when {
                        error != null -> {
                            Timber.tag(TAG).e(error, "getAllActiveBanners: Query failed")
                            val wrappedException = when {
                                error.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                                    SecurityException("permission denied: cannot access banners", error)
                                error.message?.contains("network", ignoreCase = true) == true ->
                                    IOException("connection error while fetching banners", error)
                                else -> IllegalStateException("service unavailable: failed to fetch banners", error)
                            }
                            close(wrappedException)
                        }
                        snapshot != null -> {
                            try {
                                val banners = snapshot.documents.mapNotNull { document ->
                                    try {
                                        document.toObject<BannerDto>()?.let { dto ->
                                            BannerMapper.toDomain(dto)
                                        }
                                    } catch (e: Exception) {
                                        Timber.tag(TAG).e(e, "Failed to parse banner document ${document.id}")
                                        null
                                    }
                                }

                                trySend(banners)
                            } catch (e: Exception) {
                                Timber.tag(TAG).e(e, "getAllActiveBanners: Failed to process snapshot")
                                close(IllegalStateException("Failed to process banner data", e))
                            }
                        }
                    }
                }

                awaitClose {
                    listener.remove()
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "getAllActiveBanners: Failed to set up query")
                close(IOException("Failed to set up banner query", e))
            }
        }

    /**
     * Gets a specific banner by ID.
     *
     * @param bannerId The unique identifier of the banner
     * @return The banner if found, null otherwise
     */
    suspend fun getBannerById(bannerId: BannerId): Banner? =
        try {
            val document = firestore.collection(BANNERS_COLLECTION)
                .document(bannerId.value)
                .get()
                .await()

            if (document.exists()) {
                document.toObject<BannerDto>()?.let { dto ->
                    BannerMapper.toDomain(dto)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "getBannerById: Failed to fetch banner ${bannerId.value}")
            when {
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                    throw SecurityException("permission denied: cannot access banner ${bannerId.value}", e)
                e.message?.contains("network", ignoreCase = true) == true ->
                    throw IOException("connection error while fetching banner ${bannerId.value}", e)
                else -> throw IllegalStateException("service unavailable: failed to fetch banner", e)
            }
        }

    /**
     * Observes real-time changes to a specific set of banners.
     *
     * @param bannerIds List of banner IDs to observe
     * @return Flow of banners that updates when any banner changes
     */
    fun observeBanners(bannerIds: List<BannerId>): Flow<List<Banner>> =
        callbackFlow {
            if (bannerIds.isEmpty()) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }

            try {
                val listener = firestore.collection(BANNERS_COLLECTION)
                    .whereIn("__name__", bannerIds.map { it.value })
                    .addSnapshotListener { snapshot, error ->
                        when {
                            error != null -> {
                                Timber.tag(TAG).e(error, "observeBanners: Query failed")
                                close(IOException("Failed to observe banners", error))
                            }
                            snapshot != null -> {
                                try {
                                    val banners = snapshot.documents.mapNotNull { document ->
                                        try {
                                            document.toObject<BannerDto>()?.let { dto ->
                                                BannerMapper.toDomain(dto)
                                            }
                                        } catch (e: Exception) {
                                            Timber.tag(TAG).e(e, "Failed to parse banner document ${document.id}")
                                            null
                                        }
                                    }

                                    trySend(banners)
                                } catch (e: Exception) {
                                    Timber.tag(TAG).e(e, "observeBanners: Failed to process snapshot")
                                    close(IllegalStateException("Failed to process banner data", e))
                                }
                            }
                        }
                    }

                awaitClose {
                    listener.remove()
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "observeBanners: Failed to set up observation")
                close(IOException("Failed to set up banner observation", e))
            }
        }
}
