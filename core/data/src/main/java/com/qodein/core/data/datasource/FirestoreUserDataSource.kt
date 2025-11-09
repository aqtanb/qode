package com.qodein.core.data.datasource

import co.touchlab.kermit.Logger
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.qodein.core.data.dto.UserDto
import com.qodein.core.data.mapper.UserMapper
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.model.User
import com.qodein.shared.model.UserId
import kotlinx.coroutines.tasks.await
import java.io.IOException

class FirestoreUserDataSource constructor(private val firestore: FirebaseFirestore) {
    companion object {
        private const val TAG = "FirestoreUserDS"
        private const val USERS_COLLECTION = "users"
    }

    suspend fun incrementPromocodeCount(userId: String): Result<Unit, OperationError> =
        try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .set(
                    mapOf("stats" to mapOf("submittedPromocodesCount" to FieldValue.increment(1))),
                    SetOptions.merge(),
                )
                .await()

            Logger.i(TAG) { "Incremented promocode count for user: $userId" }
            Result.Success(Unit)
        } catch (e: SecurityException) {
            Logger.e(TAG, e) { "Unauthorized to update user stats: $userId" }
            Result.Error(SystemError.PermissionDenied)
        } catch (e: IOException) {
            Logger.e(TAG, e) { "Network error updating user stats: $userId" }
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Failed to increment promocode count: $userId" }
            Result.Error(SystemError.Unknown)
        }

    suspend fun incrementPostCount(userId: String): Result<Unit, OperationError> =
        try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .set(
                    mapOf("stats" to mapOf("submittedPostsCount" to FieldValue.increment(1))),
                    SetOptions.merge(),
                )
                .await()

            Logger.i(TAG) { "Incremented post count for user: $userId" }
            Result.Success(Unit)
        } catch (e: SecurityException) {
            Logger.e(TAG, e) { "Unauthorized to update user stats: $userId" }
            Result.Error(SystemError.PermissionDenied)
        } catch (e: IOException) {
            Logger.e(TAG, e) { "Network error updating user stats: $userId" }
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Failed to increment post count: $userId" }
            Result.Error(SystemError.Unknown)
        }

    suspend fun createUserIfNew(user: User): Result<Unit, OperationError> =
        try {
            val docRef = firestore.collection(USERS_COLLECTION).document(user.id.value)
            val snapshot = docRef.get().await()

            if (snapshot.exists()) {
                Logger.i(TAG) { "User already exists in Firestore: ${user.id.value}" }
                Result.Success(Unit)
            } else {
                val userDto = UserMapper.toDto(user)
                docRef.set(userDto).await()
                Logger.i(TAG) { "Created new user in Firestore: ${user.id.value}" }
                Result.Success(Unit)
            }
        } catch (e: SecurityException) {
            Logger.e(TAG, e) { "Unauthorized to create user: ${user.id.value}" }
            Result.Error(SystemError.PermissionDenied)
        } catch (e: IOException) {
            Logger.e(TAG, e) { "Network error creating user: ${user.id.value}" }
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Failed to create user: ${user.id.value}" }
            Result.Error(SystemError.Unknown)
        }

    suspend fun getUserById(userId: String): Result<User, OperationError> =
        try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (!snapshot.exists()) {
                Logger.w(TAG) { "User not found in Firestore: $userId" }
                Result.Error(SystemError.Unknown)
            } else {
                val userDto = snapshot.toObject(UserDto::class.java)
                if (userDto == null) {
                    Logger.e(TAG) { "Failed to parse user document: $userId" }
                    Result.Error(SystemError.Unknown)
                } else {
                    val user = UserMapper.toDomain(userDto, UserId(userId))
                    Logger.i(TAG) { "Fetched user from Firestore: $userId" }
                    Result.Success(user)
                }
            }
        } catch (e: SecurityException) {
            Logger.e(TAG, e) { "Unauthorized to fetch user: $userId" }
            Result.Error(SystemError.PermissionDenied)
        } catch (e: IOException) {
            Logger.e(TAG, e) { "Network error fetching user: $userId" }
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Failed to fetch user: $userId" }
            Result.Error(SystemError.Unknown)
        }
}
