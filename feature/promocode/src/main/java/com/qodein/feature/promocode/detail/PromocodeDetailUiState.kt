package com.qodein.feature.promocode.detail

import com.qodein.core.ui.component.AuthPromptAction
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.PromoCodeWithUserState
import com.qodein.shared.model.VoteState

data class AuthBottomSheetState(val action: AuthPromptAction, val isLoading: Boolean = false)

data class PromocodeDetailUiState(
    // Data State
    val promoCodeId: PromoCodeId,
    val promoCodeWithUserState: PromoCodeWithUserState? = null,
    val isLoading: Boolean = false,
    val errorType: OperationError? = null,

    // Interaction States
    val isSharing: Boolean = false,
    val isCopying: Boolean = false,

    // UI States
    val showVoteAnimation: Boolean = false,
    val lastVoteType: VoteState? = null,
    val authBottomSheet: AuthBottomSheetState? = null,

    // TODO Follow states
    val isFollowingService: Boolean = false
) {
    val hasError: Boolean get() = errorType != null
    val hasData: Boolean get() = promoCodeWithUserState != null
    val isEmpty: Boolean get() = !isLoading && !hasError && !hasData
}
