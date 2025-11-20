package com.qodein.core.data.repository

import com.qodein.core.data.datasource.FirestoreUserDataSource
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementation of UserRepository using Firestore as the data source.
 * DataSource already returns Result, so repository simply wraps in Flow.
 */

class UserRepositoryImpl(private val dataSource: FirestoreUserDataSource) : UserRepository {

    override fun getUserById(userId: String): Flow<Result<User, OperationError>> =
        flow {
            emit(dataSource.getUserById(userId))
        }

    override fun createUser(user: User): Flow<Result<Unit, OperationError>> =
        flow {
            emit(dataSource.createUserIfNew(user))
        }

    override fun incrementPromocodeCount(userId: String): Flow<Result<Unit, OperationError>> =
        flow {
            emit(dataSource.incrementPromocodeCount(userId))
        }

    override fun incrementPostCount(userId: String): Flow<Result<Unit, OperationError>> =
        flow {
            emit(dataSource.incrementPostCount(userId))
        }
}
