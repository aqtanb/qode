package com.qodein.core.data.datasource

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.toObject
import com.qodein.core.data.dto.UserDto
import com.qodein.core.data.dto.UserStatsDto
import com.qodein.core.data.util.ErrorMapper
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.FirestoreError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.IOException

class FirestoreUserDataSource(private val firestore: FirebaseFirestore) {

    suspend fun incrementPromocodeCount(userId: String): Result<Unit, OperationError> =
        try {
            val fieldPath = "${UserDto.FIELD_STATS}.${UserStatsDto.FIELD_SUBMITTED_PROMOCODES_COUNT}"
            firestore.collection(UserDto.COLLECTION_NAME)
                .document(userId)
                .update(fieldPath, FieldValue.increment(1))
                .await()
            Result.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "Firestore error incrementing promocode count for user: $userId")
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: IOException) {
            Timber.e(e, "Network error incrementing promocode count for user: $userId")
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unknown error incrementing promocode count for user: $userId")
            Result.Error(SystemError.Unknown)
        }

    suspend fun incrementPostCount(userId: String): Result<Unit, OperationError> =
        try {
            val fieldPath = "${UserDto.FIELD_STATS}.${UserStatsDto.FIELD_SUBMITTED_POSTS_COUNT}"
            firestore.collection(UserDto.COLLECTION_NAME)
                .document(userId)
                .update(fieldPath, FieldValue.increment(1))
                .await()
            Result.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "Firestore error incrementing post count for user: $userId")
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: IOException) {
            Timber.e(e, "Network error incrementing post count for user: $userId")
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unknown error incrementing post count for user: $userId")
            Result.Error(SystemError.Unknown)
        }

    suspend fun createUser(userDto: UserDto): Result<Unit, OperationError> =
        try {
            firestore.collection(UserDto.COLLECTION_NAME)
                .document(userDto.documentId)
                .set(userDto)
                .await()
            Result.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "Firestore error creating user: ${userDto.documentId}")
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: IOException) {
            Timber.e(e, "Network error creating user: ${userDto.documentId}")
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unknown error creating user: ${userDto.documentId}")
            Result.Error(SystemError.Unknown)
        }

    suspend fun getUserById(userId: String): Result<UserDto, OperationError> =
        try {
            val dto = firestore.collection(UserDto.COLLECTION_NAME)
                .document(userId)
                .get()
                .await()
                .toObject<UserDto>()

            dto?.let { Result.Success(it) }
                ?: Result.Error(FirestoreError.NotFound)
        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "Firestore error getting user: $userId")
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: IOException) {
            Timber.e(e, "Network error getting user: $userId")
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unknown error getting user: $userId")
            Result.Error(SystemError.Unknown)
        }
}
