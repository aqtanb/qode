package com.qodein.feature.promocode.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.qodein.core.ui.AuthPromptAction
import com.qodein.feature.promocode.navigation.PromocodeDetailRoute
import com.qodein.shared.common.Result
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.interaction.GetUserInteractionUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleVoteUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodeShareContentUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodeUseCase
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PromocodeDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val getPromocodeUseCase: GetPromocodeUseCase,
    private val getUserInteractionUseCase: GetUserInteractionUseCase,
    private val toggleVoteUseCase: ToggleVoteUseCase,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val getPromocodeShareContentUseCase: GetPromocodeShareContentUseCase
) : ViewModel() {

    private val args: PromocodeDetailRoute = savedStateHandle.toRoute()
    private val promocodeId = PromocodeId(args.promoCodeId)

    private val _uiState = MutableStateFlow(PromocodeDetailUiState(promocodeId = promocodeId))
    val uiState: StateFlow<PromocodeDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PromocodeDetailEvent>()
    val events = _events.asSharedFlow()

    init {
        observeAuthState()
        refreshPromocode()
    }

    fun onAction(action: PromocodeDetailAction) {
        when (action) {
            is PromocodeDetailAction.RefreshData -> refreshPromocode()
            is PromocodeDetailAction.ToggleVoteClicked -> {
                toggleVote(action.voteState)
            }
            is PromocodeDetailAction.ShareClicked -> handleShare()
            is PromocodeDetailAction.CopyCodeClicked -> handleCopyCode()
            is PromocodeDetailAction.BackClicked -> handleBack()
            is PromocodeDetailAction.BlockUserClicked -> blockUser(action.userId)
            is PromocodeDetailAction.ReportPromocodeClicked -> report(action.promocodeId)
            is PromocodeDetailAction.RetryClicked -> refreshPromocode()
        }
    }

    private suspend fun loadPromocode(promocodeId: PromocodeId) {
        _uiState.update { it.copy(promocodeState = PromocodeUiState.Loading) }
        when (val result = getPromocodeUseCase(promocodeId)) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        promocodeState = PromocodeUiState.Success(result.data),
                    )
                }
            }
            is Result.Error -> {
                _uiState.update { it.copy(promocodeState = PromocodeUiState.Error(result.error)) }
            }
        }
    }

    private fun refreshPromocode() {
        viewModelScope.launch {
            loadPromocode(promocodeId)
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            getAuthStateUseCase().collectLatest { authState ->
                when (authState) {
                    is AuthState.Authenticated -> {
                        _uiState.update { it.copy(currentUserId = authState.userId) }
                        loadUserInteractions(
                            userId = authState.userId,
                            itemId = promocodeId.value,
                        )
                    }

                    AuthState.Unauthenticated -> {
                        _uiState.update { it.copy(currentUserId = null, userVoteState = VoteState.NONE) }
                    }
                }
            }
        }
    }

    private fun toggleVote(targetVoteState: VoteState) {
        viewModelScope.launch {
            val userId = _uiState.value.currentUserId
            if (userId == null) {
                _events.emit(PromocodeDetailEvent.NavigateToAuth(AuthPromptAction.Vote))
                return@launch
            }

            val currentVoteState = _uiState.value.userVoteState
            val optimisticVoteState = currentVoteState.toggleTo(targetVoteState)
            val scoreDelta = VoteState.computeScoreDelta(currentVoteState, optimisticVoteState)
            val currentDelta = _uiState.value.voteScoreDelta

            _uiState.update {
                it.copy(
                    userVoteState = optimisticVoteState,
                    voteScoreDelta = currentDelta + scoreDelta,
                )
            }

            when (
                val result = toggleVoteUseCase(
                    itemId = promocodeId.value,
                    itemType = ContentType.PROMOCODE,
                    userId = userId,
                    currentVoteState = currentVoteState,
                    isBookmarked = false,
                    targetVoteState = targetVoteState,
                )
            ) {
                is Result.Error -> {
                    _uiState.update { it.copy(userVoteState = currentVoteState, voteScoreDelta = currentDelta) }
                    _events.emit(PromocodeDetailEvent.ShowError(result.error))
                }
                is Result.Success -> {
                    _uiState.update { it.copy(userVoteState = result.data.voteState) }
                }
            }
        }
    }

    private fun handleShare() {
        viewModelScope.launch {
            when (val result = getPromocodeShareContentUseCase(promocodeId)) {
                is Result.Success -> {
                    _events.emit(PromocodeDetailEvent.SharePromocode(result.data))
                }
                is Result.Error -> {
                    _events.emit(PromocodeDetailEvent.ShowError(result.error))
                }
            }
        }
    }

    private fun handleCopyCode() {
        val promoState = _uiState.value.promocodeState
        if (promoState !is PromocodeUiState.Success) return
        val currentPromoCode = promoState.data

        viewModelScope.launch {
            _events.emit(PromocodeDetailEvent.CopyCodeToClipboard(currentPromoCode.code))
        }
    }

    private fun handleBack() {
        viewModelScope.launch {
            _events.emit(PromocodeDetailEvent.NavigateBack)
        }
    }

    private suspend fun loadUserInteractions(
        itemId: String,
        userId: UserId
    ) {
        when (val result = getUserInteractionUseCase(itemId, ContentType.PROMOCODE, userId)) {
            is Result.Error -> _events.emit(PromocodeDetailEvent.ShowError(result.error))
            is Result.Success -> _uiState.update {
                it.copy(userVoteState = result.data?.voteState ?: VoteState.NONE)
            }
        }
    }

    private fun report(promocodeId: PromocodeId) {
        viewModelScope.launch {
            _uiState.value.currentUserId ?: run {
                _events.emit(PromocodeDetailEvent.NavigateToAuth(AuthPromptAction.ReportContent))
                return@launch
            }
            val promocodeUiState = _uiState.value.promocodeState as? PromocodeUiState.Success ?: return@launch
            val currentPromocode = promocodeUiState.data
            _events.emit(
                PromocodeDetailEvent.NavigateToReport(
                    reportedItemId = promocodeId.value,
                    itemTitle = currentPromocode.code,
                    itemAuthor = currentPromocode.authorUsername,
                ),
            )
        }
    }

    private fun blockUser(userId: UserId) {
        viewModelScope.launch {
            _uiState.value.currentUserId ?: run {
                _events.emit(PromocodeDetailEvent.NavigateToAuth(AuthPromptAction.ReportContent))
                return@launch
            }
            val promocodeUiState = _uiState.value.promocodeState as? PromocodeUiState.Success ?: return@launch
            val currentPromocode = promocodeUiState.data
            _events.emit(
                PromocodeDetailEvent.NavigateToBlockUser(
                    userId = userId,
                    username = currentPromocode.authorUsername,
                    photoUrl = currentPromocode.authorAvatarUrl,
                ),
            )
        }
    }
}
