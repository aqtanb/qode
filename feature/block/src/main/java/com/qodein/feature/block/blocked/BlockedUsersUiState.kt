package com.qodein.feature.block.blocked

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.User

sealed interface BlockedUsersUiState {
    data object Loading : BlockedUsersUiState
    data class Success(val blockedUsers: List<User> = emptyList(), val dialogState: UnblockDialogState) : BlockedUsersUiState
    data class Error(val error: OperationError) : BlockedUsersUiState
}

sealed interface UnblockDialogState {
    data object Hidden : UnblockDialogState
    sealed interface Visible : UnblockDialogState {
        val user: User
    }
    data class Idle(override val user: User) : Visible
    data class Loading(override val user: User) : Visible
    data class Error(override val user: User, val error: OperationError) : Visible
}
