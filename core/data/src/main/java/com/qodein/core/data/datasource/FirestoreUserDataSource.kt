package com.qodein.core.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.qodein.core.data.dto.UserDto
import com.qodein.core.data.dto.UserStatsDto
import kotlinx.coroutines.tasks.await

class FirestoreUserDataSource(private val firestore: FirebaseFirestore) {

    suspend fun incrementPromocodeCount(userId: String) {
        val fieldPath = "${UserDto.FIELD_STATS}.${UserStatsDto.FIELD_SUBMITTED_PROMOCODES_COUNT}"
        firestore.collection(UserDto.COLLECTION_NAME)
            .document(userId)
            .update(fieldPath, FieldValue.increment(1))
            .await()
    }

    suspend fun incrementPostCount(userId: String) {
        val fieldPath = "${UserDto.FIELD_STATS}.${UserStatsDto.FIELD_SUBMITTED_POSTS_COUNT}"
        firestore.collection(UserDto.COLLECTION_NAME)
            .document(userId)
            .update(fieldPath, FieldValue.increment(1))
            .await()
    }

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
}
