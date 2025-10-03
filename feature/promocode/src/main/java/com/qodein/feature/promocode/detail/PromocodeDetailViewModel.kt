package com.qodein.feature.promocode.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logCopyPromoCode
import com.qodein.core.analytics.logPromoCodeView
import com.qodein.core.analytics.logVote
import com.qodein.core.ui.component.AuthPromptAction
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.auth.AuthStateManager
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import com.qodein.shared.domain.usecase.interaction.GetUserInteractionUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleBookmarkUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleVoteUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodeByIdUseCase
import com.qodein.shared.domain.userIdOrNull
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.PromoCodeWithUserState
import com.qodein.shared.model.VoteState
import com.qodein.shared.presentation.interaction.InteractionStateHandler
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
    @Assisted private val promoCodeId: PromoCodeId,
    private val getPromoCodeByIdUseCase: GetPromocodeByIdUseCase,
    private val getUserInteractionUseCase: GetUserInteractionUseCase,
    private val toggleVoteUseCase: ToggleVoteUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val analyticsHelper: AnalyticsHelper,
    private val authStateManager: AuthStateManager,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(promoCodeId: PromoCodeId): PromocodeDetailViewModel
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
            authStateManager.getAuthState()
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
            is PromocodeDetailAction.UpvoteClicked -> handleVote(VoteState.UPVOTE, AuthPromptAction.UpvotePromoCode)
            is PromocodeDetailAction.DownvoteClicked -> handleVote(VoteState.DOWNVOTE, AuthPromptAction.DownvotePromoCode)
            is PromocodeDetailAction.CopyCodeClicked -> handleCopyCode()
            is PromocodeDetailAction.ShareClicked -> handleShare()
            is PromocodeDetailAction.BookmarkToggleClicked -> handleBookmarkToggle()
            is PromocodeDetailAction.CommentsClicked -> handleCommentsClick()
            is PromocodeDetailAction.FollowServiceClicked -> handleFollowService()
            is PromocodeDetailAction.BackClicked -> handleBack()
            is PromocodeDetailAction.ServiceClicked -> handleServiceClick()
            is PromocodeDetailAction.SignInWithGoogleClicked -> handleSignInWithGoogle()
            is PromocodeDetailAction.DismissAuthSheet -> dismissAuthSheet()
            is PromocodeDetailAction.RetryClicked -> reloadPromocode()
            is PromocodeDetailAction.ErrorDismissed -> dismissError()
        }
    }

    private suspend fun loadPromocode(
        promoCodeId: PromoCodeId,
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
                            Logger.w("PromocodeDetailViewModel") {
                                "Failed to load user interaction: ${userInteractionResult.error}"
                            }
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
                    Logger.e("PromocodeDetailViewModel") { "Error loading promo code: ${promoCodeResult.error}" }
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

        // Log promo code view for analytics
        analyticsHelper.logPromoCodeView(
            promocodeId = promoCodeWithUserState.promoCode.id.value,
            promocodeType = when (promoCodeWithUserState.promoCode) {
                is PromoCode.PercentagePromoCode -> "percentage"
                is PromoCode.FixedAmountPromoCode -> "fixed_amount"
                else -> "unknown"
            },
        )
    }

    private fun reloadPromocode() {
        viewModelScope.launch {
            val authState = authStateManager.getAuthState().first()
            loadPromocode(promoCodeId, authState)
        }
    }

    private fun handleVote(
        targetVoteState: VoteState,
        authPromptAction: AuthPromptAction
    ) {
        viewModelScope.launch {
            val authState = requireAuth(authPromptAction) ?: return@launch
            val currentPromoCode = _uiState.value.promoCodeWithUserState?.promoCode ?: return@launch
            val currentUserState = _uiState.value.promoCodeWithUserState?.userInteraction

            // Save original state for rollback on error
            val originalPromoCodeWithUserState = _uiState.value.promoCodeWithUserState

            // Compute optimistic updates using handler
            val voteUpdate = InteractionStateHandler.computeVoteUpdate(
                currentUpvotes = currentPromoCode.upvotes,
                currentDownvotes = currentPromoCode.downvotes,
                currentVoteState = currentUserState?.voteState ?: VoteState.NONE,
                targetVoteState = targetVoteState,
            )

            val updatedPromoCode = when (currentPromoCode) {
                is PromoCode.PercentagePromoCode -> currentPromoCode.copy(
                    upvotes = voteUpdate.newUpvotes,
                    downvotes = voteUpdate.newDownvotes,
                )
                is PromoCode.FixedAmountPromoCode -> currentPromoCode.copy(
                    upvotes = voteUpdate.newUpvotes,
                    downvotes = voteUpdate.newDownvotes,
                )
            }

            val updatedInteraction = InteractionStateHandler.createOrUpdateVoteInteraction(
                currentInteraction = currentUserState,
                newVoteState = voteUpdate.newVoteState,
                contentId = currentPromoCode.id.value,
                contentType = ContentType.PROMO_CODE,
                userId = authState.user.id,
            )

            val optimisticState = PromoCodeWithUserState(
                promoCode = updatedPromoCode,
                userInteraction = updatedInteraction,
            )

            // Apply optimistic update to UI
            _uiState.update { currentState ->
                currentState.copy(
                    promoCodeWithUserState = optimisticState,
                    showVoteAnimation = true,
                    lastVoteType = voteUpdate.newVoteState,
                )
            }

            // Execute vote operation
            val result = if (voteUpdate.newVoteState == VoteState.NONE) {
                // Removing vote
                toggleVoteUseCase.removeVote(
                    itemId = currentPromoCode.id.value,
                    itemType = ContentType.PROMO_CODE,
                    userId = authState.user.id,
                )
            } else {
                // Adding/switching vote
                when (targetVoteState) {
                    VoteState.UPVOTE -> toggleVoteUseCase.toggleUpvote(
                        itemId = currentPromoCode.id.value,
                        itemType = ContentType.PROMO_CODE,
                        userId = authState.user.id,
                    )
                    VoteState.DOWNVOTE -> toggleVoteUseCase.toggleDownvote(
                        itemId = currentPromoCode.id.value,
                        itemType = ContentType.PROMO_CODE,
                        userId = authState.user.id,
                    )
                    VoteState.NONE -> {
                        Logger.e("PromocodeDetailViewModel") { "Invalid vote state: NONE" }
                        _events.emit(PromocodeDetailEvent.ShowError(SystemError.Unknown))
                        return@launch
                    }
                }
            }

            when (result) {
                is Result.Success -> {
                    val updatedInteraction = result.data
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
                        val voteTypeForAnalytics = when {
                            voteUpdate.newVoteState == VoteState.NONE && targetVoteState == VoteState.UPVOTE -> "remove_upvote"
                            voteUpdate.newVoteState == VoteState.NONE && targetVoteState == VoteState.DOWNVOTE -> "remove_downvote"
                            voteUpdate.newVoteState == VoteState.UPVOTE -> "upvote"
                            voteUpdate.newVoteState == VoteState.DOWNVOTE -> "downvote"
                            else -> "unknown"
                        }
                        analyticsHelper.logVote(
                            promocodeId = currentPromoCode.id.value,
                            voteType = voteTypeForAnalytics,
                        )

                        // Hide vote animation after delay
                        viewModelScope.launch {
                            delay(VOTE_ANIMATION_DURATION)
                            _uiState.update { it.copy(showVoteAnimation = false, lastVoteType = null) }
                        }
                    }
                }
                is Result.Error -> {
                    Logger.e("PromocodeDetailViewModel") { "Error voting: ${result.error}" }

                    // Rollback optimistic update
                    _uiState.update {
                        it.copy(
                            promoCodeWithUserState = originalPromoCodeWithUserState,
                            showVoteAnimation = false,
                            lastVoteType = null,
                        )
                    }

                    // Show transient error (doesn't block UI)
                    _events.emit(PromocodeDetailEvent.ShowError(result.error))
                }
            }
        }
    }

    private fun handleBookmarkToggle() {
        viewModelScope.launch {
            val authState = requireAuth(AuthPromptAction.BookmarkPromoCode) ?: return@launch
            val currentPromoCode = _uiState.value.promoCodeWithUserState?.promoCode ?: return@launch
            val currentUserState = _uiState.value.promoCodeWithUserState?.userInteraction

            // Save original state for rollback on error
            val originalPromoCodeWithUserState = _uiState.value.promoCodeWithUserState

            // Compute optimistic bookmark update using handler
            val updatedInteraction = InteractionStateHandler.createOrUpdateBookmarkInteraction(
                currentInteraction = currentUserState,
                contentId = currentPromoCode.id.value,
                contentType = ContentType.PROMO_CODE,
                userId = authState.user.id,
            )

            val optimisticState = PromoCodeWithUserState(
                promoCode = currentPromoCode,
                userInteraction = updatedInteraction,
            )

            // Apply optimistic update to UI
            _uiState.update { currentState ->
                currentState.copy(
                    promoCodeWithUserState = optimisticState,
                )
            }

            val result = toggleBookmarkUseCase(
                itemId = currentPromoCode.id.value,
                itemType = ContentType.PROMO_CODE,
                userId = authState.user.id,
            )

            when (result) {
                is Result.Success -> {
                    val updatedInteraction = result.data
                    val updatedPromoCodeWithUserState = _uiState.value.promoCodeWithUserState?.copy(
                        userInteraction = updatedInteraction,
                    )
                    if (updatedPromoCodeWithUserState != null) {
                        _uiState.update { currentState ->
                            currentState.copy(
                                promoCodeWithUserState = updatedPromoCodeWithUserState,
                            )
                        }
                    }
                }
                is Result.Error -> {
                    Logger.e("PromocodeDetailViewModel") { "Error toggling bookmark: ${result.error}" }

                    // Rollback optimistic update
                    _uiState.update {
                        it.copy(promoCodeWithUserState = originalPromoCodeWithUserState)
                    }

                    // Show transient error (doesn't block UI)
                    _events.emit(PromocodeDetailEvent.ShowError(result.error))
                }
            }
        }
    }

    private fun handleCopyCode() {
        val currentPromoCode = _uiState.value.promoCodeWithUserState?.promoCode
        if (currentPromoCode != null) {
            _uiState.update { it.copy(isCopying = true) }

            viewModelScope.launch {
                _events.emit(PromocodeDetailEvent.CopyCodeToClipboard(currentPromoCode.code))

                // Log analytics
                analyticsHelper.logCopyPromoCode(
                    promocodeId = currentPromoCode.id.value,
                    promocodeType = when (currentPromoCode) {
                        is PromoCode.PercentagePromoCode -> "percentage"
                        is PromoCode.FixedAmountPromoCode -> "fixed_amount"
                        else -> "unknown"
                    },
                )

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

    private fun handleSignInWithGoogle() {
        val currentAuthSheet = _uiState.value.authBottomSheet
        if (currentAuthSheet != null) {
            _uiState.update { it.copy(authBottomSheet = currentAuthSheet.copy(isLoading = true)) }

            viewModelScope.launch {
                signInWithGoogleUseCase().collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.update { it.copy(authBottomSheet = null) }
                            // Screen auto-reloads with user data via auth state listener in init
                        }
                        is Result.Error -> {
                            Logger.e("PromocodeDetailViewModel") { "Sign in error: ${result.error}" }
                            _uiState.update { currentState ->
                                currentState.copy(
                                    authBottomSheet = currentAuthSheet.copy(isLoading = false),
                                )
                            }
                            _events.emit(PromocodeDetailEvent.ShowError(result.error))
                        }
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
        val authState = authStateManager.getAuthState().first()
        return if (authState is AuthState.Authenticated) {
            authState
        } else {
            showAuthBottomSheet(action)
            null
        }
    }
}
