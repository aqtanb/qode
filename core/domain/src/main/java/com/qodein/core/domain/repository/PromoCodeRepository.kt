package com.qodein.core.domain.repository

import com.qodein.core.model.PromoCode
import com.qodein.core.model.PromoCodeId
import com.qodein.core.model.PromoCodeUsage
import com.qodein.core.model.PromoCodeVote
import com.qodein.core.model.UserId
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for PromoCode operations.
 *
 * All methods may throw standard exceptions which will be handled by use cases
 * or ViewModels using the .asResult() extension.
 *
 * Follows NIA pattern - uses standard exceptions, no custom exception hierarchies.
 */
interface PromoCodeRepository {

    /**
     * Create a new promo code.
     *
     * @param promoCode The promo code to create
     * @return Flow that emits [PromoCode] on successful creation
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws IllegalArgumentException when validation fails
     */
    fun createPromoCode(promoCode: PromoCode): Flow<PromoCode>

    /**
     * Get promo codes with filtering and sorting.
     *
     * @param query Search query text (optional)
     * @param sortBy Sort criteria (popularity, newest, expiring, etc.)
     * @param filterByType Filter by promo code type (optional)
     * @param filterByService Filter by service name (optional)
     * @param filterByCategory Filter by category (optional)
     * @param isFirstUserOnly Filter first-user-only codes
     * @param limit Maximum number of results
     * @param offset Pagination offset
     * @return Flow that emits List<[PromoCode]>
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPromoCodes(
        query: String? = null,
        sortBy: PromoCodeSortBy = PromoCodeSortBy.POPULARITY,
        filterByType: String? = null,
        filterByService: String? = null,
        filterByCategory: String? = null,
        isFirstUserOnly: Boolean? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<PromoCode>>

    /**
     * Get a specific promo code by ID.
     *
     * @param id The promo code ID
     * @return Flow that emits [PromoCode] or null if not found
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPromoCodeById(id: PromoCodeId): Flow<PromoCode?>

    /**
     * Get promo code by code string.
     *
     * @param code The promo code string
     * @return Flow that emits [PromoCode] or null if not found
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPromoCodeByCode(code: String): Flow<PromoCode?>

    /**
     * Update a promo code.
     *
     * @param promoCode The updated promo code
     * @return Flow that emits [PromoCode] on successful update
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws IllegalArgumentException when validation fails
     * @throws SecurityException when user lacks permission to update
     */
    fun updatePromoCode(promoCode: PromoCode): Flow<PromoCode>

    /**
     * Delete a promo code.
     *
     * @param id The promo code ID to delete
     * @return Flow that emits [Unit] on successful deletion
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws SecurityException when user lacks permission to delete
     */
    fun deletePromoCode(id: PromoCodeId): Flow<Unit>

    /**
     * Vote on a promo code (upvote or downvote).
     *
     * @param promoCodeId The promo code ID
     * @param userId The user ID casting the vote
     * @param isUpvote true for upvote, false for downvote
     * @return Flow that emits [PromoCodeVote] on successful vote
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws IllegalArgumentException when user already voted
     */
    fun voteOnPromoCode(
        promoCodeId: PromoCodeId,
        userId: UserId,
        isUpvote: Boolean
    ): Flow<PromoCodeVote>

    /**
     * Remove a user's vote from a promo code.
     *
     * @param promoCodeId The promo code ID
     * @param userId The user ID removing the vote
     * @return Flow that emits [Unit] on successful removal
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun removeVote(
        promoCodeId: PromoCodeId,
        userId: UserId
    ): Flow<Unit>

    /**
     * Get user's vote on a specific promo code.
     *
     * @param promoCodeId The promo code ID
     * @param userId The user ID
     * @return Flow that emits [PromoCodeVote] or null if no vote
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getUserVote(
        promoCodeId: PromoCodeId,
        userId: UserId
    ): Flow<PromoCodeVote?>

    /**
     * Increment view count for a promo code.
     *
     * @param id The promo code ID
     * @return Flow that emits [Unit] on successful increment
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun incrementViewCount(id: PromoCodeId): Flow<Unit>

    /**
     * Record promo code usage.
     *
     * @param usage The usage record
     * @return Flow that emits [PromoCodeUsage] on successful record
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws IllegalArgumentException when validation fails
     */
    fun recordUsage(usage: PromoCodeUsage): Flow<PromoCodeUsage>

    /**
     * Get usage statistics for a promo code.
     *
     * @param promoCodeId The promo code ID
     * @return Flow that emits List<[PromoCodeUsage]>
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getUsageStatistics(promoCodeId: PromoCodeId): Flow<List<PromoCodeUsage>>

    /**
     * Add comment to a promo code.
     *
     * @param promoCodeId The promo code ID
     * @param userId The user ID adding the comment
     * @param comment The comment text
     * @return Flow that emits updated [PromoCode] with new comment
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws IllegalArgumentException when comment is invalid
     */
    fun addComment(
        promoCodeId: PromoCodeId,
        userId: UserId,
        comment: String
    ): Flow<PromoCode>

    /**
     * Get promo codes created by a specific user.
     *
     * @param userId The user ID
     * @param limit Maximum number of results
     * @param offset Pagination offset
     * @return Flow that emits List<[PromoCode]>
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPromoCodesByUser(
        userId: UserId,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<PromoCode>>

    /**
     * Get promo codes for a specific service.
     *
     * @param serviceName The service name
     * @return Flow that emits List<[PromoCode]>
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPromoCodesByService(serviceName: String): Flow<List<PromoCode>>

    /**
     * Get promo code by code and service combination.
     *
     * @param code The promo code string
     * @param serviceName The service name
     * @return Flow that emits [PromoCode] or null if not found
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPromoCodeByCodeAndService(
        code: String,
        serviceName: String
    ): Flow<PromoCode?>

    /**
     * Get real-time updates for promo codes.
     * Useful for live community features.
     *
     * @param ids List of promo code IDs to observe
     * @return Flow that emits List<[PromoCode]> on any changes
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun observePromoCodes(ids: List<PromoCodeId>): Flow<List<PromoCode>>
}

/**
 * Enum for promo code sorting options.
 */
enum class PromoCodeSortBy {
    POPULARITY, // Sort by vote score (upvotes - downvotes)
    NEWEST, // Sort by creation date (newest first)
    OLDEST, // Sort by creation date (oldest first)
    EXPIRING_SOON, // Sort by end date (expiring first)
    MOST_VIEWED, // Sort by view count
    MOST_USED, // Sort by usage count
    ALPHABETICAL // Sort by code alphabetically
}
