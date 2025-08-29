package com.qodein.feature.promocode.detail

import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.model.PromoCode

data class PromocodeDetailUiState(
    // Data State
    val promoCode: PromoCode? = null,
    val isLoading: Boolean = false,
    val errorType: ErrorType? = null,

    // Interaction States
    val isVoting: Boolean = false,
    val isBookmarked: Boolean = false,
    val isSharing: Boolean = false,
    val isCopying: Boolean = false,

    // UI States
    val showVoteAnimation: Boolean = false,
    val lastVoteType: VoteType? = null,

    // TODO Follow states (as requested by user)
    val isFollowingService: Boolean = false,
    val isFollowingCategory: Boolean = false
) {
    val hasError: Boolean get() = errorType != null
    val hasData: Boolean get() = promoCode != null
    val isEmpty: Boolean get() = !isLoading && !hasError && !hasData
}

enum class VoteType {
    UPVOTE,
    DOWNVOTE
}
