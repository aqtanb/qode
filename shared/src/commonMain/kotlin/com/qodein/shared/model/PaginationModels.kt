package com.qodein.shared.model

/**
 * Represents a pagination cursor for database queries.
 * Type-safe with the sort order that was used.
 * Platform-agnostic: value contains platform-specific cursor (e.g., DocumentSnapshot on Firebase).
 *
 * @property value Platform-specific cursor (e.g., Firestore DocumentSnapshot)
 * @property sortBy The sort order that was used for this query
 */
data class PaginationCursor<out S : SortBy>(val value: Any?, val sortBy: S)

/**
 * Represents a pagination request with cursor-based parameters.
 */
data class PaginationRequest<out S : SortBy>(val limit: Int, val cursor: PaginationCursor<S>? = null) {
    init {
        require(limit > 0) { "Limit must be positive, got: $limit" }
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 3

        /**
         * Create a request for the first page.
         */
        fun <S : SortBy> firstPage(limit: Int = DEFAULT_PAGE_SIZE): PaginationRequest<S> = PaginationRequest(limit = limit, cursor = null)

        /**
         * Create a request for the next page using a cursor.
         */
        fun <S : SortBy> nextPage(
            cursor: PaginationCursor<S>,
            limit: Int = DEFAULT_PAGE_SIZE
        ): PaginationRequest<S> = PaginationRequest(limit = limit, cursor = cursor)
    }
}

/**
 * Represents a paginated result containing data and pagination information.
 * Type-safe with the sort order that was used.
 *
 * @property data The items for the current page
 * @property nextCursor Cursor for fetching the next page, null if no more pages
 */
data class PaginatedResult<T, out S : SortBy>(val data: List<T>, val nextCursor: PaginationCursor<S>?) {
    val hasMore: Boolean get() = nextCursor != null
}
