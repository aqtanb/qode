package com.qodein.core.data.repository

import com.google.firebase.firestore.FirebaseFirestoreException
import com.qodein.core.data.datasource.FirestoreUserDataSource
import com.qodein.core.data.mapper.toUser
import com.qodein.core.data.mapper.toUserDto
import com.qodein.core.data.util.ErrorMapper
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.model.User
import timber.log.Timber
import java.io.IOException

class UserRepositoryImpl(private val dataSource: FirestoreUserDataSource) : UserRepository {

    override suspend fun getUserById(userId: String): Result<User, OperationError> =
        try {
            val dto = dataSource.getUserById(userId)
            dto?.let { Result.Success(it.toUser()) }
                ?: Result.Error(UserError.ProfileFailure.NotFound)
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
            dataSource.createUser(user.toUserDto())
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

    override suspend fun incrementPromocodeCount(userId: String): Result<Unit, OperationError> =
        try {
            dataSource.incrementPromocodeCount(userId)
            Result.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "Firestore error incrementing promocode count: $userId")
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: IOException) {
            Timber.e(e, "Network error incrementing promocode count: $userId")
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unknown error incrementing promocode count: $userId")
            Result.Error(SystemError.Unknown)
        }

    override suspend fun incrementPostCount(userId: String): Result<Unit, OperationError> =
        try {
            dataSource.incrementPostCount(userId)
            Result.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Timber.e(e, "Firestore error incrementing post count: $userId")
            Result.Error(ErrorMapper.mapFirestoreException(e))
        } catch (e: IOException) {
            Timber.e(e, "Network error incrementing post count: $userId")
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Timber.e(e, "Unknown error incrementing post count: $userId")
            Result.Error(SystemError.Unknown)
        }
}
