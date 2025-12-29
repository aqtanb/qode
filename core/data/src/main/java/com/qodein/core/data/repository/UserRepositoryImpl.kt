package com.qodein.core.data.repository

import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.functions.FirebaseFunctionsException
import com.qodein.core.data.datasource.FirestoreUserDataSource
import com.qodein.core.data.mapper.UserMapper
import com.qodein.core.data.util.ErrorMapper
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.model.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException

class UserRepositoryImpl(private val dataSource: FirestoreUserDataSource) : UserRepository {

    override suspend fun getUserById(userId: String): Result<User, OperationError> =
        try {
            val dto = dataSource.getUserById(userId)
            if (dto == null) {
                Timber.w("User not found in Firestore: $userId")
                Result.Error(UserError.ProfileFailure.NotFound)
            } else {
                Result.Success(UserMapper.toDomain(dto))
            }
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

    override suspend fun createUser(user: User): Result<Unit, OperationError> =
        try {
            dataSource.createUser(UserMapper.toDto(user))
            Result.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "Firestore error creating user: ${user.id}")
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: IOException) {
            Timber.e(e, "Network error creating user: ${user.id}")
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unknown error creating user: ${user.id}")
            Result.Error(SystemError.Unknown)
        }

    override suspend fun updateUserConsent(
        userId: String,
        legalPoliciesAcceptedAt: Long
    ): Result<Unit, OperationError> =
        try {
            dataSource.updateUserConsent(userId, legalPoliciesAcceptedAt)
            Result.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "Firestore error updating user consent: $userId")
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: IOException) {
            Timber.e(e, "Network error updating user consent: $userId")
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unknown error updating user consent: $userId")
            Result.Error(SystemError.Unknown)
        }

    override fun observeUser(userId: String): Flow<Result<User, OperationError>> =
        dataSource.observeUser(userId)
            .map { dto ->
                if (dto != null) {
                    Result.Success(UserMapper.toDomain(dto))
                } else {
                    Result.Error(UserError.ProfileFailure.NotFound as OperationError)
                }
            }
            .catch { e ->
                if (e is CancellationException) throw e
                Timber.e(e, "Error observing user: $userId")
                val opError: OperationError = when (e) {
                    is FirebaseFirestoreException -> ErrorMapper.mapFirestoreException(e)
                    is IOException -> SystemError.Offline
                    else -> SystemError.Unknown
                }
                emit(Result.Error(opError))
            }

    override suspend fun blockUser(
        currentUserId: String,
        blockedUserId: String
    ): Result<Unit, OperationError> =
        try {
            Timber.i("Blocking user: $blockedUserId by $currentUserId")
            dataSource.blockUser(currentUserId, blockedUserId)
            Timber.i("Successfully blocked user: $blockedUserId")
            Result.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "Firestore error blocking user: $blockedUserId")
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: IOException) {
            Timber.e(e, "Network error blocking user: $blockedUserId")
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unknown error blocking user: $blockedUserId")
            Result.Error(SystemError.Unknown)
        }

    override fun getBlockedUserIds(currentUserId: String): Flow<Set<String>> = dataSource.getBlockedUserIds(currentUserId)

    override suspend fun deleteUserAccount(userId: String): Result<Unit, OperationError> =
        try {
            Timber.i("Deleting user account: $userId")
            dataSource.deleteUserAccount(userId)
            Timber.i("Successfully deleted user account: $userId")
            Result.Success(Unit)
        } catch (e: FirebaseFunctionsException) {
            Timber.e(e, "Cloud Function error deleting account: $userId")

            if (e.details != null) {
                Result.Error(UserError.DeletionFailure.PartialDeletion)
            } else {
                when (e.code) {
                    FirebaseFunctionsException.Code.UNAUTHENTICATED ->
                        Result.Error(UserError.DeletionFailure.NotAuthenticated)
                    else ->
                        Result.Error(UserError.DeletionFailure.DeleteFailed)
                }
            }
        } catch (e: IOException) {
            Timber.e(e, "Network error deleting account: $userId")
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unknown error deleting account: $userId")
            Result.Error(SystemError.Unknown)
        }
}
