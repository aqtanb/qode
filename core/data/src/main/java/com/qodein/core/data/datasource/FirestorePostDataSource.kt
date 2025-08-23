package com.qodein.core.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.mapper.PostMapper
import com.qodein.core.data.model.PostDto
import com.qodein.shared.domain.repository.PostSortBy
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.UserId
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestorePostDataSource @Inject constructor(private val firestore: FirebaseFirestore) {
    companion object {
        private const val TAG = "FirestorePostDS"
        private const val POSTS_COLLECTION = "posts"
        private const val VOTES_SUBCOLLECTION = "votes"
        private const val BOOKMARKS_SUBCOLLECTION = "bookmarks"
    }

    suspend fun createPost(post: Post): Post {
        val dto = PostMapper.toDto(post)

        try {
            firestore.collection(POSTS_COLLECTION)
                .document(dto.id)
                .set(dto)
                .await()
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error in createPost")

            when {
                e.message?.contains("ALREADY_EXISTS", ignoreCase = true) == true ||
                    e.message?.contains("already exists", ignoreCase = true) == true -> {
                    throw IllegalArgumentException("post already exists with ID: ${post.id.value}", e)
                }
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true -> {
                    throw SecurityException("permission denied: cannot create post", e)
                }
                e.message?.contains("network", ignoreCase = true) == true ||
                    e.message?.contains("timeout", ignoreCase = true) == true -> {
                    throw IOException("connection error while creating post", e)
                }
                else -> {
                    throw IllegalStateException("service unavailable: failed to create post", e)
                }
            }
        }

        return post
    }

    suspend fun getPosts(
        query: String?,
        sortBy: PostSortBy,
        filterByTag: String?,
        filterByAuthor: String?,
        limit: Int,
        offset: Int
    ): List<Post> {
        Timber.tag(TAG).d("getPosts: Starting query with sortBy=$sortBy, limit=$limit")
        var firestoreQuery: Query = firestore.collection(POSTS_COLLECTION)

        // Apply filters
        query?.let { searchQuery ->
            if (searchQuery.isNotBlank()) {
                // Simple text search - for better search, consider using Algolia
                firestoreQuery = firestoreQuery
                    .whereGreaterThanOrEqualTo("title", searchQuery)
                    .whereLessThanOrEqualTo("title", searchQuery + "\uf8ff")
            }
        }

        filterByAuthor?.let { author ->
            firestoreQuery = firestoreQuery.whereEqualTo("authorUsername", author)
        }

        // Tag filtering requires array-contains query
        filterByTag?.let { tag ->
            firestoreQuery = firestoreQuery.whereArrayContains("tags", mapOf("name" to tag))
        }

        // Apply sorting
        firestoreQuery = when (sortBy) {
            PostSortBy.NEWEST -> {
                firestoreQuery.orderBy("createdAt", Query.Direction.DESCENDING)
            }
            PostSortBy.OLDEST -> {
                firestoreQuery.orderBy("createdAt", Query.Direction.ASCENDING)
            }
            PostSortBy.POPULAR -> {
                // Sort by vote score - may need composite index
                firestoreQuery.orderBy("upvotes", Query.Direction.DESCENDING)
            }
            PostSortBy.MOST_SHARED -> {
                firestoreQuery.orderBy("shares", Query.Direction.DESCENDING)
            }
            PostSortBy.TRENDING -> {
                // For trending, use a combination of recent votes and creation time
                // For now, fallback to newest
                firestoreQuery.orderBy("createdAt", Query.Direction.DESCENDING)
            }
        }

        // Apply pagination
        firestoreQuery = firestoreQuery.limit(limit.toLong())

        val querySnapshot = firestoreQuery.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<PostDto>()?.let { dto ->
                    PostMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to parse post ${document.id}")
                null
            }
        }
    }

    suspend fun getPostById(id: PostId): Post? {
        val document = firestore.collection(POSTS_COLLECTION)
            .document(id.value)
            .get()
            .await()

        return document.toObject<PostDto>()?.let { dto ->
            PostMapper.toDomain(dto)
        }
    }

    suspend fun updatePost(post: Post): Post {
        val dto = PostMapper.toDto(post)

        firestore.collection(POSTS_COLLECTION)
            .document(dto.id)
            .set(dto)
            .await()

        return post
    }

    suspend fun deletePost(
        id: PostId,
        authorId: UserId
    ) {
        val postRef = firestore.collection(POSTS_COLLECTION).document(id.value)

        // Check if user owns the post
        val postDoc = postRef.get().await()
        val postDto = postDoc.toObject<PostDto>()
            ?: throw IllegalArgumentException("post not found: ${id.value}")

        if (postDto.authorId != authorId.value) {
            throw SecurityException("permission denied: cannot delete post owned by another user")
        }

        postRef.delete().await()
    }

    suspend fun voteOnPost(
        postId: PostId,
        userId: UserId,
        isUpvote: Boolean
    ): Post {
        val postRef = firestore.collection(POSTS_COLLECTION).document(postId.value)
        val voteId = "${userId.value}_${postId.value}"
        val voteRef = postRef.collection(VOTES_SUBCOLLECTION).document(voteId)

        // Use batch write to ensure atomicity
        val batch = firestore.batch()

        // Add/update vote
        val voteData = mapOf(
            "userId" to userId.value,
            "postId" to postId.value,
            "isUpvote" to isUpvote,
            "createdAt" to FieldValue.serverTimestamp(),
        )
        batch.set(voteRef, voteData)

        // Update vote counts on post
        if (isUpvote) {
            batch.update(postRef, "upvotes", FieldValue.increment(1))
        } else {
            batch.update(postRef, "downvotes", FieldValue.increment(1))
        }

        batch.commit().await()

        // Return updated post
        val updatedDoc = postRef.get().await()
        val updatedDto = updatedDoc.toObject<PostDto>()
            ?: throw IllegalStateException("service unavailable: failed to retrieve updated post")

        return PostMapper.toDomain(updatedDto)
    }

    suspend fun removePostVote(
        postId: PostId,
        userId: UserId
    ): Post {
        val postRef = firestore.collection(POSTS_COLLECTION).document(postId.value)
        val voteId = "${userId.value}_${postId.value}"
        val voteRef = postRef.collection(VOTES_SUBCOLLECTION).document(voteId)

        // Get existing vote to know which counter to decrement
        val existingVoteSnapshot = voteRef.get().await()
        val existingVote = existingVoteSnapshot.data

        if (existingVote != null) {
            val isUpvote = existingVote["isUpvote"] as Boolean
            val batch = firestore.batch()

            // Remove vote
            batch.delete(voteRef)

            // Update vote counts on post
            if (isUpvote) {
                batch.update(postRef, "upvotes", FieldValue.increment(-1))
            } else {
                batch.update(postRef, "downvotes", FieldValue.increment(-1))
            }

            batch.commit().await()
        }

        // Return updated post
        val updatedDoc = postRef.get().await()
        val updatedDto = updatedDoc.toObject<PostDto>()
            ?: throw IllegalStateException("service unavailable: failed to retrieve updated post")

        return PostMapper.toDomain(updatedDto)
    }

    suspend fun bookmarkPost(
        postId: PostId,
        userId: UserId,
        isBookmarked: Boolean
    ): Post {
        val postRef = firestore.collection(POSTS_COLLECTION).document(postId.value)
        val bookmarkId = "${userId.value}_${postId.value}"
        val bookmarkRef = postRef.collection(BOOKMARKS_SUBCOLLECTION).document(bookmarkId)

        if (isBookmarked) {
            // Add bookmark
            val bookmarkData = mapOf(
                "userId" to userId.value,
                "postId" to postId.value,
                "createdAt" to FieldValue.serverTimestamp(),
            )
            bookmarkRef.set(bookmarkData).await()
        } else {
            // Remove bookmark
            bookmarkRef.delete().await()
        }

        // Return updated post (bookmark status would be computed at query time)
        val updatedDoc = postRef.get().await()
        val updatedDto = updatedDoc.toObject<PostDto>()
            ?: throw IllegalStateException("service unavailable: failed to retrieve updated post")

        return PostMapper.toDomain(updatedDto)
    }

    suspend fun incrementShareCount(id: PostId) {
        firestore.collection(POSTS_COLLECTION)
            .document(id.value)
            .update("shares", FieldValue.increment(1))
            .await()
    }

    suspend fun getPostsByUser(
        userId: UserId,
        limit: Int,
        offset: Int
    ): List<Post> {
        val query = firestore.collection(POSTS_COLLECTION)
            .whereEqualTo("authorId", userId.value)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        val querySnapshot = query.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<PostDto>()?.let { dto ->
                    PostMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getPostsByTag(
        tagName: String,
        limit: Int,
        offset: Int
    ): List<Post> {
        val query = firestore.collection(POSTS_COLLECTION)
            .whereArrayContains("tags", mapOf("name" to tagName))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        val querySnapshot = query.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<PostDto>()?.let { dto ->
                    PostMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getBookmarkedPosts(
        userId: UserId,
        limit: Int,
        offset: Int
    ): List<Post> {
        // This requires a more complex query - in production, consider maintaining
        // a user bookmarks collection for better performance

        // For now, return empty list - implement user bookmarks collection separately
        return emptyList()
    }

    suspend fun getTrendingPosts(
        timeWindow: Int,
        limit: Int
    ): List<Post> {
        // Simplified trending: posts created in the last timeWindow hours, sorted by vote score
        val cutoffTime = System.currentTimeMillis() - (timeWindow * 60 * 60 * 1000)

        val query = firestore.collection(POSTS_COLLECTION)
            .whereGreaterThan("createdAt", cutoffTime)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .orderBy("upvotes", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        val querySnapshot = query.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<PostDto>()?.let { dto ->
                    PostMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun observePosts(ids: List<PostId>): Flow<List<Post>> =
        callbackFlow {
            if (ids.isEmpty()) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }

            val listener = firestore.collection(POSTS_COLLECTION)
                .whereIn("__name__", ids.map { it.value })
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        val wrappedException = when {
                            error.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                                SecurityException("permission denied: cannot observe posts", error)
                            error.message?.contains("network", ignoreCase = true) == true ->
                                IOException("connection error while observing posts", error)
                            else -> IllegalStateException("service unavailable: failed to observe posts", error)
                        }
                        close(wrappedException)
                        return@addSnapshotListener
                    }

                    val posts = snapshot?.documents?.mapNotNull { document ->
                        try {
                            document.toObject<PostDto>()?.let { dto ->
                                PostMapper.toDomain(dto)
                            }
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()

                    trySend(posts)
                }

            awaitClose {
                listener.remove()
            }
        }
}
