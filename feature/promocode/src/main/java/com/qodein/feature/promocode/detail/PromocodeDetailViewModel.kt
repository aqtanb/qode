package com.qodein.feature.promocode.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.analytics.AnalyticsEvent
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
import com.qodein.shared.domain.usecase.promocode.GetPromoCodeByIdUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodeVoteUseCase
import com.qodein.shared.domain.usecase.promocode.VoteOnPromocodeUseCase
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.UserId
import com.qodein.shared.model.Vote
import com.qodein.shared.model.VoteState
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val getPromoCodeVoteUseCase: GetPromocodeVoteUseCase,
    private val voteOnPromoCodeUseCase: VoteOnPromocodeUseCase,
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
                getPromoCodeByIdUseCase(promoCodeId).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val promoCode = result.data
                            if (promoCode != null) {
                                // Load vote state if user is authenticated
                                if (userId != null) {
                                    loadUserVoteStateForUser(promoCode, userId)
                                } else {
                                    showPromoCodeWithoutVoteState(promoCode)
                                }
                            } else {
                                _uiState.update { currentState ->
                                    currentState.copy(
                                        promoCode = null,
                                        isLoading = false,
                                        errorType = null,
                                        isBookmarked = false,
                                    )
                                }
                            }
                        }
                        is Result.Loading -> {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = true,
                                    errorType = null,
                                )
                            }
                        }
                        is Result.Error -> {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    errorType = result.exception.toErrorType(),
                                )
                            }
                            Logger.e("PromocodeDetailViewModel") {
                                "Failed to load promocode: ${result.exception.message}"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorType = e.toErrorType(),
                    )
                }
                Logger.e("PromocodeDetailViewModel") { "Unexpected error: ${e.message}" }
            }
        }
    }

    private suspend fun loadUserVoteStateForUser(
        promoCode: PromoCode,
        userId: UserId
    ) {
        try {
            val voteResult = getPromoCodeVoteUseCase(promoCode.id, userId).first()
            when (voteResult) {
                is Result.Success -> {
                    val userVote = voteResult.data
                    val updatedPromoCode = updatePromoCodeWithVoteState(promoCode, userVote)

                    _uiState.update { currentState ->
                        currentState.copy(
                            promoCode = updatedPromoCode,
                            isLoading = false,
                            errorType = null,
                            isBookmarked = updatedPromoCode.isBookmarkedByCurrentUser,
                        )
                    }

                    trackPromoCodeView(updatedPromoCode)
                }
                is Result.Loading -> {
                    showPromoCodeWithoutVoteState(promoCode)
                }
                is Result.Error -> {
                    Logger.w("PromocodeDetailViewModel") {
                        "Failed to load user vote: ${voteResult.exception.message}"
                    }
                    showPromoCodeWithoutVoteState(promoCode)
                }
            }
        } catch (e: Exception) {
            Logger.w("PromocodeDetailViewModel") { "Exception loading user vote: ${e.message}" }
            showPromoCodeWithoutVoteState(promoCode)
        }
    }

    private fun showPromoCodeWithoutVoteState(promoCode: PromoCode) {
        _uiState.update { currentState ->
            currentState.copy(
                promoCode = promoCode,
                isLoading = false,
                errorType = null,
                isBookmarked = promoCode.isBookmarkedByCurrentUser,
            )
        }
        trackPromoCodeView(promoCode)
    }

    private fun updatePromoCodeWithVoteState(
        promoCode: PromoCode,
        userVote: Vote?
    ): PromoCode {
        val isUpvoted = userVote?.voteState == VoteState.UPVOTE
        val isDownvoted = userVote?.voteState == VoteState.DOWNVOTE

        return when (promoCode) {
            is PromoCode.PercentagePromoCode -> promoCode.copy(
                isUpvotedByCurrentUser = isUpvoted,
                isDownvotedByCurrentUser = isDownvoted,
            )
            is PromoCode.FixedAmountPromoCode -> promoCode.copy(
                isUpvotedByCurrentUser = isUpvoted,
                isDownvotedByCurrentUser = isDownvoted,
            )
        }
    }

    private fun trackPromoCodeView(promoCode: PromoCode) {
        val promoType = when (promoCode) {
            is PromoCode.PercentagePromoCode -> "percentage"
            is PromoCode.FixedAmountPromoCode -> "fixed_amount"
        }
        analyticsHelper.logPromoCodeView(promoCode.id.value, promoType)
    }

    private fun refreshCurrentPromocode() {
        val currentPromoCode = _uiState.value.promoCode
        currentPromoCode?.let {
            loadPromocode(it.id)
        }
    }

    // Handle upvote action with 3-state logic
    private fun handleUpvote() {
        viewModelScope.launch {
            when (val authState = authStateManager.getAuthState().first()) {
                is AuthState.Authenticated -> {
                    val promoCode = _uiState.value.promoCode ?: return@launch
                    val currentlyUpvoted = promoCode.isUpvotedByCurrentUser
                    val targetState = if (currentlyUpvoted) VoteState.NONE else VoteState.UPVOTE
                    handleAuthenticatedVote(targetState, authState.user.id)
                }
                is AuthState.Unauthenticated -> {
                    _uiState.update {
                        it.copy(authBottomSheet = AuthBottomSheetState(action = AuthPromptAction.UpvotePromoCode))
                    }
                }
                is AuthState.Loading -> {
                    _events.emit(PromocodeDetailEvent.ShowSnackbar("Please wait..."))
                }
            }
        }
    }

    // Handle downvote action with 3-state logic
    private fun handleDownvote() {
        viewModelScope.launch {
            when (val authState = authStateManager.getAuthState().first()) {
                is AuthState.Authenticated -> {
                    val promoCode = _uiState.value.promoCode ?: return@launch
                    val currentlyDownvoted = promoCode.isDownvotedByCurrentUser
                    val targetState = if (currentlyDownvoted) VoteState.NONE else VoteState.DOWNVOTE
                    handleAuthenticatedVote(targetState, authState.user.id)
                }
                is AuthState.Unauthenticated -> {
                    _uiState.update {
                        it.copy(authBottomSheet = AuthBottomSheetState(action = AuthPromptAction.DownvotePromoCode))
                    }
                }
                is AuthState.Loading -> {
                    _events.emit(PromocodeDetailEvent.ShowSnackbar("Please wait..."))
                }
            }
        }
    }

    // Authenticated vote handling
    private fun handleAuthenticatedVote(
        targetVoteState: VoteState,
        userId: UserId
    ) {
        val currentState = _uiState.value
        val promoCode = currentState.promoCode ?: return

        if (currentState.isVoting) return

        val actionDescription = when (targetVoteState) {
            VoteState.UPVOTE -> "upvote"
            VoteState.DOWNVOTE -> "downvote"
            VoteState.NONE -> "remove vote"
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isVoting = true,
                    showVoteAnimation = true,
                    lastVoteType = targetVoteState,
                )
            }

            // Optimistic update
            val optimisticPromoCode = updatePromoCodeOptimistically(promoCode, targetVoteState)
            _uiState.update { it.copy(promoCode = optimisticPromoCode) }

            try {
                val result = voteOnPromoCodeUseCase(promoCode.id, userId, targetVoteState)
                when (result) {
                    is Result.Loading -> {
                        // Loading state handled by isVoting flag
                    }
                    is Result.Success -> {
                        // Refresh to get actual server state
                        loadPromocodeWithUser(promoCode.id, userId)

                        _events.emit(PromocodeDetailEvent.ShowVoteFeedback(targetVoteState == VoteState.UPVOTE))

                        // Track analytics
                        val analyticsVoteType = when (targetVoteState) {
                            VoteState.UPVOTE -> "upvote"
                            VoteState.DOWNVOTE -> "downvote"
                            VoteState.NONE -> "remove_vote"
                        }
                        analyticsHelper.logVote(promoCode.id.value, analyticsVoteType)

                        Logger.d("PromocodeDetailViewModel") { "Successfully $actionDescription" }
                    }
                    is Result.Error -> {
                        // Rollback optimistic update
                        _uiState.update { it.copy(promoCode = promoCode) }

                        val errorMessage = when {
                            result.exception.message?.contains("network") == true -> {
                                "Network error. Please check your connection"
                            }
                            else -> "Failed to $actionDescription. Please try again"
                        }

                        _events.emit(PromocodeDetailEvent.ShowSnackbar(errorMessage))
                        Logger.e("PromocodeDetailViewModel") {
                            "Failed to $actionDescription: ${result.exception.message}"
                        }
                    }
                }
            } catch (e: Exception) {
                // Rollback optimistic update
                _uiState.update { it.copy(promoCode = promoCode) }
                _events.emit(
                    PromocodeDetailEvent.ShowSnackbar(
                        "Voting failed. Please check your connection.",
                    ),
                )
                Logger.e("PromocodeDetailViewModel") { "Vote error: ${e.message}" }
            } finally {
                _uiState.update { it.copy(isVoting = false) }
            }

            // Hide vote animation after delay
            delay(VOTE_ANIMATION_DURATION)
            _uiState.update { it.copy(showVoteAnimation = false, lastVoteType = null) }
        }
    }

    private fun updatePromoCodeOptimistically(
        promoCode: PromoCode,
        targetVoteState: VoteState
    ): PromoCode {
        val currentlyUpvoted = promoCode.isUpvotedByCurrentUser
        val currentlyDownvoted = promoCode.isDownvotedByCurrentUser

        return when (promoCode) {
            is PromoCode.PercentagePromoCode -> {
                when (targetVoteState) {
                    VoteState.UPVOTE -> {
                        if (currentlyDownvoted) {
                            // Switch from downvote to upvote
                            promoCode.copy(
                                upvotes = promoCode.upvotes + 1,
                                downvotes = (promoCode.downvotes - 1).coerceAtLeast(0),
                                isUpvotedByCurrentUser = true,
                                isDownvotedByCurrentUser = false,
                            )
                        } else {
                            // Add upvote (from none)
                            promoCode.copy(
                                upvotes = promoCode.upvotes + 1,
                                isUpvotedByCurrentUser = true,
                                isDownvotedByCurrentUser = false,
                            )
                        }
                    }
                    VoteState.DOWNVOTE -> {
                        if (currentlyUpvoted) {
                            // Switch from upvote to downvote
                            promoCode.copy(
                                upvotes = (promoCode.upvotes - 1).coerceAtLeast(0),
                                downvotes = promoCode.downvotes + 1,
                                isUpvotedByCurrentUser = false,
                                isDownvotedByCurrentUser = true,
                            )
                        } else {
                            // Add downvote (from none)
                            promoCode.copy(
                                downvotes = promoCode.downvotes + 1,
                                isUpvotedByCurrentUser = false,
                                isDownvotedByCurrentUser = true,
                            )
                        }
                    }
                    VoteState.NONE -> {
                        // Remove current vote
                        if (currentlyUpvoted) {
                            promoCode.copy(
                                upvotes = (promoCode.upvotes - 1).coerceAtLeast(0),
                                isUpvotedByCurrentUser = false,
                                isDownvotedByCurrentUser = false,
                            )
                        } else if (currentlyDownvoted) {
                            promoCode.copy(
                                downvotes = (promoCode.downvotes - 1).coerceAtLeast(0),
                                isUpvotedByCurrentUser = false,
                                isDownvotedByCurrentUser = false,
                            )
                        } else {
                            promoCode // No change if already NONE
                        }
                    }
                }
            }
            is PromoCode.FixedAmountPromoCode -> {
                when (targetVoteState) {
                    VoteState.UPVOTE -> {
                        if (currentlyDownvoted) {
                            // Switch from downvote to upvote
                            promoCode.copy(
                                upvotes = promoCode.upvotes + 1,
                                downvotes = (promoCode.downvotes - 1).coerceAtLeast(0),
                                isUpvotedByCurrentUser = true,
                                isDownvotedByCurrentUser = false,
                            )
                        } else {
                            // Add upvote (from none)
                            promoCode.copy(
                                upvotes = promoCode.upvotes + 1,
                                isUpvotedByCurrentUser = true,
                                isDownvotedByCurrentUser = false,
                            )
                        }
                    }
                    VoteState.DOWNVOTE -> {
                        if (currentlyUpvoted) {
                            // Switch from upvote to downvote
                            promoCode.copy(
                                upvotes = (promoCode.upvotes - 1).coerceAtLeast(0),
                                downvotes = promoCode.downvotes + 1,
                                isUpvotedByCurrentUser = false,
                                isDownvotedByCurrentUser = true,
                            )
                        } else {
                            // Add downvote (from none)
                            promoCode.copy(
                                downvotes = promoCode.downvotes + 1,
                                isUpvotedByCurrentUser = false,
                                isDownvotedByCurrentUser = true,
                            )
                        }
                    }
                    VoteState.NONE -> {
                        // Remove current vote
                        if (currentlyUpvoted) {
                            promoCode.copy(
                                upvotes = (promoCode.upvotes - 1).coerceAtLeast(0),
                                isUpvotedByCurrentUser = false,
                                isDownvotedByCurrentUser = false,
                            )
                        } else if (currentlyDownvoted) {
                            promoCode.copy(
                                downvotes = (promoCode.downvotes - 1).coerceAtLeast(0),
                                isUpvotedByCurrentUser = false,
                                isDownvotedByCurrentUser = false,
                            )
                        } else {
                            promoCode // No change if already NONE
                        }
                    }
                }
            }
        }
    }

    private fun handleCopyCode() {
        val promoCode = _uiState.value.promoCode ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isCopying = true) }

            _events.emit(PromocodeDetailEvent.CopyCodeToClipboard(promoCode.code))
            _events.emit(PromocodeDetailEvent.ShowSnackbar("Code copied to clipboard!"))

            val promoType = when (promoCode) {
                is PromoCode.PercentagePromoCode -> "percentage"
                is PromoCode.FixedAmountPromoCode -> "fixed_amount"
            }
            analyticsHelper.logCopyPromoCode(promoCode.id.value, promoType)

            delay(COPY_FEEDBACK_DURATION)
            _uiState.update { it.copy(isCopying = false) }
        }
    }

    private fun handleShare() {
        val promoCode = _uiState.value.promoCode ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSharing = true) }

            _events.emit(PromocodeDetailEvent.SharePromocode(promoCode))

            analyticsHelper.logEvent(
                AnalyticsEvent(
                    type = "share_promocode",
                    extras = listOf(
                        AnalyticsEvent.Param("promocode_id", promoCode.id.value),
                        AnalyticsEvent.Param("service_name", promoCode.serviceName),
                        AnalyticsEvent.Param(
                            "discount_type",
                            when (promoCode) {
                                is PromoCode.PercentagePromoCode -> "percentage"
                                is PromoCode.FixedAmountPromoCode -> "fixed_amount"
                            },
                        ),
                    ),
                ),
            )

            delay(1000)
            _uiState.update { it.copy(isSharing = false) }
        }
    }

    private fun handleBookmarkToggle() {
        val currentBookmarkState = _uiState.value.isBookmarked
        _uiState.update { it.copy(isBookmarked = !currentBookmarkState) }

        viewModelScope.launch {
            val message = if (!currentBookmarkState) "Bookmarked!" else "Bookmark removed"
            _events.emit(PromocodeDetailEvent.ShowSnackbar(message))
        }
    }

    private fun handleCommentsClick() {
        val promoCode = _uiState.value.promoCode ?: return

        viewModelScope.launch {
            _events.emit(PromocodeDetailEvent.NavigateToComments(promoCode.id))
        }
    }

    private fun handleFollowService() {
        val promoCode = _uiState.value.promoCode ?: return

        viewModelScope.launch {
            _events.emit(PromocodeDetailEvent.ShowFollowServiceTodo(promoCode.serviceName))
        }
    }

    private fun handleFollowCategory() {
        val promoCode = _uiState.value.promoCode ?: return
        val category = promoCode.category ?: return

        viewModelScope.launch {
            _events.emit(PromocodeDetailEvent.ShowFollowCategoryTodo(category))
        }
    }

    private fun handleBack() {
        viewModelScope.launch {
            _events.emit(PromocodeDetailEvent.NavigateBack)
        }
    }

    private fun handleServiceClick() {
        val promoCode = _uiState.value.promoCode ?: return

        viewModelScope.launch {
            _events.emit(PromocodeDetailEvent.NavigateToService(promoCode.serviceName))
        }
    }

    private fun handleRetry() {
        val errorType = _uiState.value.errorType
        if (errorType != null && isErrorTypeRetryable(errorType)) {
            refreshCurrentPromocode()
        }
    }

    private fun isErrorTypeRetryable(errorType: ErrorType): Boolean =
        when (errorType) {
            ErrorType.NETWORK_TIMEOUT,
            ErrorType.NETWORK_NO_CONNECTION,
            ErrorType.NETWORK_HOST_UNREACHABLE,
            ErrorType.NETWORK_GENERAL,
            ErrorType.SERVICE_UNAVAILABLE_GENERAL,
            ErrorType.SERVICE_CONFIGURATION_ERROR,
            ErrorType.UNKNOWN_ERROR -> true
            else -> false
        }

    private fun handleSignInWithGoogle() {
        val currentAuthSheet = _uiState.value.authBottomSheet ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(authBottomSheet = currentAuthSheet.copy(isLoading = true))
            }

            try {
                signInWithGoogleUseCase().collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            // Loading handled by isLoading flag
                        }
                        is Result.Success -> {
                            // Sign-in successful - dismiss auth sheet
                            _uiState.update { it.copy(authBottomSheet = null) }

                            // Refresh current promocode with user data
                            val promoCode = _uiState.value.promoCode
                            if (promoCode != null) {
                                loadPromocodeWithUser(promoCode.id, result.data.id)
                            }

                            Logger.d("PromocodeDetailViewModel") { "Sign-in successful" }
                        }
                        is Result.Error -> {
                            // Sign-in failed - show error but keep sheet open
                            _uiState.update {
                                it.copy(authBottomSheet = currentAuthSheet.copy(isLoading = false))
                            }

                            val errorMessage = when {
                                result.exception.message?.contains("cancelled") == true -> {
                                    "Sign-in was cancelled"
                                }
                                result.exception.message?.contains("network") == true -> {
                                    "Network error. Please check your connection"
                                }
                                else -> "Sign-in failed. Please try again"
                            }

                            _events.emit(PromocodeDetailEvent.ShowSnackbar(errorMessage))
                            Logger.e("PromocodeDetailViewModel") {
                                "Sign-in failed: ${result.exception.message}"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(authBottomSheet = currentAuthSheet.copy(isLoading = false))
                }
                _events.emit(PromocodeDetailEvent.ShowSnackbar("Sign-in failed. Please try again."))
                Logger.e("PromocodeDetailViewModel") { "Sign-in error: ${e.message}" }
            }
        }
    }

    private fun dismissAuthSheet() {
        _uiState.update { it.copy(authBottomSheet = null) }
    }

    private fun dismissError() {
        _uiState.update { it.copy(errorType = null) }
    }
}
