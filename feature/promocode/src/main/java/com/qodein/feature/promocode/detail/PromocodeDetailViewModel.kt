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

    private val _interactionState = MutableStateFlow<InteractionUiState>(InteractionUiState.None)
    val interactionState: StateFlow<InteractionUiState> = _interactionState.asStateFlow()

    private val _promocodeUiState = MutableStateFlow<PromocodeUiState>(PromocodeUiState.Loading)
    val promocodeUiState: StateFlow<PromocodeUiState> = _promocodeUiState.asStateFlow()

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
            is PromocodeDetailAction.CopyCodeClicked -> handleCopyCode()
            is PromocodeDetailAction.ShareClicked -> handleShare()
            is PromocodeDetailAction.BackClicked -> handleBack()
            is PromocodeDetailAction.SignInWithGoogleClicked -> signInWithGoogle(action.context)
            is PromocodeDetailAction.DismissAuthSheet -> dismissAuthSheet()
            is PromocodeDetailAction.RetryClicked -> refreshPromocode()
            is PromocodeDetailAction.ErrorDismissed -> dismissError()
        }
    }

    private suspend fun loadPromocode(promocodeId: PromocodeId) {
        _promocodeUiState.value = PromocodeUiState.Loading
        when (val result = getPromocodeUseCase(promocodeId)) {
            is Result.Success -> {
                _promocodeUiState.value = PromocodeUiState.Success(result.data)
            }
            is Result.Error -> {
                _promocodeUiState.value = PromocodeUiState.Error(result.error)
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
                    val interaction = result.data
                    if (interaction != null) {
                        _interactionState.value = InteractionUiState.Success(interaction)
                    } else {
                        _interactionState.value = InteractionUiState.None
                    }
                }
                is Result.Error -> {
                    _interactionState.value = InteractionUiState.Error(result.error)
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
            val currentVoteState = when (val state = _interactionState.value) {
                is InteractionUiState.Success -> state.interaction.voteState
                else -> return@launch
            }
            _interactionState.value = InteractionUiState.Loading
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
                    _interactionState.value = InteractionUiState.Success(result.data)
                }
                is Result.Error -> {
                    _interactionState.value = InteractionUiState.Error(result.error)
                }
            }
        }
    }

    private fun handleCopyCode() {
        val currentPromoCode = _uiState.value.promocodeInteraction?.promocode
        if (currentPromoCode != null) {
            _uiState.update { it.copy(isCopying = true) }

            viewModelScope.launch {
                _events.emit(PromocodeDetailEvent.CopyCodeToClipboard(currentPromoCode.code.value))

                _uiState.update { it.copy(isCopying = false) }
            }
        }
    }

    private fun handleShare() {
        val currentPromoCode = _uiState.value.promocodeInteraction?.promocode
        if (currentPromoCode != null) {
            _uiState.update { it.copy(isSharing = true) }

            viewModelScope.launch {
                _events.emit(PromocodeDetailEvent.SharePromocode(currentPromoCode))
                _uiState.update { it.copy(isSharing = false) }
            }
        }
    }

    private fun handleBack() {
        viewModelScope.launch {
            _events.emit(PromocodeDetailEvent.NavigateBack)
        }
    }

    private fun signInWithGoogle(context: Context) {
        val currentAuthSheet = _uiState.value.authBottomSheet
        if (currentAuthSheet != null) {
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
    }

    private fun dismissAuthSheet() {
        _uiState.update { it.copy(authBottomSheet = null) }
    }

    private fun dismissError() {
        _uiState.update { it.copy(errorType = null) }
    }

    private fun showAuthBottomSheet(action: AuthPromptAction) {
        _uiState.update { it.copy(authBottomSheet = AuthBottomSheetState(action)) }
    }
}
