package com.qodein.shared.domain.usecase.user

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn

class ObserveCurrentUserUseCase(getAuthStateUseCase: GetAuthStateUseCase, userRepository: UserRepository, scope: CoroutineScope) {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val sharedFlow = getAuthStateUseCase().flatMapLatest { authState ->
        when (authState) {
            is AuthState.Authenticated -> {
                userRepository.observeUser(authState.userId.value)
            }
            AuthState.Unauthenticated -> {
                flowOf(Result.Error(UserError.AuthenticationFailure.NoCredentialsAvailable))
            }
        }
    }.shareIn(scope, SharingStarted.WhileSubscribed(5000), replay = 1)

    operator fun invoke(): Flow<Result<User, OperationError>> = sharedFlow
}
