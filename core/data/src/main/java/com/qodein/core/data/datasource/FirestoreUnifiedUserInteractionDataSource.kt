package com.qodein.core.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.qodein.core.data.model.UserInteractionDto
import com.qodein.shared.model.UserInteraction
import kotlinx.coroutines.tasks.await

/**
 * Firestore data source for unified user interactions (votes + bookmarks).
 * Handles direct Firestore operations for /user_interactions collection.
 *
 * Collection structure: /user_interactions/{itemId}_{userId}
 */

class FirestoreUnifiedUserInteractionDataSource constructor(private val firestore: FirebaseFirestore) {
    companion object {
        private const val COLLECTION_NAME = "user_interactions"
    }

    /**
     * Get single user interaction document.
     * Returns null if document doesn't exist (user hasn't interacted yet).
     *
     * @throws com.google.firebase.firestore.FirebaseFirestoreException on Firestore errors
     */
    suspend fun getUserInteraction(
        itemId: String,
        userId: String
    ): UserInteractionDto? {
        val docId = UserInteraction.generateId(itemId, userId)
        val document = firestore.collection(COLLECTION_NAME)
            .document(docId)
            .get()
            .await()

        return if (document.exists()) {
            document.toObject<UserInteractionDto>()
        } else {
            null
        }
    }

    /**
     * Create or update user interaction document.
     *
     * @throws com.google.firebase.firestore.FirebaseFirestoreException on Firestore errors
     */
    suspend fun upsertUserInteraction(dto: UserInteractionDto): UserInteractionDto {
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
        return updatedDoc.toObject<UserInteractionDto>() ?: dto
    }

    /**
     * Delete user interaction document.
     *
     * @throws com.google.firebase.firestore.FirebaseFirestoreException on Firestore errors
     */
    suspend fun deleteUserInteraction(
        itemId: String,
        userId: String
    ) {
        val docId = UserInteraction.generateId(itemId, userId)
        firestore.collection(COLLECTION_NAME)
            .document(docId)
            .delete()
            .await()
    }

    /**
     * Get all bookmarked content for user
     * Query: userId == user && isBookmarked == true
     */
    suspend fun getUserBookmarks(userId: String): List<UserInteractionDto> {
        val querySnapshot = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("userId", userId)
            .whereEqualTo("bookmarked", true)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { it.toObject<UserInteractionDto>() }
    }

    /**
     * Get all interactions for specific user
     */
    suspend fun getAllUserInteractions(userId: String): List<UserInteractionDto> {
        val querySnapshot = firestore.collection(COLLECTION_NAME)
            .whereEqualTo("userId", userId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { it.toObject<UserInteractionDto>() }
    }
}
