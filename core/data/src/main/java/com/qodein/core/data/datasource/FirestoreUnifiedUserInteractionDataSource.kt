package com.qodein.core.data.datasource

import co.touchlab.kermit.Logger
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.model.UserInteractionDto
import com.qodein.shared.model.UserInteraction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore data source for unified user interactions (votes + bookmarks).
 * Handles direct Firestore operations for /user_interactions collection.
 *
 * Collection structure: /user_interactions/{itemId}_{userId}
 */
@Singleton
class FirestoreUnifiedUserInteractionDataSource @Inject constructor(private val firestore: FirebaseFirestore) {
    companion object {
        private const val TAG = "FirestoreUnifiedUserInteractionDS"
        private const val COLLECTION_NAME = "user_interactions"
    }

    /**
     * Get single user interaction document
     */
    suspend fun getUserInteraction(
        itemId: String,
        userId: String
    ): UserInteractionDto? =
        try {
            val docId = UserInteraction.generateId(itemId, userId)
            val document = firestore.collection(COLLECTION_NAME)
                .document(docId)
                .get()
                .await()

            if (document.exists()) {
                document.toObject<UserInteractionDto>()
            } else {
                null
            }
        } catch (e: Exception) {
            Logger.e(TAG) { "Failed to get user interaction for item: $itemId, user: $userId" }
            throw IOException("Failed to fetch user interaction", e)
        }

    /**
     * Create or update user interaction document
     */
    suspend fun upsertUserInteraction(dto: UserInteractionDto): UserInteractionDto =
        try {
            val docRef = firestore.collection(COLLECTION_NAME).document(dto.documentId)

            // Create data map excluding @DocumentId field to avoid Firestore serialization issues
            val dataMap = mapOf(
                "itemId" to dto.itemId,
                "itemType" to dto.itemType,
                "userId" to dto.userId,
                "voteState" to dto.voteState,
                "bookmarked" to dto.isBookmarked, // Use PropertyName value
                "createdAt" to dto.createdAt,
                "updatedAt" to dto.updatedAt,
            )

            // Use set with merge to create or update
            docRef.set(dataMap, com.google.firebase.firestore.SetOptions.merge()).await()

            // Return the updated document to ensure consistency
            val updatedDoc = docRef.get().await()
            updatedDoc.toObject<UserInteractionDto>() ?: dto
        } catch (e: Exception) {
            Logger.e(TAG) { "Failed to upsert user interaction: ${dto.documentId}" }
            throw IOException("Failed to save user interaction", e)
        }

    /**
     * Delete user interaction document
     */
    suspend fun deleteUserInteraction(
        itemId: String,
        userId: String
    ) {
        try {
            val docId = UserInteraction.generateId(itemId, userId)
            firestore.collection(COLLECTION_NAME)
                .document(docId)
                .delete()
                .await()
        } catch (e: Exception) {
            Logger.e(TAG) { "Failed to delete user interaction for item: $itemId, user: $userId" }
            throw IOException("Failed to delete user interaction", e)
        }
    }

    /**
     * Observe single user interaction for real-time updates
     */
    fun observeUserInteraction(
        itemId: String,
        userId: String
    ): Flow<UserInteractionDto?> =
        callbackFlow {
            val docId = UserInteraction.generateId(itemId, userId)
            val listener = firestore.collection(COLLECTION_NAME)
                .document(docId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Logger.e(TAG) { "Error observing user interaction: $docId" }
                        close(error)
                        return@addSnapshotListener
                    }

                    val dto = if (snapshot?.exists() == true) {
                        snapshot.toObject<UserInteractionDto>()
                    } else {
                        null
                    }

                    trySend(dto)
                }

            awaitClose { listener.remove() }
        }

    /**
     * Get all bookmarked content for user
     * Query: userId == user && isBookmarked == true
     */
    suspend fun getUserBookmarks(userId: String): List<UserInteractionDto> =
        try {
            val querySnapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("bookmarked", true)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { it.toObject<UserInteractionDto>() }
        } catch (e: Exception) {
            Logger.e(TAG) { "Failed to get user bookmarks for user: $userId" }
            throw IOException("Failed to fetch user bookmarks", e)
        }

    /**
     * Get all interactions for specific user
     */
    suspend fun getAllUserInteractions(userId: String): List<UserInteractionDto> =
        try {
            val querySnapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { it.toObject<UserInteractionDto>() }
        } catch (e: Exception) {
            Logger.e(TAG) { "Failed to get user interactions for user: $userId" }
            throw IOException("Failed to fetch user interactions", e)
        }

    /**
     * Get user interactions for multiple content items (batch operation)
     * Optimized for feed screens where we need user state for multiple items
     */
    suspend fun getUserInteractionsForItems(
        itemIds: List<String>,
        userId: String
    ): Map<String, UserInteractionDto> =
        try {
            // Generate document IDs for batch retrieval
            val docIds = itemIds.map { UserInteraction.generateId(it, userId) }
            val interactions = mutableMapOf<String, UserInteractionDto>()

            // Fetch documents individually (Firestore doesn't have getAll in Kotlin)
            docIds.forEach { docId ->
                val document = firestore.collection(COLLECTION_NAME).document(docId).get().await()
                if (document.exists()) {
                    val dto = document.toObject<UserInteractionDto>()
                    if (dto != null) {
                        interactions[dto.itemId] = dto
                    }
                }
            }

            interactions
        } catch (e: Exception) {
            Logger.e(TAG) { "Failed to get user interactions for items: $itemIds" }
            throw IOException("Failed to fetch batch user interactions", e)
        }

    /**
     * Get all interactions for specific content item
     * Query: itemId == contentId
     */
    suspend fun getInteractionsForContent(itemId: String): List<UserInteractionDto> =
        try {
            val querySnapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("itemId", itemId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            querySnapshot.documents.mapNotNull { it.toObject<UserInteractionDto>() }
        } catch (e: Exception) {
            Logger.e(TAG) { "Failed to get interactions for content: $itemId" }
            throw IOException("Failed to fetch content interactions", e)
        }

    /**
     * Get aggregated stats for content without returning individual interactions
     * More efficient than getInteractionsForContent when you only need counts
     */
    suspend fun getInteractionStats(itemId: String): InteractionStatsDto =
        try {
            val interactions = getInteractionsForContent(itemId)

            var upvoteCount = 0
            var downvoteCount = 0
            var bookmarkCount = 0

            interactions.forEach { dto ->
                when (dto.voteState) {
                    "UPVOTE" -> upvoteCount++
                    "DOWNVOTE" -> downvoteCount++
                }
                if (dto.isBookmarked) {
                    bookmarkCount++
                }
            }

            InteractionStatsDto(
                itemId = itemId,
                upvoteCount = upvoteCount,
                downvoteCount = downvoteCount,
                bookmarkCount = bookmarkCount,
                totalInteractions = interactions.size,
            )
        } catch (e: Exception) {
            Logger.e(TAG) { "Failed to get interaction stats for content: $itemId" }
            throw IOException("Failed to fetch interaction stats", e)
        }
}

/**
 * DTO for interaction statistics
 */
data class InteractionStatsDto(
    val itemId: String,
    val upvoteCount: Int,
    val downvoteCount: Int,
    val bookmarkCount: Int,
    val totalInteractions: Int
)
