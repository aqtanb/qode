package com.qodein.feature.promocode.detail

import com.qodein.core.ui.component.AuthPromptAction
import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.model.PromoCodeWithUserState
import com.qodein.shared.model.VoteState

data class AuthBottomSheetState(val action: AuthPromptAction, val isLoading: Boolean = false)

data class PromocodeDetailUiState(
    // Data State
    val promoCodeWithUserState: PromoCodeWithUserState? = null,
    val isLoading: Boolean = false,
    val errorType: ErrorType? = null,

    // Interaction States
    val isVoting: Boolean = false,
    val isBookmarked: Boolean = false,
    val isSharing: Boolean = false,
    val isCopying: Boolean = false,

    // UI States
    val showVoteAnimation: Boolean = false,
    val lastVoteType: VoteState? = null,
    val authBottomSheet: AuthBottomSheetState? = null,

    // TODO Follow states (as requested by user)
    val isFollowingService: Boolean = false,
    val isFollowingCategory: Boolean = false
) {
    val hasError: Boolean get() = errorType != null
    val hasData: Boolean get() = promoCodeWithUserState != null
    val isEmpty: Boolean get() = !isLoading && !hasError && !hasData
}
