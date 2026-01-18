package com.qodein.core.data.datasource.util

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.dto.PagedFirestoreResult

fun Query.applyPaginationCursor(cursor: DocumentSnapshot?): Query = cursor?.let { startAfter(it) } ?: this

inline fun <reified T> List<DocumentSnapshot>.toPagedResult(limit: Int): PagedFirestoreResult<T> {
    val hasMore = size > limit
    val pagedDocuments = take(limit)
    val items = pagedDocuments.mapNotNull { it.toObject<T>() }
    val nextCursor = if (hasMore) pagedDocuments.lastOrNull() else null

    return PagedFirestoreResult(
        items = items,
        nextCursor = nextCursor,
    )
}
