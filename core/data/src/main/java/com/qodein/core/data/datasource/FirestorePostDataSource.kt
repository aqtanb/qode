package com.qodein.core.data.datasource

import co.touchlab.kermit.Logger
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.mapper.PostMapper
import com.qodein.core.data.model.PostDto
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestorePostDataSource @Inject constructor(
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

            Logger.i(TAG) { "Successfully created post document: ${dto.id}" }
            Result.Success(post)
        } catch (e: SecurityException) {
            Logger.e(TAG, e) { "Unauthorized to create post: ${post.title}" }
            Result.Error(PostError.SubmissionFailure.NotAuthorized)
        } catch (e: IllegalArgumentException) {
            Logger.e(TAG, e) { "Invalid post data: ${post.title}" }
            Result.Error(PostError.SubmissionFailure.InvalidData)
        } catch (e: IOException) {
            Logger.e(TAG, e) { "Network error creating post: ${post.title}" }
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Failed to create post: ${post.title}" }
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
                    Logger.e(TAG, e) { "Failed to parse document ${document.id}" }
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

            Logger.d(TAG) { "Fetched ${results.size} posts, hasMore: ${paginatedResult.hasMore}" }
            Result.Success(paginatedResult)
        } catch (e: IOException) {
            Logger.e(TAG, e) { "Network error fetching posts" }
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Failed to fetch posts" }
            Result.Error(SystemError.Unknown)
        }

    suspend fun getPostById(id: PostId): Result<Post?, OperationError> {
        Logger.d(TAG) { "Fetching post by ID: ${id.value}" }

        return try {
            val docSnapshot = firestore.collection(POSTS_COLLECTION)
                .document(id.value)
                .get()
                .await()

            if (!docSnapshot.exists()) {
                Logger.w(TAG) { "Post not found: ${id.value}" }
                return Result.Success(null)
            }

            val dto = docSnapshot.toObject<PostDto>()
            if (dto == null) {
                Logger.w(TAG) { "Failed to convert post document to DTO: ${id.value}" }
                return Result.Error(PostError.RetrievalFailure.NotFound)
            }

            Result.Success(PostMapper.toDomain(dto))
        } catch (e: IOException) {
            Logger.e(TAG, e) { "Network error fetching post: ${id.value}" }
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Failed to fetch post: ${id.value}" }
            Result.Error(SystemError.Unknown)
        }
    }
}
