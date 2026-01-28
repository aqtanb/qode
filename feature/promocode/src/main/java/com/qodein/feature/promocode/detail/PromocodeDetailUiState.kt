package com.qodein.feature.promocode.detail

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState

data class PromocodeDetailUiState(
    val promocodeId: PromocodeId,
    val currentUserId: UserId? = null,
    val promocodeState: PromocodeUiState = PromocodeUiState.Loading,
    val userVoteState: VoteState = VoteState.NONE,
    val voteScoreDelta: Int = 0,
    val isCopying: Boolean = false,
    val isRefreshing: Boolean = false
)

sealed interface PromocodeUiState {
    data object Loading : PromocodeUiState
    data class Success(val data: Promocode) : PromocodeUiState
    data class Error(val error: OperationError) : PromocodeUiState
}
