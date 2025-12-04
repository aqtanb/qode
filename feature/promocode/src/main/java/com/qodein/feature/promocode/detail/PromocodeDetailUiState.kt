package com.qodein.feature.promocode.detail

import com.qodein.core.ui.component.AuthPromptAction
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.PromocodeInteraction
import com.qodein.shared.model.UserId
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
}
