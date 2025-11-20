package com.qodein.core.data.repository

import com.qodein.core.data.datasource.FirebaseAuthDataSource
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class AuthRepositoryImpl(private val dataSource: FirebaseAuthDataSource) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }

    override suspend fun signInWithGoogle(): Result<User, OperationError> {
        try {
            val firebaseUser = dataSource.signIn()
            return Result.Success(result)
        } catch (e: Exception) {
            Timber.e(e)
            return Result.Error(OperationError.UnknownError)
        }
    }

    override suspend fun signOut(): Result<Unit, OperationError> {
    }

    override suspend fun getAuthStateFlow(): Flow<User?> {
        dataSource.getAuthStateFlow().map { firebaseUser ->
            firebaseUser?.let { user ->
                User.create(
                    id = user.uid,
                    email = user.email,
                    profile = TODO(),
                )
            }
        }
    }
}
