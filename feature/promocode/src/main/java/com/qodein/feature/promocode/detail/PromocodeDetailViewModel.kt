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
import com.qodein.shared.domain.usecase.interaction.ToggleBookmarkUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleVoteUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodeByIdUseCase
import com.qodein.shared.domain.userIdOrNull
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.PromoCodeWithUserState
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
import com.qodein.shared.model.VoteState
import com.qodein.shared.presentation.interaction.InteractionStateHandler
import com.qodein.shared.presentation.interaction.VoteUpdate
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = PromocodeDetailViewModel.Factory::class)
class PromocodeDetailViewModel @AssistedInject constructor(
    @Assisted promoCodeIdString: String,
    private val getPromoCodeByIdUseCase: GetPromocodeByIdUseCase,
    private val getUserInteractionUseCase: GetUserInteractionUseCase,
    private val toggleVoteUseCase: ToggleVoteUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val analyticsHelper: AnalyticsHelper,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val idTokenProvider: IdTokenProvider,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
) : ViewModel() {

    private val promoCodeId = PromocodeId(promoCodeIdString)

    @AssistedFactory
    interface Factory {
        fun create(promoCodeId: String): PromocodeDetailViewModel
    }

    private val _uiState = MutableStateFlow(PromocodeDetailUiState(promoCodeId = promoCodeId))
    val uiState: StateFlow<PromocodeDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PromocodeDetailEvent>()
    val events = _events.asSharedFlow()

    companion object {
        private const val VOTE_ANIMATION_DURATION = 800L
        private const val COPY_FEEDBACK_DURATION = 1500L
    }

    init {
        // Load promocode on creation and reload when auth state changes
        viewModelScope.launch {
            getAuthStateUseCase()
                .distinctUntilChanged()
                .collect { authState ->
                    Logger.d("PromocodeDetailViewModel") {
                        "Auth state changed to ${authState::class.simpleName}, loading promocode"
                    }
                    loadPromocode(promoCodeId, authState)
                }
        }
    }

    fun onAction(action: PromocodeDetailAction) {
        when (action) {
            is PromocodeDetailAction.RefreshData -> reloadPromocode()
            is PromocodeDetailAction.UpvoteClicked -> handleVote(VoteState.UPVOTE, AuthPromptAction.UpvotePrompt)
            is PromocodeDetailAction.DownvoteClicked -> handleVote(VoteState.DOWNVOTE, AuthPromptAction.DownvotePrompt)
            is PromocodeDetailAction.CopyCodeClicked -> handleCopyCode()
            is PromocodeDetailAction.ShareClicked -> handleShare()
            is PromocodeDetailAction.BookmarkToggleClicked -> handleBookmarkToggle()
            is PromocodeDetailAction.CommentsClicked -> handleCommentsClick()
            is PromocodeDetailAction.FollowServiceClicked -> handleFollowService()
            is PromocodeDetailAction.BackClicked -> handleBack()
            is PromocodeDetailAction.ServiceClicked -> handleServiceClick()
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

        val userId = authState.userIdOrNull

        // Load data in parallel
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

                    val promoCodeWithUserState = PromoCodeWithUserState(
                        promoCode = promoCode,
                        userInteraction = userInteraction,
                    )
                    showPromoCodeWithUserState(promoCodeWithUserState)
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

    private fun showPromoCodeWithUserState(promoCodeWithUserState: PromoCodeWithUserState) {
        _uiState.update { currentState ->
            currentState.copy(
                promoCodeWithUserState = promoCodeWithUserState,
                isLoading = false,
                errorType = null,
            )
        }
    }

    private fun reloadPromocode() {
        viewModelScope.launch {
            val authState = getAuthStateUseCase().first()
            loadPromocode(promoCodeId, authState)
        }
    }

    private fun handleVote(
        targetVoteState: VoteState,
        authPromptAction: AuthPromptAction
    ) {
        viewModelScope.launch {
            val authState = requireAuth(authPromptAction) ?: return@launch
            val promoCodeWithUserState = _uiState.value.promoCodeWithUserState ?: return@launch
            val currentPromoCode = promoCodeWithUserState.promoCode
            val currentUserState = promoCodeWithUserState.userInteraction
            val originalPromoCodeWithUserState = promoCodeWithUserState

            // Get current vote state
            val currentVoteState = currentUserState?.voteState ?: VoteState.NONE

            // Compute optimistic updates using handler
            val voteUpdate = InteractionStateHandler.computeVoteUpdate(
                currentUpvotes = currentPromoCode.upvotes,
                currentDownvotes = currentPromoCode.downvotes,
                currentVoteState = currentVoteState,
                targetVoteState = targetVoteState,
            )

//            val updatedPromoCode = currentPromoCode.copy(
//                upvotes = voteUpdate.newUpvotes,
//                downvotes = voteUpdate.newDownvotes,
//            )

            val updatedInteraction = InteractionStateHandler.createOrUpdateVoteInteraction(
                currentInteraction = currentUserState,
                newVoteState = voteUpdate.newVoteState,
                contentId = currentPromoCode.id.value,
                contentType = ContentType.PROMO_CODE,
                userId = authState.user.id,
            )

            // Apply optimistic update to UI
            _uiState.update { currentState ->
                currentState.copy(
                    promoCodeWithUserState = PromoCodeWithUserState(
                        promoCode = currentPromoCode,
                        userInteraction = updatedInteraction,
                    ),
                    showVoteAnimation = true,
                    lastVoteType = voteUpdate.newVoteState,
                )
            }

            // Execute vote operation
            val result = executeVoteOperation(
                currentVoteState = currentVoteState,
                targetVoteState = targetVoteState,
                contentId = currentPromoCode.id.value,
                userId = authState.user.id,
            )

            when (result) {
                is Result.Success -> handleVoteSuccess(
                    updatedInteraction = result.data,
                    voteUpdate = voteUpdate,
                    targetVoteState = targetVoteState,
                    contentId = currentPromoCode.id.value,
                )
                is Result.Error -> handleInteractionError(
                    error = result.error,
                    originalState = originalPromoCodeWithUserState,
                )
            }
        }
    }

    private fun handleBookmarkToggle() {
        viewModelScope.launch {
            val authState = requireAuth(AuthPromptAction.BookmarkPromoCode) ?: return@launch
            val promoCodeWithUserState = _uiState.value.promoCodeWithUserState ?: return@launch
            val currentPromoCode = promoCodeWithUserState.promoCode
            val currentUserState = promoCodeWithUserState.userInteraction
            val originalPromoCodeWithUserState = promoCodeWithUserState

            // Compute optimistic bookmark update using handler
            val updatedInteraction = InteractionStateHandler.createOrUpdateBookmarkInteraction(
                currentInteraction = currentUserState,
                contentId = currentPromoCode.id.value,
                contentType = ContentType.PROMO_CODE,
                userId = authState.user.id,
            )

            // Apply optimistic update to UI
            _uiState.update { currentState ->
                currentState.copy(
                    promoCodeWithUserState = PromoCodeWithUserState(
                        promoCode = currentPromoCode,
                        userInteraction = updatedInteraction,
                    ),
                )
            }

            val result = toggleBookmarkUseCase(
                itemId = currentPromoCode.id.value,
                itemType = ContentType.PROMO_CODE,
                userId = authState.user.id,
            )

            when (result) {
                is Result.Success -> {
                    _uiState.value.promoCodeWithUserState?.let { currentState ->
                        _uiState.update {
                            it.copy(
                                promoCodeWithUserState = currentState.copy(userInteraction = result.data),
                            )
                        }
                    }
                }
                is Result.Error -> handleInteractionError(
                    error = result.error,
                    originalState = originalPromoCodeWithUserState,
                )
            }
        }
    }

    private fun handleCopyCode() {
        val currentPromoCode = _uiState.value.promoCodeWithUserState?.promoCode
        if (currentPromoCode != null) {
            _uiState.update { it.copy(isCopying = true) }

            viewModelScope.launch {
                _events.emit(PromocodeDetailEvent.CopyCodeToClipboard(currentPromoCode.code.value))

                delay(COPY_FEEDBACK_DURATION)
                _uiState.update { it.copy(isCopying = false) }
            }
        }
    }

    private fun handleShare() {
        val currentPromoCode = _uiState.value.promoCodeWithUserState?.promoCode
        if (currentPromoCode != null) {
            _uiState.update { it.copy(isSharing = true) }

            viewModelScope.launch {
                _events.emit(PromocodeDetailEvent.SharePromocode(currentPromoCode))
                _uiState.update { it.copy(isSharing = false) }
            }
        }
    }

    private fun handleCommentsClick() {
        val currentPromoCode = _uiState.value.promoCodeWithUserState?.promoCode
        if (currentPromoCode != null) {
            viewModelScope.launch {
                _events.emit(PromocodeDetailEvent.NavigateToComments(currentPromoCode.id))
            }
        }
    }

    private fun handleFollowService() {
        // TODO: Implement service following
        _uiState.update { it.copy(isFollowingService = !it.isFollowingService) }
    }

    private fun handleBack() {
        viewModelScope.launch {
            _events.emit(PromocodeDetailEvent.NavigateBack)
        }
    }

    private fun handleServiceClick() {
        val currentPromoCode = _uiState.value.promoCodeWithUserState?.promoCode
        if (currentPromoCode != null) {
            viewModelScope.launch {
                _events.emit(PromocodeDetailEvent.NavigateToService(currentPromoCode.serviceName))
            }
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

    private suspend fun requireAuth(action: AuthPromptAction): AuthState.Authenticated? {
        val authState = getAuthStateUseCase().first()
        return if (authState is AuthState.Authenticated) {
            authState
        } else {
            showAuthBottomSheet(action)
            null
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
                itemType = ContentType.PROMO_CODE,
                userId = userId,
                currentVoteState = currentVoteState,
            )
            VoteState.DOWNVOTE -> toggleVoteUseCase.toggleDownvote(
                itemId = contentId,
                itemType = ContentType.PROMO_CODE,
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
        val updatedPromoCodeWithUserState = _uiState.value.promoCodeWithUserState?.copy(
            userInteraction = updatedInteraction,
        )
        if (updatedPromoCodeWithUserState != null) {
            val newVoteState = updatedInteraction.voteState
            _uiState.update { currentState ->
                currentState.copy(
                    promoCodeWithUserState = updatedPromoCodeWithUserState,
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

            // Hide vote animation after delay
            delay(VOTE_ANIMATION_DURATION)
            _uiState.update { it.copy(showVoteAnimation = false, lastVoteType = null) }
        }
    }

    private suspend fun handleInteractionError(
        error: OperationError,
        originalState: PromoCodeWithUserState?
    ) {
        // Rollback optimistic update
        _uiState.update {
            it.copy(
                promoCodeWithUserState = originalState,
                showVoteAnimation = false,
                lastVoteType = null,
            )
        }

        // Show transient error (doesn't block UI)
        _events.emit(PromocodeDetailEvent.ShowError(error))
    }
}
