package com.qodein.core.data.datasource

import android.util.Log
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
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore data source for banner operations.
 * Provides real-time data updates and country-based filtering.
 */
@Singleton
class FirestoreBannerDataSource @Inject constructor(private val firestore: FirebaseFirestore) {
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
            Log.d(TAG, "getBannersForCountry: Starting query for country=$countryCode, limit=$limit")

            try {
                // Note: Firestore doesn't support compound queries with array-contains and timestamp comparison
                // We'll need to handle expiration filtering client-side or use composite queries
                val query = firestore.collection(BANNERS_COLLECTION)
                    .whereEqualTo("isActive", true)
                    .whereArrayContains("targetCountries", countryCode.uppercase())
                    .orderBy("priority", Query.Direction.DESCENDING)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(limit.toLong())

                val listener = query.addSnapshotListener { snapshot, error ->
                    when {
                        error != null -> {
                            Log.e(TAG, "getBannersForCountry: Query failed", error)
                            close(IOException("Failed to fetch banners for country $countryCode", error))
                        }
                        snapshot != null -> {
                            try {
                                Log.d(TAG, "getBannersForCountry: Received ${snapshot.documents.size} documents")

                                val banners = snapshot.documents.mapNotNull { document ->
                                    try {
                                        Log.d(TAG, "Processing banner document ${document.id}")
                                        document.toObject<BannerDto>()?.let { dto ->
                                            BannerMapper.toDomain(dto)
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Failed to parse banner document ${document.id}", e)
                                        null
                                    }
                                }

                                Log.d(TAG, "getBannersForCountry: Successfully parsed ${banners.size} banners")
                                trySend(banners)
                            } catch (e: Exception) {
                                Log.e(TAG, "getBannersForCountry: Failed to process snapshot", e)
                                close(IllegalStateException("Failed to process banner data", e))
                            }
                        }
                    }
                }

                awaitClose {
                    Log.d(TAG, "getBannersForCountry: Removing listener")
                    listener.remove()
                }
            } catch (e: Exception) {
                Log.e(TAG, "getBannersForCountry: Failed to set up query", e)
                close(IOException("Failed to set up banner query", e))
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
            Log.d(TAG, "getAllActiveBanners: Starting query for limit=$limit")

            try {
                val query = firestore.collection(BANNERS_COLLECTION)
                    .whereEqualTo("isActive", true)
                    .orderBy("priority", Query.Direction.DESCENDING)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(limit.toLong())

                val listener = query.addSnapshotListener { snapshot, error ->
                    when {
                        error != null -> {
                            Log.e(TAG, "getAllActiveBanners: Query failed", error)
                            close(IOException("Failed to fetch active banners", error))
                        }
                        snapshot != null -> {
                            try {
                                Log.d(TAG, "getAllActiveBanners: Received ${snapshot.documents.size} documents")

                                val banners = snapshot.documents.mapNotNull { document ->
                                    try {
                                        document.toObject<BannerDto>()?.let { dto ->
                                            BannerMapper.toDomain(dto)
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Failed to parse banner document ${document.id}", e)
                                        null
                                    }
                                }

                                Log.d(TAG, "getAllActiveBanners: Successfully parsed ${banners.size} banners")
                                trySend(banners)
                            } catch (e: Exception) {
                                Log.e(TAG, "getAllActiveBanners: Failed to process snapshot", e)
                                close(IllegalStateException("Failed to process banner data", e))
                            }
                        }
                    }
                }

                awaitClose {
                    Log.d(TAG, "getAllActiveBanners: Removing listener")
                    listener.remove()
                }
            } catch (e: Exception) {
                Log.e(TAG, "getAllActiveBanners: Failed to set up query", e)
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
            Log.d(TAG, "getBannerById: Fetching banner ${bannerId.value}")

            val document = firestore.collection(BANNERS_COLLECTION)
                .document(bannerId.value)
                .get()
                .await()

            if (document.exists()) {
                document.toObject<BannerDto>()?.let { dto ->
                    Log.d(TAG, "getBannerById: Successfully found banner ${bannerId.value}")
                    BannerMapper.toDomain(dto)
                }
            } else {
                Log.d(TAG, "getBannerById: Banner ${bannerId.value} not found")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "getBannerById: Failed to fetch banner ${bannerId.value}", e)
            throw IOException("Failed to fetch banner ${bannerId.value}", e)
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
                Log.d(TAG, "observeBanners: Empty banner IDs list, emitting empty list")
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }

            Log.d(TAG, "observeBanners: Starting observation for ${bannerIds.size} banners")

            try {
                val listener = firestore.collection(BANNERS_COLLECTION)
                    .whereIn("__name__", bannerIds.map { it.value })
                    .addSnapshotListener { snapshot, error ->
                        when {
                            error != null -> {
                                Log.e(TAG, "observeBanners: Query failed", error)
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
                                            Log.e(TAG, "Failed to parse banner document ${document.id}", e)
                                            null
                                        }
                                    }

                                    Log.d(TAG, "observeBanners: Successfully parsed ${banners.size} banners")
                                    trySend(banners)
                                } catch (e: Exception) {
                                    Log.e(TAG, "observeBanners: Failed to process snapshot", e)
                                    close(IllegalStateException("Failed to process banner data", e))
                                }
                            }
                        }
                    }

                awaitClose {
                    Log.d(TAG, "observeBanners: Removing listener")
                    listener.remove()
                }
            } catch (e: Exception) {
                Log.e(TAG, "observeBanners: Failed to set up observation", e)
                close(IOException("Failed to set up banner observation", e))
            }
        }
}
