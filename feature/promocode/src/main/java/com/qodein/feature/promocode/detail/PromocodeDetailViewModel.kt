package com.qodein.feature.promocode.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.ui.auth.IdTokenProvider
import com.qodein.core.ui.component.AuthPromptAction
import com.qodein.shared.common.Result
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import com.qodein.shared.domain.usecase.interaction.GetUserInteractionUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleVoteUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodeUseCase
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = PromocodeDetailViewModel.Factory::class)
class PromocodeDetailViewModel @AssistedInject constructor(
    @Assisted promoCodeIdString: String,
    private val getPromocodeUseCase: GetPromocodeUseCase,
    private val getUserInteractionUseCase: GetUserInteractionUseCase,
    private val toggleVoteUseCase: ToggleVoteUseCase,
    private val analyticsHelper: AnalyticsHelper,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val idTokenProvider: IdTokenProvider,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
) : ViewModel() {

    private val promocodeId = PromocodeId(promoCodeIdString)

    @AssistedFactory
    interface Factory {
        fun create(promoCodeId: String): PromocodeDetailViewModel
    }

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
            is PromocodeDetailAction.VoteClicked -> {
                handleVote(action.voteState)
            }
            is PromocodeDetailAction.ShareClicked -> handleShare()
            is PromocodeDetailAction.BackClicked -> handleBack()
            is PromocodeDetailAction.SignInWithGoogleClicked -> signInWithGoogle(action.context)
            is PromocodeDetailAction.DismissAuthSheet -> dismissAuthSheet()
            is PromocodeDetailAction.RetryClicked -> refreshPromocode()
        }
    }

    private fun computeVoteCounts(
        previousVote: VoteState,
        newVote: VoteState,
        currentUpvotes: Int,
        currentDownvotes: Int
    ): Pair<Int, Int> =
        when (previousVote to newVote) {
            VoteState.NONE to VoteState.UPVOTE -> (currentUpvotes + 1) to currentDownvotes
            VoteState.NONE to VoteState.DOWNVOTE -> currentUpvotes to (currentDownvotes + 1)
            VoteState.UPVOTE to VoteState.NONE -> (currentUpvotes - 1).coerceAtLeast(0) to currentDownvotes
            VoteState.DOWNVOTE to VoteState.NONE -> currentUpvotes to (currentDownvotes - 1).coerceAtLeast(0)
            VoteState.UPVOTE to VoteState.DOWNVOTE -> (currentUpvotes - 1).coerceAtLeast(0) to (currentDownvotes + 1)
            VoteState.DOWNVOTE to VoteState.UPVOTE -> (currentUpvotes + 1) to (currentDownvotes - 1).coerceAtLeast(0)
            else -> currentUpvotes to currentDownvotes
        }

    private suspend fun loadPromocode(promocodeId: PromocodeId) {
        _uiState.update { it.copy(promocodeState = PromocodeUiState.Loading) }
        when (val result = getPromocodeUseCase(promocodeId)) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        promocodeState = PromocodeUiState.Success(result.data),
                        optimisticUpvotes = null,
                        optimisticDownvotes = null,
                    )
                }
            }
            is Result.Error -> {
                _uiState.update { it.copy(promocodeState = PromocodeUiState.Error(result.error)) }
            }
        }
    }

    private fun loadUserInteraction(
        promocodeId: PromocodeId,
        userId: UserId
    ) {
        viewModelScope.launch {
            when (
                val result = getUserInteractionUseCase(
                    itemId = promocodeId.value,
                    userId = userId,
                )
            ) {
                is Result.Success -> {
                    _uiState.update { it.copy(userInteraction = result.data) }
                }
                is Result.Error -> {
                    _events.emit(PromocodeDetailEvent.ShowError(result.error))
                }
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
            getAuthStateUseCase()
                .collectLatest { authState ->
                    when (authState) {
                        is AuthState.Authenticated -> {
                            _uiState.update { it.copy(userId = authState.userId) }
                            loadUserInteraction(promocodeId, authState.userId)
                        }
                        AuthState.Unauthenticated -> _uiState.update { it.copy(userId = null) }
                    }
                }
        }
    }

    private fun handleVote(targetVoteState: VoteState) {
        viewModelScope.launch {
            // Auth check
            val authPrompt = when (targetVoteState) {
                VoteState.UPVOTE -> AuthPromptAction.UpvotePrompt
                else -> AuthPromptAction.DownvotePrompt
            }
            _uiState.value.userId ?: run {
                showAuthBottomSheet(authPrompt)
                return@launch
            }

            // Voting
            val currentVoteState = _uiState.value.userInteraction?.voteState ?: run {
                // Interaction not loaded yet; skip vote until user data arrives
                return@launch
            }
            _uiState.update { it.copy(currentVoting = targetVoteState) }
            when (
                val result = toggleVoteUseCase(
                    itemId = promocodeId.value,
                    itemType = ContentType.PROMO_CODE,
                    userId = _uiState.value.userId,
                    currentVoteState = currentVoteState,
                    targetVoteState = targetVoteState,
                )
            ) {
                is Result.Success -> {
                    val newInteraction = result.data
                    val promoState = _uiState.value.promocodeState
                    val promo = (promoState as? PromocodeUiState.Success)?.data
                    if (promo != null) {
                        val baseUpvotes = _uiState.value.optimisticUpvotes ?: promo.upvotes
                        val baseDownvotes = _uiState.value.optimisticDownvotes ?: promo.downvotes
                        val (newUpvotes, newDownvotes) = computeVoteCounts(
                            previousVote = currentVoteState,
                            newVote = newInteraction.voteState,
                            currentUpvotes = baseUpvotes,
                            currentDownvotes = baseDownvotes,
                        )
                        _uiState.update {
                            it.copy(
                                userInteraction = newInteraction,
                                optimisticUpvotes = newUpvotes,
                                optimisticDownvotes = newDownvotes,
                            )
                        }
                    } else {
                        _uiState.update { it.copy(userInteraction = newInteraction) }
                    }
                }
                is Result.Error -> {
                    _events.emit(PromocodeDetailEvent.ShowError(result.error))
                }
            }
            _uiState.update { it.copy(currentVoting = null) }
        }
    }

    private fun handleShare() {
        val promoState = _uiState.value.promocodeState
        if (promoState !is PromocodeUiState.Success) return
        val currentPromoCode = promoState.data
        _uiState.update { it.copy(isSharing = true) }

        viewModelScope.launch {
            _events.emit(PromocodeDetailEvent.SharePromocode(currentPromoCode))
            _uiState.update { it.copy(isSharing = false) }
        }
    }

    private fun handleBack() {
        viewModelScope.launch {
            _events.emit(PromocodeDetailEvent.NavigateBack)
        }
    }

    private fun signInWithGoogle(context: Context) {
        val currentAuthSheet = _uiState.value.authBottomSheet ?: return
        _uiState.update { it.copy(authBottomSheet = currentAuthSheet.copy(isLoading = true)) }

        viewModelScope.launch {
            when (val tokenResult = idTokenProvider.getIdToken(context)) {
                is Result.Success -> {
                    when (val signInResult = signInWithGoogleUseCase(tokenResult.data)) {
                        is Result.Success -> {
                            _uiState.update { it.copy(authBottomSheet = null) }
                            // Screen auto-reloads with user data via auth state listener in init
                        }
                        is Result.Error -> {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    authBottomSheet = currentAuthSheet.copy(isLoading = false),
                                )
                            }
                            _events.emit(PromocodeDetailEvent.ShowError(signInResult.error))
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            authBottomSheet = currentAuthSheet.copy(isLoading = false),
                        )
                    }
                    _events.emit(PromocodeDetailEvent.ShowError(tokenResult.error))
                }
            }
        }
    }

    private fun dismissAuthSheet() {
        _uiState.update { it.copy(authBottomSheet = null) }
    }

    private fun showAuthBottomSheet(action: AuthPromptAction) {
        _uiState.update { it.copy(authBottomSheet = AuthBottomSheetState(action)) }
    }
}
