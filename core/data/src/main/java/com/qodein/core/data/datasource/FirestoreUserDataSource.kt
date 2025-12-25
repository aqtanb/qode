package com.qodein.core.data.datasource

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import com.qodein.core.data.dto.UserDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirestoreUserDataSource(private val firestore: FirebaseFirestore) {
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
}
