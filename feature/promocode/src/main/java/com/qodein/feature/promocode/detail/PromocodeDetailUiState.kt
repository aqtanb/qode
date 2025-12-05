package com.qodein.feature.promocode.detail

import com.qodein.core.ui.component.AuthPromptAction
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.PromocodeInteraction
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
import com.qodein.shared.model.VoteState

data class AuthBottomSheetState(val action: AuthPromptAction, val isLoading: Boolean = false)

data class PromocodeDetailUiState(
    val promocodeId: PromocodeId,
    val userId: UserId? = null,
    val promocodeInteraction: PromocodeInteraction? = null,
    val isLoading: Boolean = false,
    val errorType: OperationError? = null,

    val isSharing: Boolean = false,
    val isCopying: Boolean = false,

    val showVoteAnimation: Boolean = false,
    val lastVoteType: VoteState? = null,
    val authBottomSheet: AuthBottomSheetState? = null
) {
    val hasData: Boolean get() = promocodeInteraction != null
    val voteState: VoteState? get() = promocodeInteraction?.userInteraction?.voteState
    val promocode: Promocode? get() = promocodeInteraction?.promocode
    val userInteraction: UserInteraction? get() = promocodeInteraction?.userInteraction
}

sealed interface InteractionUiState {
    data object None : InteractionUiState
    data object Loading : InteractionUiState
    data class Success(val interaction: UserInteraction) : InteractionUiState
    data class Error(val error: OperationError) : InteractionUiState
}

sealed interface PromocodeUiState {
    data object Loading : PromocodeUiState
    data class Success(val data: Promocode) : PromocodeUiState
    data class Error(val error: OperationError) : PromocodeUiState
}
