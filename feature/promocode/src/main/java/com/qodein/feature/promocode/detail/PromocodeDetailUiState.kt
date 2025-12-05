package com.qodein.feature.promocode.detail

import com.qodein.core.ui.component.AuthPromptAction
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
import com.qodein.shared.model.VoteState

data class AuthBottomSheetState(val action: AuthPromptAction, val isLoading: Boolean = false)

data class PromocodeDetailUiState(
    val promocodeId: PromocodeId,
    val userId: UserId? = null,

    val promocodeState: PromocodeUiState = PromocodeUiState.Loading,
    val userInteraction: UserInteraction? = null,
    val currentVoting: VoteState? = null,
    val optimisticUpvotes: Int? = null,
    val optimisticDownvotes: Int? = null,
    val isSharing: Boolean = false,
    val isCopying: Boolean = false,
    val transientError: OperationError? = null,

    val authBottomSheet: AuthBottomSheetState? = null
)

sealed interface PromocodeUiState {
    data object Loading : PromocodeUiState
    data class Success(val data: Promocode) : PromocodeUiState
    data class Error(val error: OperationError) : PromocodeUiState
}
