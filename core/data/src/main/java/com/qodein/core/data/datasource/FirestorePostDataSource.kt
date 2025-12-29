package com.qodein.core.data.datasource

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.dto.PostDto
import com.qodein.core.data.mapper.PostMapper
import com.qodein.core.data.util.ErrorMapper.mapFirestoreException
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.PostError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationCursor
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.PostSortBy
import com.qodein.shared.model.Tag
import kotlinx.coroutines.tasks.await
import java.io.IOException

class FirestorePostDataSource constructor(
    private val firestore: FirebaseFirestore
    // TODO: add caching
) {
    companion object {
        private const val TAG = "FirestorePostDS"
        private const val POSTS_COLLECTION = "posts"
    }

    suspend fun createPost(post: Post): Result<Post, OperationError> =
        try {
            val dto = PostMapper.toDto(post)

            firestore.collection(POSTS_COLLECTION)
                .document(dto.id)
                .set(dto)
                .await()

            Result.Success(post)
        } catch (e: SecurityException) {
            Result.Error(PostError.SubmissionFailure.NotAuthorized)
        } catch (e: IllegalArgumentException) {
            Result.Error(PostError.SubmissionFailure.InvalidData)
        } catch (e: IOException) {
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Result.Error(SystemError.Unknown)
        }

    suspend fun getPosts(
        sortBy: PostSortBy,
        filterByTags: List<Tag>?,
        paginationRequest: PaginationRequest<PostSortBy>
    ): Result<PaginatedResult<Post, PostSortBy>, OperationError> =
        try {
            var firestoreQuery: Query = firestore.collection(POSTS_COLLECTION)

            // Apply sorting
            firestoreQuery = when (sortBy) {
                PostSortBy.POPULARITY -> {
                    firestoreQuery.orderBy("upvotes", Query.Direction.DESCENDING)
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                }
                PostSortBy.NEWEST -> {
                    firestoreQuery.orderBy("createdAt", Query.Direction.DESCENDING)
                }
            }

            // Apply cursor-based pagination
            paginationRequest.cursor?.let { cursor ->
                cursor.documentSnapshot.let { docSnapshot ->
                    firestoreQuery = firestoreQuery.startAfter(docSnapshot as DocumentSnapshot)
                }
            }

            firestoreQuery = firestoreQuery.limit(paginationRequest.limit.toLong())

            val querySnapshot = firestoreQuery.get().await()
            val documents = querySnapshot.documents

            val results = documents.mapNotNull { document ->
                try {
                    document.toObject<PostDto>()?.let { PostMapper.toDomain(it) }
                } catch (e: Exception) {
                    Log.e("FirestorePostDataSource", "Error mapping post ${document.id}", e)
                    null
                }
            }

            val nextCursor = if (documents.isNotEmpty() && documents.size == paginationRequest.limit) {
                val lastDoc = documents.last()
                PaginationCursor(
                    documentSnapshot = lastDoc,
                    sortBy = sortBy,
                )
            } else {
                null
            }

            val paginatedResult = PaginatedResult(
                data = results,
                nextCursor = nextCursor,
                hasMore = nextCursor != null,
            )

            Result.Success(paginatedResult)
        } catch (e: IOException) {
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Result.Error(SystemError.Unknown)
        }

    suspend fun getPostById(id: PostId): Result<Post, OperationError> {
        return try {
            val docSnapshot = firestore.collection(POSTS_COLLECTION)
                .document(id.value)
                .get()
                .await()

            if (!docSnapshot.exists()) {
                return Result.Error(PostError.RetrievalFailure.NotFound)
            }

            val dto = docSnapshot.toObject<PostDto>()
            if (dto == null) {
                return Result.Error(PostError.RetrievalFailure.NotFound)
            }

            Result.Success(PostMapper.toDomain(dto))
        } catch (e: FirebaseFirestoreException) {
            val error = mapFirestoreException(e)
            Result.Error(error)
        } catch (e: IOException) {
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Result.Error(SystemError.Unknown)
        }
    }
}
