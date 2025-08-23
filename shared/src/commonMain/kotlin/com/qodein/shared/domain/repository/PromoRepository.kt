package com.qodein.shared.domain.repository

import com.qodein.shared.model.Promo
import com.qodein.shared.model.PromoId
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Promo operations.
 * Promo represents user-submitted deals without actual promo codes (different from PromoCode).
 *
 * All methods may throw standard exceptions which will be handled by use cases
 * or ViewModels using the .asResult() extension.
 *
 * Follows NIA pattern - uses standard exceptions, no custom exception hierarchies.
 */
interface PromoRepository {

    /**
     * Create a new promo.
     *
     * @param promo The promo to create
     * @return Flow that emits [Promo] on successful creation
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws IllegalArgumentException when validation fails
     */
    fun createPromo(promo: Promo): Flow<Promo>

    /**
     * Get promos with filtering and sorting.
     *
     * @param query Search query text (optional)
     * @param sortBy Sort criteria (popular, newest, expiring, etc.)
     * @param filterByService Filter by service name (optional)
     * @param filterByCategory Filter by category (optional)
     * @param filterByCountry Filter by target country (optional)
     * @param includeExpired Include expired promos (default false)
     * @param limit Maximum number of results
     * @param offset Pagination offset
     * @return Flow that emits List<[Promo]>
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPromos(
        query: String? = null,
        sortBy: PromoSortBy = PromoSortBy.POPULARITY,
        filterByService: String? = null,
        filterByCategory: String? = null,
        filterByCountry: String? = null,
        includeExpired: Boolean = false,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<Promo>>

    /**
     * Get a specific promo by ID.
     *
     * @param id The promo ID
     * @return Flow that emits [Promo] or null if not found
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPromoById(id: PromoId): Flow<Promo?>

    /**
     * Update a promo.
     *
     * @param promo The updated promo
     * @return Flow that emits [Promo] on successful update
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws IllegalArgumentException when validation fails
     * @throws java.lang.SecurityException when user lacks permission to update
     */
    fun updatePromo(promo: Promo): Flow<Promo>

    /**
     * Delete a promo.
     *
     * @param id The promo ID to delete
     * @param createdBy The user requesting deletion (for permission check)
     * @return Flow that emits [Unit] on successful deletion
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws java.lang.SecurityException when user lacks permission to delete
     */
    fun deletePromo(
        id: PromoId,
        createdBy: UserId
    ): Flow<Unit>

    /**
     * Vote on a promo (upvote or downvote).
     *
     * @param promoId The promo ID
     * @param userId The user ID casting the vote
     * @param isUpvote true for upvote, false for downvote
     * @return Flow that emits updated [Promo] with new vote counts
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws IllegalArgumentException when user already voted
     */
    fun voteOnPromo(
        promoId: PromoId,
        userId: UserId,
        isUpvote: Boolean
    ): Flow<Promo>

    /**
     * Remove a user's vote from a promo.
     *
     * @param promoId The promo ID
     * @param userId The user ID removing the vote
     * @return Flow that emits updated [Promo] with updated vote counts
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun removePromoVote(
        promoId: PromoId,
        userId: UserId
    ): Flow<Promo>

    /**
     * Bookmark/unbookmark a promo for a user.
     *
     * @param promoId The promo ID
     * @param userId The user ID
     * @param isBookmarked true to bookmark, false to remove bookmark
     * @return Flow that emits updated [Promo] with bookmark status
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun bookmarkPromo(
        promoId: PromoId,
        userId: UserId,
        isBookmarked: Boolean
    ): Flow<Promo>

    /**
     * Increment view count for a promo.
     *
     * @param id The promo ID
     * @return Flow that emits [Unit] on successful increment
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun incrementViewCount(id: PromoId): Flow<Unit>

    /**
     * Increment share count for a promo.
     *
     * @param id The promo ID
     * @return Flow that emits [Unit] on successful increment
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun incrementShareCount(id: PromoId): Flow<Unit>

    /**
     * Get promos created by a specific user.
     *
     * @param userId The user ID
     * @param limit Maximum number of results
     * @param offset Pagination offset
     * @return Flow that emits List<[Promo]>
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPromosByUser(
        userId: UserId,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<Promo>>

    /**
     * Get promos for a specific service.
     *
     * @param serviceName The service name
     * @param limit Maximum number of results
     * @param offset Pagination offset
     * @return Flow that emits List<[Promo]>
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPromosByService(
        serviceName: String,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<Promo>>

    /**
     * Get promos by category.
     *
     * @param category The category name
     * @param limit Maximum number of results
     * @param offset Pagination offset
     * @return Flow that emits List<[Promo]> in the specified category
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPromosByCategory(
        category: String,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<Promo>>

    /**
     * Get promos targeting a specific country.
     *
     * @param countryCode ISO country code (e.g., "KZ", "US")
     * @param limit Maximum number of results
     * @param offset Pagination offset
     * @return Flow that emits List<[Promo]> targeting the country (or global promos)
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPromosByCountry(
        countryCode: String,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<Promo>>

    /**
     * Get bookmarked promos for a user.
     *
     * @param userId The user ID
     * @param limit Maximum number of results
     * @param offset Pagination offset
     * @return Flow that emits List<[Promo]> bookmarked by the user
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getBookmarkedPromos(
        userId: UserId,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<Promo>>

    /**
     * Get expiring promos (ending within specified days).
     *
     * @param daysAhead Number of days to look ahead for expiring promos
     * @param limit Maximum number of results
     * @return Flow that emits List<[Promo]> expiring within the time frame
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getExpiringPromos(
        daysAhead: Int = 7,
        limit: Int = 20
    ): Flow<List<Promo>>

    /**
     * Get real-time updates for specific promos.
     * Useful for live promo feeds.
     *
     * @param ids List of promo IDs to observe
     * @return Flow that emits List<[Promo]> on any changes
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun observePromos(ids: List<PromoId>): Flow<List<Promo>>

    /**
     * Get trending promos based on recent activity.
     *
     * @param timeWindow Hours to look back for trending calculation (default 24h)
     * @param limit Maximum number of results
     * @return Flow that emits List<[Promo]> sorted by trending score
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getTrendingPromos(
        timeWindow: Int = 24,
        limit: Int = 20
    ): Flow<List<Promo>>

    /**
     * Get verified promos (auto-verified at 10+ votes or manually verified).
     *
     * @param limit Maximum number of results
     * @param offset Pagination offset
     * @return Flow that emits List<[Promo]> that are verified
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getVerifiedPromos(
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<Promo>>
}

/**
 * Enum for promo sorting options.
 */
enum class PromoSortBy {
    POPULARITY, // Sort by vote score (upvotes - downvotes)
    NEWEST, // Sort by creation date (newest first)
    OLDEST, // Sort by creation date (oldest first)
    EXPIRING_SOON, // Sort by expiration date (expiring first)
    MOST_VIEWED, // Sort by view count
    MOST_SHARED, // Sort by share count
    TRENDING // Sort by recent activity and engagement
}
