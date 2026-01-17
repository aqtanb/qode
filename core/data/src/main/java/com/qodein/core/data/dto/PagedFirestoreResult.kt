package com.qodein.core.data.dto

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Generic paginated result from Firestore queries.
 *
 * @param T The DTO type (e.g., PromocodeDto, PostDto)
 * @property items The fetched items for the current page
 * @property nextCursor Cursor for the next page, null if no more data
 */
data class PagedFirestoreResult<T>(val items: List<T>, val nextCursor: DocumentSnapshot?)
