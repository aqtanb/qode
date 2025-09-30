package com.qodein.shared.model

/**
 * Represents a pagination cursor for Firebase Firestore queries.
 * Type-safe with the sort order that was used.
 *
 * @property documentSnapshot The Firestore DocumentSnapshot used for startAfter() queries (contains all field values)
 * @property sortBy The sort order that was used for this query
 * @property documentId Optional document ID for debugging/display
 */
data class PaginationCursor<out S : SortBy>(val documentSnapshot: FirestoreDocumentSnapshot?, val sortBy: S, val documentId: String? = null)

/**
 * Represents a pagination request with cursor-based parameters.
 */
data class PaginationRequest<out S : SortBy>(val limit: Int = 20, val cursor: PaginationCursor<S>? = null) {
    companion object {
        /**
         * Create a request for the first page.
         */
        fun <S : SortBy> firstPage(limit: Int = 20): PaginationRequest<S> = PaginationRequest(limit = limit, cursor = null)

        /**
         * Create a request for the next page using a cursor.
         */
        fun <S : SortBy> nextPage(
            cursor: PaginationCursor<S>,
            limit: Int = 20
        ): PaginationRequest<S> = PaginationRequest(limit = limit, cursor = cursor)
    }
}

/**
 * Represents a paginated result containing data and pagination information.
 * Type-safe with the sort order that was used.
 */
data class PaginatedResult<T, out S : SortBy>(val data: List<T>, val nextCursor: PaginationCursor<S>?, val hasMore: Boolean) {
    /**
     * Check if this is an empty result.
     */
    val isEmpty: Boolean get() = data.isEmpty()

    /**
     * Get the size of the data.
     */
    val size: Int get() = data.size

    companion object {
        /**
         * Create an empty paginated result.
         */
        fun <T, S : SortBy> empty(): PaginatedResult<T, S> =
            PaginatedResult(
                data = emptyList(),
                nextCursor = null,
                hasMore = false,
            )

        /**
         * Create a paginated result with data.
         */
        fun <T, S : SortBy> of(
            data: List<T>,
            nextCursor: PaginationCursor<S>?,
            hasMore: Boolean
        ): PaginatedResult<T, S> =
            PaginatedResult(
                data = data,
                nextCursor = nextCursor,
                hasMore = hasMore,
            )
    }
}
