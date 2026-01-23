package com.qodein.core.data.datasource

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.functions.FirebaseFunctions
import com.qodein.core.data.datasource.util.applyPaginationCursor
import com.qodein.core.data.datasource.util.toPagedResult
import com.qodein.core.data.dto.BlockedUserDto
import com.qodein.core.data.dto.PagedFirestoreResult
import com.qodein.core.data.dto.UserDto
import com.qodein.core.data.dto.UserInteractionDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirestoreUserDataSource(private val firestore: FirebaseFirestore, private val functions: FirebaseFunctions) {
    suspend fun createUser(userDto: UserDto) {
        firestore.collection(UserDto.COLLECTION_NAME)
            .document(userDto.documentId)
            .set(userDto)
            .await()
    }

    suspend fun getUserById(userId: String): UserDto? =
        firestore.collection(UserDto.COLLECTION_NAME)
            .document(userId)
            .get()
            .await()
            .toObject<UserDto>()

    suspend fun updateUserConsent(
        userId: String,
        legalPoliciesAcceptedAt: Long
    ) {
        firestore.collection(UserDto.COLLECTION_NAME)
            .document(userId)
            .update("consent.legalPoliciesAcceptedAt", Timestamp(Date(legalPoliciesAcceptedAt)))
            .await()
    }

    fun observeUser(userId: String): Flow<UserDto?> =
        callbackFlow {
            var registration: ListenerRegistration? = null
            try {
                registration = firestore.collection(UserDto.COLLECTION_NAME)
                    .document(userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }
                        trySend(snapshot?.toObject<UserDto>())
                    }
            } catch (e: Exception) {
                close(e)
            }
            awaitClose { registration?.remove() }
        }

    suspend fun blockUser(
        currentUserId: String,
        blockedUserId: String
    ) {
        firestore.collection(UserDto.COLLECTION_NAME)
            .document(currentUserId)
            .collection("blocks")
            .document(blockedUserId)
            .set(
                hashMapOf(
                    "blockedAt" to Timestamp.now(),
                ),
            )
            .await()
    }

    suspend fun unblockUser(
        currentUserId: String,
        blockedUserId: String
    ) {
        firestore.collection(UserDto.COLLECTION_NAME)
            .document(currentUserId)
            .collection(UserDto.SUBCOLLECTION_BLOCKS)
            .document(blockedUserId)
            .delete()
    }

    suspend fun getBlockedUserIds(currentUserId: String): Set<String> =
        firestore.collection(UserDto.COLLECTION_NAME)
            .document(currentUserId)
            .collection(UserDto.SUBCOLLECTION_BLOCKS)
            .get()
            .await()
            .documents
            .map { it.id }
            .toSet()

    suspend fun getBlockedUsers(
        currentUserId: String,
        limit: Int,
        startAfter: DocumentSnapshot?
    ): PagedFirestoreResult<BlockedUserDto> {
        val fetchLimit = limit + 1
        val documents = firestore.collection(UserDto.COLLECTION_NAME)
            .document(currentUserId)
            .collection(UserDto.SUBCOLLECTION_BLOCKS)
            .orderBy(BlockedUserDto.FIELD_BLOCKED_AT, Query.Direction.DESCENDING)
            .applyPaginationCursor(startAfter)
            .limit(fetchLimit.toLong())
            .get()
            .await()
            .documents

        return documents.toPagedResult<BlockedUserDto>(limit)
    }

    suspend fun deleteUserAccount(userId: String) {
        functions
            .getHttpsCallable("deleteUserAccount")
            .call()
            .await()
    }

    suspend fun getUserInteraction(
        documentId: String,
        userId: String
    ): UserInteractionDto? =
        firestore.collection(UserDto.COLLECTION_NAME)
            .document(userId)
            .collection(UserDto.SUBCOLLECTION_INTERACTIONS)
            .document(documentId)
            .get()
            .await()
            .toObject<UserInteractionDto>()

    suspend fun setUserInteraction(dto: UserInteractionDto) {
        firestore.collection(UserDto.COLLECTION_NAME)
            .document(dto.userId)
            .collection(UserDto.SUBCOLLECTION_INTERACTIONS)
            .document(dto.documentId)
            .set(dto)
            .await()
    }

    suspend fun deleteUserInteraction(
        documentId: String,
        userId: String
    ) {
        firestore.collection(UserDto.COLLECTION_NAME)
            .document(userId)
            .collection(UserDto.SUBCOLLECTION_INTERACTIONS)
            .document(documentId)
            .delete()
            .await()
    }
}
