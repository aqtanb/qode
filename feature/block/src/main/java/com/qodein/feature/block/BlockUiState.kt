package com.qodein.feature.block

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.UserId

sealed interface BlockUiState {
    data class Confirming(val userId: UserId, val username: String?, val photoUrl: String?, val isLoading: Boolean = false) : BlockUiState

    data object Success : BlockUiState
    data class Error(val error: OperationError) : BlockUiState
}
