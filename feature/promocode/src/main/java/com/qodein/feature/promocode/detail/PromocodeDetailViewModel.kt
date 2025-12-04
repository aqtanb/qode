package com.qodein.feature.promocode.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logVote
import com.qodein.core.ui.auth.IdTokenProvider
import com.qodein.core.ui.component.AuthPromptAction
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import com.qodein.shared.domain.usecase.interaction.GetUserInteractionUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleVoteUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodeByIdUseCase
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.PromocodeInteraction
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
import com.qodein.shared.model.VoteState
import com.qodein.shared.presentation.interaction.VoteUpdate
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = PromocodeDetailViewModel.Factory::class)
class PromocodeDetailViewModel @AssistedInject constructor(
    @Assisted promoCodeIdString: String,
    private val getPromoCodeByIdUseCase: GetPromocodeByIdUseCase,
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
        viewModelScope.launch {
            getAuthStateUseCase()
                .collectLatest { authState ->
                    when (authState) {
                        is AuthState.Authenticated -> _uiState.update { it.copy(userId = authState.userId) }
                        AuthState.Unauthenticated -> _uiState.update { it.copy(userId = null) }
                    }
                    loadPromocode(promocodeId, authState)
                }
        }
    }

    fun onAction(action: PromocodeDetailAction) {
        when (action) {
            is PromocodeDetailAction.RefreshData -> reloadPromocode()
            is PromocodeDetailAction.UpvoteClicked -> {
                val userId = _uiState.value.userId
                if (userId == null) {
                    showAuthBottomSheet(AuthPromptAction.UpvotePrompt)
                } else {
                    handleVote(VoteState.UPVOTE)
                }
            }
            is PromocodeDetailAction.DownvoteClicked -> {
                val userId = _uiState.value.userId
                if (userId == null) {
                    showAuthBottomSheet(AuthPromptAction.UpvotePrompt)
                } else {
                    handleVote(VoteState.DOWNVOTE)
                }
            }
            is PromocodeDetailAction.CopyCodeClicked -> handleCopyCode()
            is PromocodeDetailAction.ShareClicked -> handleShare()
            is PromocodeDetailAction.BackClicked -> handleBack()
            is PromocodeDetailAction.SignInWithGoogleClicked -> signInWithGoogle(action.context)
            is PromocodeDetailAction.DismissAuthSheet -> dismissAuthSheet()
            is PromocodeDetailAction.RetryClicked -> reloadPromocode()
            is PromocodeDetailAction.ErrorDismissed -> dismissError()
        }
    }

    private suspend fun loadPromocode(
        promoCodeId: PromocodeId,
        authState: AuthState
    ) {
        _uiState.update { it.copy(isLoading = true, errorType = null) }

        val userId = when (authState) {
            is AuthState.Authenticated -> authState.userId
            AuthState.Unauthenticated -> null
        }

        coroutineScope {
            val promoCodeDeferred = async {
                getPromoCodeByIdUseCase(promoCodeId)
            }

            val userInteractionDeferred = async {
                if (userId != null) {
                    getUserInteractionUseCase(promoCodeId.value, userId)
                } else {
                    Result.Success(null)
                }
            }

            val promoCodeResult = promoCodeDeferred.await()
            val userInteractionResult = userInteractionDeferred.await()

            when (promoCodeResult) {
                is Result.Success -> {
                    val promoCode = promoCodeResult.data

                    // Get user interaction data if available
                    val userInteraction = when (userInteractionResult) {
                        is Result.Success -> userInteractionResult.data
                        is Result.Error -> {
                            _events.emit(PromocodeDetailEvent.ShowError(userInteractionResult.error))
                            null
                        }
                    }

                    val promocodeInteraction = PromocodeInteraction(
                        promocode = promoCode,
                        userInteraction = userInteraction,
                    )
                    showPromoCodeWithUserState(promocodeInteraction)
                }

                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorType = promoCodeResult.error,
                        )
                    }
                }
            }
        }
    }

    private fun showPromoCodeWithUserState(promocodeInteraction: PromocodeInteraction) {
        _uiState.update { currentState ->
            currentState.copy(
                promocodeInteraction = promocodeInteraction,
                isLoading = false,
                errorType = null,
            )
        }
    }

    private fun reloadPromocode() {
        viewModelScope.launch {
            val authState = getAuthStateUseCase().first()
            loadPromocode(promocodeId, authState)
        }
    }

    private fun handleVote(targetVoteState: VoteState) {
        viewModelScope.launch {
            val currentVoteState = _uiState.value.voteState
            if (targetVoteState == VoteState.UPVOTE) {
                toggleVoteUseCase.toggleUpvote(
                    itemId = uiState.value.promocodeId.value,
                    itemType = ContentType.PROMOCODE,
                    userId = uiState.value.userId ?: return@launch,
                    currentVoteState = currentVoteState ?: return@launch,
                )
            } else {
                toggleVoteUseCase.toggleDownvote(
                    itemId = uiState.value.promocodeId.value,
                    itemType = ContentType.PROMOCODE,
                    userId = uiState.value.userId ?: return@launch,
                    currentVoteState = currentVoteState ?: return@launch,
                )
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

    private fun requireAuth(action: AuthPromptAction) {
        viewModelScope.launch {
            val authState = getAuthStateUseCase().first()
            if (authState !is AuthState.Authenticated) {
                showAuthBottomSheet(action)
            }
        }
    }

    private suspend fun executeVoteOperation(
        currentVoteState: VoteState,
        targetVoteState: VoteState,
        contentId: String,
        userId: UserId
    ): Result<UserInteraction, OperationError> =
        when (targetVoteState) {
            VoteState.UPVOTE -> toggleVoteUseCase.toggleUpvote(
                itemId = contentId,
                itemType = ContentType.PROMOCODE,
                userId = userId,
                currentVoteState = currentVoteState,
            )
            VoteState.DOWNVOTE -> toggleVoteUseCase.toggleDownvote(
                itemId = contentId,
                itemType = ContentType.PROMOCODE,
                userId = userId,
                currentVoteState = currentVoteState,
            )
            VoteState.NONE -> {
                Logger.e("PromocodeDetailViewModel") { "Invalid target vote state: NONE" }
                Result.Error(SystemError.Unknown)
            }
        }

    private fun computeAnalyticsVoteType(
        newVoteState: VoteState,
        targetVoteState: VoteState
    ): String =
        when {
            newVoteState == VoteState.NONE && targetVoteState == VoteState.UPVOTE -> "remove_upvote"
            newVoteState == VoteState.NONE && targetVoteState == VoteState.DOWNVOTE -> "remove_downvote"
            newVoteState == VoteState.UPVOTE -> "upvote"
            newVoteState == VoteState.DOWNVOTE -> "downvote"
            else -> "unknown"
        }

    private suspend fun handleVoteSuccess(
        updatedInteraction: UserInteraction,
        voteUpdate: VoteUpdate,
        targetVoteState: VoteState,
        contentId: String
    ) {
        val updatedPromoCodeWithUserState = _uiState.value.promocodeInteraction?.copy(
            userInteraction = updatedInteraction,
        )
        if (updatedPromoCodeWithUserState != null) {
            val newVoteState = updatedInteraction.voteState
            _uiState.update { currentState ->
                currentState.copy(
                    promocodeInteraction = updatedPromoCodeWithUserState,
                    showVoteAnimation = true,
                    lastVoteType = newVoteState,
                )
            }

            // Log analytics
            val voteTypeForAnalytics = computeAnalyticsVoteType(voteUpdate.newVoteState, targetVoteState)
            analyticsHelper.logVote(
                promocodeId = contentId,
                voteType = voteTypeForAnalytics,
            )

            _uiState.update { it.copy(showVoteAnimation = false, lastVoteType = null) }
        }
    }

    private suspend fun handleInteractionError(
        error: OperationError,
        originalState: PromocodeInteraction?
    ) {
        // Rollback optimistic update
        _uiState.update {
            it.copy(
                promocodeInteraction = originalState,
                showVoteAnimation = false,
                lastVoteType = null,
            )
        }

        // Show transient error (doesn't block UI)
        _events.emit(PromocodeDetailEvent.ShowError(error))
    }
}
