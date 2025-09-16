package com.qodein.feature.promocode.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logCopyPromoCode
import com.qodein.core.analytics.logPromoCodeView
import com.qodein.core.analytics.logVote
import com.qodein.core.ui.component.AuthPromptAction
import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.toErrorType
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.auth.AuthStateManager
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import com.qodein.shared.domain.usecase.interaction.GetUserInteractionUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleBookmarkUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleVoteUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromoCodeByIdUseCase
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.PromoCodeWithUserState
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PromocodeDetailViewModel @Inject constructor(
    private val getPromoCodeByIdUseCase: GetPromoCodeByIdUseCase,
    private val getUserInteractionUseCase: GetUserInteractionUseCase,
    private val toggleVoteUseCase: ToggleVoteUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val analyticsHelper: AnalyticsHelper,
    private val authStateManager: AuthStateManager,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PromocodeDetailUiState())
    val uiState: StateFlow<PromocodeDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PromocodeDetailEvent>()
    val events = _events.asSharedFlow()

    companion object {
        private const val VOTE_ANIMATION_DURATION = 800L
        private const val COPY_FEEDBACK_DURATION = 1500L
    }

    fun onAction(action: PromocodeDetailAction) {
        when (action) {
            is PromocodeDetailAction.LoadPromocode -> loadPromocode(action.promoCodeId)
            is PromocodeDetailAction.LoadPromocodeWithUser -> loadPromocodeWithUser(action.promoCodeId, action.userId)
            is PromocodeDetailAction.RefreshData -> refreshCurrentPromocode()
            is PromocodeDetailAction.UpvoteClicked -> handleUpvote()
            is PromocodeDetailAction.DownvoteClicked -> handleDownvote()
            is PromocodeDetailAction.CopyCodeClicked -> handleCopyCode()
            is PromocodeDetailAction.ShareClicked -> handleShare()
            is PromocodeDetailAction.BookmarkToggleClicked -> handleBookmarkToggle()
            is PromocodeDetailAction.CommentsClicked -> handleCommentsClick()
            is PromocodeDetailAction.FollowServiceClicked -> handleFollowService()
            is PromocodeDetailAction.BackClicked -> handleBack()
            is PromocodeDetailAction.ServiceClicked -> handleServiceClick()
            is PromocodeDetailAction.SignInWithGoogleClicked -> handleSignInWithGoogle()
            is PromocodeDetailAction.DismissAuthSheet -> dismissAuthSheet()
            is PromocodeDetailAction.RetryClicked -> handleRetry()
            is PromocodeDetailAction.ErrorDismissed -> dismissError()
        }
    }

    private fun loadPromocode(promoCodeId: PromoCodeId) {
        loadPromocodeWithUser(promoCodeId, null)
    }

    private fun loadPromocodeWithUser(
        promoCodeId: PromoCodeId,
        userId: UserId?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorType = null) }

            try {
                // Get current auth state to determine if we need user interaction data
                val authState = authStateManager.getAuthState().first()
                val effectiveUserId = userId ?: (authState as? AuthState.Authenticated)?.user?.id

                coroutineScope {
                    // Load promo code data (always needed)
                    val promoCodeDeferred = async {
                        var result: Result<PromoCode?> = Result.Loading
                        try {
                            getPromoCodeByIdUseCase(promoCodeId).collect {
                                result = it
                            }
                            result
                        } catch (e: Exception) {
                            Logger.e("PromocodeDetailViewModel") {
                                "Failed to load promo code: $e"
                            }
                            Result.Error(e)
                        }
                    }

                    // Load user interaction data (only if user is authenticated)
                    val userInteractionDeferred = async {
                        if (effectiveUserId != null) {
                            try {
                                getUserInteractionUseCase(promoCodeId.value, effectiveUserId)
                            } catch (e: Exception) {
                                Logger.w("PromocodeDetailViewModel") {
                                    "Failed to load user interaction, using defaults: $e"
                                }
                                Result.Success(null)
                            }
                        } else {
                            Result.Success(null)
                        }
                    }

                    // Wait for both results
                    val promoCodeResult = promoCodeDeferred.await()
                    val userInteractionResult = userInteractionDeferred.await()

                    when (promoCodeResult) {
                        is Result.Success -> {
                            val promoCode = promoCodeResult.data
                            if (promoCode != null) {
                                // Get user interaction data if available
                                val userInteraction = when (userInteractionResult) {
                                    is Result.Success -> userInteractionResult.data
                                    is Result.Error -> {
                                        Logger.w("PromocodeDetailViewModel") {
                                            "Failed to load user interaction: ${userInteractionResult.exception}"
                                        }
                                        null
                                    }
                                    is Result.Loading -> null
                                }

                                val promoCodeWithUserState = PromoCodeWithUserState(
                                    promoCode = promoCode,
                                    userInteraction = userInteraction,
                                )
                                showPromoCodeWithUserState(promoCodeWithUserState)
                            } else {
                                _uiState.update { currentState ->
                                    currentState.copy(
                                        promoCodeWithUserState = null,
                                        isLoading = false,
                                        errorType = ErrorType.NETWORK_GENERAL,
                                    )
                                }
                            }
                        }
                        is Result.Error -> {
                            Logger.e("PromocodeDetailViewModel") { "Error loading promo code: ${promoCodeResult.exception}" }
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    errorType = promoCodeResult.exception.toErrorType(),
                                )
                            }
                        }
                        is Result.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e("PromocodeDetailViewModel") { "Unexpected error: $e" }
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorType = e.toErrorType(),
                    )
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
                isBookmarked = promoCodeWithUserState.isBookmarkedByCurrentUser,
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

    private fun refreshCurrentPromocode() {
        val currentPromoCode = _uiState.value.promoCodeWithUserState?.promoCode
        if (currentPromoCode != null) {
            loadPromocode(currentPromoCode.id)
        }
    }

    private fun handleUpvote() {
        viewModelScope.launch {
            val authState = authStateManager.getAuthState().first()
            val currentPromoCode = _uiState.value.promoCodeWithUserState?.promoCode

            if (authState !is AuthState.Authenticated) {
                showAuthBottomSheet(AuthPromptAction.UpvotePromoCode)
                return@launch
            }

            if (currentPromoCode == null) return@launch

            val currentUserState = _uiState.value.promoCodeWithUserState?.userInteraction
            val isCurrentlyUpvoted = currentUserState?.voteState == VoteState.UPVOTE

            // Optimistic UI update - immediately update vote counts and user state
            val currentPromoCodeWithUserState = _uiState.value.promoCodeWithUserState
            if (currentPromoCodeWithUserState != null) {
                val updatedPromoCode = if (isCurrentlyUpvoted) {
                    // Remove upvote: decrease upvote count
                    when (currentPromoCode) {
                        is PromoCode.PercentagePromoCode -> currentPromoCode.copy(
                            upvotes = maxOf(0, currentPromoCode.upvotes - 1),
                        )
                        is PromoCode.FixedAmountPromoCode -> currentPromoCode.copy(
                            upvotes = maxOf(0, currentPromoCode.upvotes - 1),
                        )
                    }
                } else {
                    // Add upvote: increase upvote count, maybe decrease downvote if switching
                    val upvoteDelta = 1
                    val downvoteDelta = if (currentUserState?.voteState == VoteState.DOWNVOTE) -1 else 0
                    when (currentPromoCode) {
                        is PromoCode.PercentagePromoCode -> currentPromoCode.copy(
                            upvotes = currentPromoCode.upvotes + upvoteDelta,
                            downvotes = maxOf(0, currentPromoCode.downvotes + downvoteDelta),
                        )
                        is PromoCode.FixedAmountPromoCode -> currentPromoCode.copy(
                            upvotes = currentPromoCode.upvotes + upvoteDelta,
                            downvotes = maxOf(0, currentPromoCode.downvotes + downvoteDelta),
                        )
                    }
                }

                // Update user interaction optimistically
                val newVoteState = if (isCurrentlyUpvoted) VoteState.NONE else VoteState.UPVOTE
                val updatedUserInteraction = currentUserState?.copy(voteState = newVoteState)
                    ?: com.qodein.shared.model.UserInteraction.create(
                        itemId = currentPromoCode.id.value,
                        itemType = ContentType.PROMO_CODE,
                        userId = authState.user.id,
                        voteState = newVoteState,
                    )

                val optimisticState = PromoCodeWithUserState(
                    promoCode = updatedPromoCode,
                    userInteraction = updatedUserInteraction,
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        promoCodeWithUserState = optimisticState,
                        isVoting = true,
                        showVoteAnimation = true,
                        lastVoteType = newVoteState,
                    )
                }
            }

            try {
                // Determine what action to take
                val result = if (isCurrentlyUpvoted) {
                    toggleVoteUseCase.removeVote(
                        itemId = currentPromoCode.id.value,
                        itemType = ContentType.PROMO_CODE,
                        userId = authState.user.id,
                    )
                } else {
                    toggleVoteUseCase.toggleUpvote(
                        itemId = currentPromoCode.id.value,
                        itemType = ContentType.PROMO_CODE,
                        userId = authState.user.id,
                    )
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
                                    isVoting = false,
                                    showVoteAnimation = true,
                                    lastVoteType = newVoteState,
                                )
                            }

                            // Log analytics
                            analyticsHelper.logVote(
                                promocodeId = currentPromoCode.id.value,
                                voteType = if (isCurrentlyUpvoted) "remove_upvote" else "upvote",
                            )

                            // Hide vote animation after delay
                            viewModelScope.launch {
                                delay(VOTE_ANIMATION_DURATION)
                                _uiState.update { it.copy(showVoteAnimation = false, lastVoteType = null) }
                            }
                        }
                    }
                    is Result.Error -> {
                        Logger.e("PromocodeDetailViewModel") { "Error voting: ${result.exception}" }
                        _uiState.update { currentState ->
                            currentState.copy(
                                isVoting = false,
                                errorType = result.exception.toErrorType(),
                            )
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isVoting = true) }
                    }
                }
            } catch (e: Exception) {
                Logger.e("PromocodeDetailViewModel") { "Unexpected error during upvote: $e" }
                _uiState.update { currentState ->
                    currentState.copy(
                        isVoting = false,
                        errorType = e.toErrorType(),
                    )
                }
            }
        }
    }

    private fun handleDownvote() {
        viewModelScope.launch {
            val authState = authStateManager.getAuthState().first()
            val currentPromoCode = _uiState.value.promoCodeWithUserState?.promoCode

            if (authState !is AuthState.Authenticated) {
                showAuthBottomSheet(AuthPromptAction.DownvotePromoCode)
                return@launch
            }

            if (currentPromoCode == null) return@launch

            val currentUserState = _uiState.value.promoCodeWithUserState?.userInteraction
            val isCurrentlyDownvoted = currentUserState?.voteState == VoteState.DOWNVOTE

            // Optimistic UI update - immediately update vote counts and user state
            val currentPromoCodeWithUserState = _uiState.value.promoCodeWithUserState
            if (currentPromoCodeWithUserState != null) {
                val updatedPromoCode = if (isCurrentlyDownvoted) {
                    // Remove downvote: decrease downvote count
                    when (currentPromoCode) {
                        is PromoCode.PercentagePromoCode -> currentPromoCode.copy(
                            downvotes = maxOf(0, currentPromoCode.downvotes - 1),
                        )
                        is PromoCode.FixedAmountPromoCode -> currentPromoCode.copy(
                            downvotes = maxOf(0, currentPromoCode.downvotes - 1),
                        )
                    }
                } else {
                    // Add downvote: increase downvote count, maybe decrease upvote if switching
                    val downvoteDelta = 1
                    val upvoteDelta = if (currentUserState?.voteState == VoteState.UPVOTE) -1 else 0
                    when (currentPromoCode) {
                        is PromoCode.PercentagePromoCode -> currentPromoCode.copy(
                            downvotes = currentPromoCode.downvotes + downvoteDelta,
                            upvotes = maxOf(0, currentPromoCode.upvotes + upvoteDelta),
                        )
                        is PromoCode.FixedAmountPromoCode -> currentPromoCode.copy(
                            downvotes = currentPromoCode.downvotes + downvoteDelta,
                            upvotes = maxOf(0, currentPromoCode.upvotes + upvoteDelta),
                        )
                    }
                }

                // Update user interaction optimistically
                val newVoteState = if (isCurrentlyDownvoted) VoteState.NONE else VoteState.DOWNVOTE
                val updatedUserInteraction = currentUserState?.copy(voteState = newVoteState)
                    ?: com.qodein.shared.model.UserInteraction.create(
                        itemId = currentPromoCode.id.value,
                        itemType = ContentType.PROMO_CODE,
                        userId = authState.user.id,
                        voteState = newVoteState,
                    )

                val optimisticState = PromoCodeWithUserState(
                    promoCode = updatedPromoCode,
                    userInteraction = updatedUserInteraction,
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        promoCodeWithUserState = optimisticState,
                        isVoting = true,
                        showVoteAnimation = true,
                        lastVoteType = newVoteState,
                    )
                }
            }

            try {
                // Determine what action to take
                val result = if (isCurrentlyDownvoted) {
                    toggleVoteUseCase.removeVote(
                        itemId = currentPromoCode.id.value,
                        itemType = ContentType.PROMO_CODE,
                        userId = authState.user.id,
                    )
                } else {
                    toggleVoteUseCase.toggleDownvote(
                        itemId = currentPromoCode.id.value,
                        itemType = ContentType.PROMO_CODE,
                        userId = authState.user.id,
                    )
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
                                    isVoting = false,
                                    showVoteAnimation = true,
                                    lastVoteType = newVoteState,
                                )
                            }

                            // Log analytics
                            analyticsHelper.logVote(
                                promocodeId = currentPromoCode.id.value,
                                voteType = if (isCurrentlyDownvoted) "remove_downvote" else "downvote",
                            )

                            // Hide vote animation after delay
                            viewModelScope.launch {
                                delay(VOTE_ANIMATION_DURATION)
                                _uiState.update { it.copy(showVoteAnimation = false, lastVoteType = null) }
                            }
                        }
                    }
                    is Result.Error -> {
                        Logger.e("PromocodeDetailViewModel") { "Error voting: ${result.exception}" }
                        _uiState.update { currentState ->
                            currentState.copy(
                                isVoting = false,
                                errorType = result.exception.toErrorType(),
                            )
                        }
                    }
                    is Result.Loading -> {
                        _uiState.update { it.copy(isVoting = true) }
                    }
                }
            } catch (e: Exception) {
                Logger.e("PromocodeDetailViewModel") { "Unexpected error during downvote: $e" }
                _uiState.update { currentState ->
                    currentState.copy(
                        isVoting = false,
                        errorType = e.toErrorType(),
                    )
                }
            }
        }
    }

    private fun handleBookmarkToggle() {
        viewModelScope.launch {
            val authState = authStateManager.getAuthState().first()
            val currentPromoCode = _uiState.value.promoCodeWithUserState?.promoCode

            if (authState !is AuthState.Authenticated) {
                showAuthBottomSheet(AuthPromptAction.BookmarkPromoCode)
                return@launch
            }

            if (currentPromoCode == null) return@launch

            val currentIsBookmarked = _uiState.value.promoCodeWithUserState?.isBookmarkedByCurrentUser == true

            // Optimistic update
            val currentPromoCodeWithUserState = _uiState.value.promoCodeWithUserState
            if (currentPromoCodeWithUserState != null) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isBookmarked = !currentIsBookmarked,
                    )
                }
            }

            try {
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
                                    isBookmarked = updatedPromoCodeWithUserState.isBookmarkedByCurrentUser,
                                )
                            }
                        }
                    }
                    is Result.Error -> {
                        Logger.e("PromocodeDetailViewModel") { "Error toggling bookmark: ${result.exception}" }
                        // Revert optimistic update
                        _uiState.update { currentState ->
                            currentState.copy(
                                isBookmarked = currentIsBookmarked,
                                errorType = result.exception.toErrorType(),
                            )
                        }
                    }
                    is Result.Loading -> {
                        // Already handling loading state optimistically
                    }
                }
            } catch (e: Exception) {
                Logger.e("PromocodeDetailViewModel") { "Unexpected error during bookmark toggle: $e" }
                // Revert optimistic update
                _uiState.update { currentState ->
                    currentState.copy(
                        isBookmarked = currentIsBookmarked,
                        errorType = e.toErrorType(),
                    )
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

                            // Retry the original action
                            when (currentAuthSheet.action) {
                                AuthPromptAction.SubmitPromoCode -> {} // User will click submit again
                                AuthPromptAction.UpvotePromoCode -> {} // User will click upvote again
                                AuthPromptAction.DownvotePromoCode -> {} // User will click downvote again
                                AuthPromptAction.WriteComment -> {} // User will click comment again
                                AuthPromptAction.BookmarkPromoCode -> {} // User will click bookmark again
                                AuthPromptAction.FollowStore -> {} // User will click follow again
                            }
                        }
                        is Result.Error -> {
                            Logger.e("PromocodeDetailViewModel") { "Sign in error: ${result.exception}" }
                            _uiState.update { currentState ->
                                currentState.copy(
                                    authBottomSheet = currentAuthSheet.copy(isLoading = false),
                                    errorType = result.exception.toErrorType(),
                                )
                            }
                        }
                        is Result.Loading -> {
                            _uiState.update { it.copy(authBottomSheet = currentAuthSheet.copy(isLoading = true)) }
                        }
                    }
                }
            }
        }
    }

    private fun dismissAuthSheet() {
        _uiState.update { it.copy(authBottomSheet = null) }
    }

    private fun handleRetry() {
        val currentPromoCode = _uiState.value.promoCodeWithUserState?.promoCode
        if (currentPromoCode != null) {
            loadPromocode(currentPromoCode.id)
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(errorType = null) }
    }

    private fun showAuthBottomSheet(action: AuthPromptAction) {
        _uiState.update { it.copy(authBottomSheet = AuthBottomSheetState(action)) }
    }
}
