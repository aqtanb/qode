package com.qodein.core.data.datasource

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.datasource.util.applyPaginationCursor
import com.qodein.core.data.datasource.util.toPagedResult
import com.qodein.core.data.dto.PagedFirestoreResult
import com.qodein.core.data.dto.PostDto
import kotlinx.coroutines.tasks.await

class FirestorePostDataSource(private val firestore: FirebaseFirestore) {
    suspend fun createPost(dto: PostDto) {
        firestore.collection(PostDto.COLLECTION_NAME)
            .document(dto.id)
            .set(dto)
            .await()
    }

    suspend fun getPosts(
        limit: Int,
        blockedUserIds: List<String>,
        startAfter: DocumentSnapshot?
    ): PagedFirestoreResult<PostDto> {
        val fetchLimit = limit + 1
        var query = firestore.collection(PostDto.COLLECTION_NAME)
            .orderBy(PostDto.FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .applyPaginationCursor(startAfter)

        if (blockedUserIds.isNotEmpty()) {
            query = query.whereNotIn(PostDto.FIELD_AUTHOR_ID, blockedUserIds)
        }

        val documents = query
            .limit(fetchLimit.toLong())
            .get()
            .await()
            .documents

        return documents.toPagedResult(limit)
    }

    suspend fun getPostById(id: String): PostDto? =
        firestore.collection(PostDto.COLLECTION_NAME)
            .document(id)
            .get()
            .await()
            .toObject()

    suspend fun getPostsByUser(
        userId: String,
        limit: Int,
        startAfter: DocumentSnapshot?
    ): PagedFirestoreResult<PostDto> {
        val fetchLimit = limit + 1
        val documents = firestore.collection(PostDto.COLLECTION_NAME)
            .whereEqualTo(PostDto.FIELD_AUTHOR_ID, userId)
            .orderBy(PostDto.FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .applyPaginationCursor(startAfter)
            .limit(fetchLimit.toLong())
            .get()
            .await()
            .documents

        return documents.toPagedResult(limit)
    }
}
