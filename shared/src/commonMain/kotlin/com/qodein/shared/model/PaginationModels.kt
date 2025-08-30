package com.qodein.shared.model

/**
 * Represents a pagination cursor for Firebase Firestore queries.
 * Contains the serialized document snapshot data needed for cursor-based pagination.
 */
data class PaginationCursor(
    val documentId: String,
    val sortFieldValue: Any? = null,
    val additionalFields: Map<String, Any> = emptyMap(),
    val lastDocumentSnapshot: Any? = null // Store DocumentSnapshot for proper Firestore pagination
) {
    companion object {
        /**
         * Create a cursor from Firebase DocumentSnapshot.
         * This will be implemented in the platform-specific data layer.
         */
        fun fromDocumentSnapshot(
            documentId: String,
            sortFieldValue: Any?,
            additionalFields: Map<String, Any> = emptyMap(),
            lastDocumentSnapshot: Any? = null
        ): PaginationCursor =
            PaginationCursor(
                documentId = documentId,
                sortFieldValue = sortFieldValue,
                additionalFields = additionalFields,
                lastDocumentSnapshot = lastDocumentSnapshot,
            )
    }
}

/**
 * Represents a pagination request with cursor-based parameters.
 */
data class PaginationRequest(val limit: Int = 20, val cursor: PaginationCursor? = null) {
    companion object {
        /**
         * Create a request for the first page.
         */
        fun firstPage(limit: Int = 20): PaginationRequest = PaginationRequest(limit = limit, cursor = null)

        /**
         * Create a request for the next page using a cursor.
         */
        fun nextPage(
            cursor: PaginationCursor,
            limit: Int = 20
        ): PaginationRequest = PaginationRequest(limit = limit, cursor = cursor)
    }
}

/**
 * Represents a paginated result containing data and pagination information.
 */
data class PaginatedResult<T>(val data: List<T>, val nextCursor: PaginationCursor?, val hasMore: Boolean) {
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
        fun <T> empty(): PaginatedResult<T> =
            PaginatedResult(
                data = emptyList(),
                nextCursor = null,
                hasMore = false,
            )

        /**
         * Create a paginated result with data.
         */
        fun <T> of(
            data: List<T>,
            nextCursor: PaginationCursor?,
            hasMore: Boolean
        ): PaginatedResult<T> =
            PaginatedResult(
                data = data,
                nextCursor = nextCursor,
                hasMore = hasMore,
            )
    }
}
