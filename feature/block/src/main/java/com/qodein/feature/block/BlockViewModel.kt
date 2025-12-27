package com.qodein.feature.block

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.ui.refresh.RefreshTarget
import com.qodein.core.ui.refresh.ScreenRefreshCoordinator
import com.qodein.shared.common.Result
import com.qodein.shared.domain.usecase.user.BlockUserUseCase
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BlockViewModel(
    internal val savedStateHandle: SavedStateHandle,
    private val userId: UserId,
    private val username: String?,
    private val photoUrl: String?,
    private val blockUserUseCase: BlockUserUseCase,
    private val analyticsHelper: AnalyticsHelper,
    private val refreshCoordinator: ScreenRefreshCoordinator
) : ViewModel() {

    private val _uiState = MutableStateFlow<BlockUiState>(
        BlockUiState.Confirming(userId, username, photoUrl),
    )
    val uiState: StateFlow<BlockUiState> = _uiState.asStateFlow()

    fun onAction(action: BlockAction) {
        when (action) {
            BlockAction.ConfirmBlock -> handleBlock()
            BlockAction.CancelBlock -> {}
            BlockAction.DismissError -> _uiState.value = BlockUiState.Confirming(userId, username, photoUrl)
        }
    }

    private fun handleBlock() {
        viewModelScope.launch {
            _uiState.value = BlockUiState.Confirming(userId, username, photoUrl, isLoading = true)
            when (val result = blockUserUseCase(userId)) {
                is Result.Success -> {
                    analyticsHelper.logEvent(
                        AnalyticsEvent(
                            type = "block_user",
                            extras = listOf(
                                AnalyticsEvent.Param("blocked_user_id", userId.value),
                            ),
                        ),
                    )
                    refreshCoordinator.triggerRefresh(RefreshTarget.HOME)
                    _uiState.value = BlockUiState.Success
                }
                is Result.Error -> _uiState.value = BlockUiState.Error(result.error)
            }
        }
    }
}
