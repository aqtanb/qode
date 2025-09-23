package com.qodein.shared.domain.repository

import com.qodein.shared.model.ContentSortBy
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.Service
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for PromoCode operations.
 *
 * All methods may throw standard exceptions which will be handled by use cases
 * or ViewModels using the .asResult() extension.
 *
 * Follows NIA pattern - uses standard exceptions, no custom exception hierarchies.
 */
interface PromocodeRepository {

    /**
     * Create a new promo code.
     *
     * @param promoCode The promo code to create
     * @return Flow that emits [com.qodein.shared.model.PromoCode] on successful creation
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws IllegalArgumentException when validation fails
     */
    fun createPromoCode(promoCode: PromoCode): Flow<PromoCode>

    /**
     * Get promo codes with filtering and sorting using cursor-based pagination.
     *
     * Uses Firebase-native cursor pagination for enterprise-grade performance
     * that scales to millions of documents with minimal read costs.
     *
     * @param query Search query text (optional)
     * @param sortBy Sort criteria (popularity, newest, expiring)
     * @param filterByServices Filter by service names (optional) - supports multiple services
     * @param filterByCategories Filter by categories (optional) - supports multiple categories
     * @param paginationRequest Cursor-based pagination parameters
     * @return Flow that emits [com.qodein.shared.model.PaginatedResult] with data and next cursor
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPromoCodes(
        query: String? = null,
        sortBy: ContentSortBy = ContentSortBy.POPULARITY,
        filterByServices: List<String>? = null,
        filterByCategories: List<String>? = null,
        paginationRequest: PaginationRequest = PaginationRequest.firstPage()
    ): Flow<PaginatedResult<PromoCode>>

    /**
     * Get a specific promo code by ID.
     *
     * @param id The promo code ID
     * @return Flow that emits [com.qodein.shared.model.PromoCode] or null if not found
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPromoCodeById(id: PromoCodeId): Flow<PromoCode?>

    /**
     * Get promo code by code string.
     *
     * @param code The promo code string
     * @return Flow that emits [com.qodein.shared.model.PromoCode] or null if not found
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPromoCodeByCode(code: String): Flow<PromoCode?>

    /**
     * Update a promo code.
     *
     * @param promoCode The updated promo code
     * @return Flow that emits [com.qodein.shared.model.PromoCode] on successful update
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws IllegalArgumentException when validation fails
     * @throws java.lang.SecurityException when user lacks permission to update
     */
    fun updatePromoCode(promoCode: PromoCode): Flow<PromoCode>

    /**
     * Delete a promo code.
     *
     * @param id The promo code ID to delete
     * @return Flow that emits [Unit] on successful deletion
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     * @throws java.lang.SecurityException when user lacks permission to delete
     */
    fun deletePromoCode(id: PromoCodeId): Flow<Unit>

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
     * Add comment to a promo code.
     *
     * @param promoCodeId The promo code ID
     * @param userId The user ID adding the comment
     * @param comment The comment text
     * @return Flow that emits updated [com.qodein.shared.model.PromoCode] with new comment
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
     * @return Flow that emits List<[com.qodein.shared.model.PromoCode]>
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
     * @return Flow that emits List<[com.qodein.shared.model.PromoCode]>
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPromoCodesByService(serviceName: String): Flow<List<PromoCode>>

    /**
     * Get promo code by code and service combination.
     *
     * @param code The promo code string
     * @param serviceName The service name
     * @return Flow that emits [com.qodein.shared.model.PromoCode] or null if not found
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
     * @return Flow that emits List<[com.qodein.shared.model.PromoCode]> on any changes
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun observePromoCodes(ids: List<PromoCodeId>): Flow<List<PromoCode>>

    // Service-related methods

    /**
     * Search for services by name or category.
     *
     * @param query Search query text
     * @param limit Maximum number of results
     * @return Flow that emits List<[com.qodein.shared.model.Service]> matching the query
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun searchServices(
        query: String,
        limit: Int = 20
    ): Flow<List<Service>>

    /**
     * Get popular services for quick selection.
     *
     * @param limit Maximum number of results
     * @return Flow that emits List<[com.qodein.shared.model.Service]> marked as popular
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getPopularServices(limit: Int = 10): Flow<List<Service>>

    /**
     * Get services by category.
     *
     * @param category Service category
     * @param limit Maximum number of results
     * @return Flow that emits List<[com.qodein.shared.model.Service]> in the specified category
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Firestore is unavailable
     */
    fun getServicesByCategory(
        category: String,
        limit: Int = 20
    ): Flow<List<Service>>
}
