package com.qodein.feature.promocode.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logCopyPromoCode
import com.qodein.core.analytics.logPromoCodeView
import com.qodein.core.analytics.logVote
import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.toErrorType
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromoCodeByIdUseCase
import com.qodein.shared.domain.usecase.promocode.GetUserVoteUseCase
import com.qodein.shared.domain.usecase.promocode.VoteOnPromoCodeUseCase
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.UserId
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
import com.qodein.shared.domain.repository.VoteType as RepositoryVoteType

@HiltViewModel
class PromocodeDetailViewModel @Inject constructor(
    private val getPromoCodeByIdUseCase: GetPromoCodeByIdUseCase,
    private val getUserVoteUseCase: GetUserVoteUseCase,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val voteOnPromoCodeUseCase: VoteOnPromoCodeUseCase,
    private val analyticsHelper: AnalyticsHelper
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
            is PromocodeDetailAction.RefreshData -> refreshCurrentPromocode()
            is PromocodeDetailAction.UpvoteClicked -> handleVote(true)
            is PromocodeDetailAction.DownvoteClicked -> handleVote(false)
            is PromocodeDetailAction.CopyCodeClicked -> handleCopyCode()
            is PromocodeDetailAction.ShareClicked -> handleShare()
            is PromocodeDetailAction.BookmarkToggleClicked -> handleBookmarkToggle()
            is PromocodeDetailAction.CommentsClicked -> handleCommentsClick()
            is PromocodeDetailAction.FollowServiceClicked -> handleFollowService()
            is PromocodeDetailAction.FollowCategoryClicked -> handleFollowCategory()
            is PromocodeDetailAction.BackClicked -> handleBack()
            is PromocodeDetailAction.ServiceClicked -> handleServiceClick()
            is PromocodeDetailAction.RetryClicked -> handleRetry()
            is PromocodeDetailAction.ErrorDismissed -> dismissError()
        }
    }

    private fun loadPromocode(promoCodeId: PromoCodeId) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorType = null) }

            try {
                getPromoCodeByIdUseCase(promoCodeId).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val promoCode = result.data
                            if (promoCode != null) {
                                // Load vote state for authenticated users
                                loadUserVoteState(promoCode)
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

    private suspend fun loadUserVoteState(promoCode: PromoCode) {
        val userId = getCurrentUserId()
        if (userId != null) {
            // User is authenticated, load their vote state
            try {
                val voteResult = getUserVoteUseCase(promoCode.id, userId).first()
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

                        // Track analytics
                        trackPromoCodeView(updatedPromoCode)
                    }
                    is Result.Loading -> {
                        // If still loading, show promo code without vote state for now
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
                    is Result.Error -> {
                        // If vote loading fails, still show promo code without vote state
                        Logger.w("PromocodeDetailViewModel") {
                            "Failed to load user vote: ${voteResult.exception.message}"
                        }
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
                }
            } catch (e: Exception) {
                Logger.w("PromocodeDetailViewModel") { "Exception loading user vote: ${e.message}" }
                // Fallback to showing promo code without vote state
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
        } else {
            // User is not authenticated, show promo code with no vote state
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
    }

    private fun updatePromoCodeWithVoteState(
        promoCode: PromoCode,
        userVote: com.qodein.shared.model.PromoCodeVote?
    ): PromoCode {
        val isUpvoted = userVote?.isUpvote == true
        val isDownvoted = userVote?.isUpvote == false

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

    private suspend fun getCurrentUserId(): UserId? =
        try {
            val authState = getAuthStateUseCase().first()
            Logger.d("PromocodeDetailViewModel") { "Auth state: $authState" }
            when (authState) {
                is AuthState.Authenticated -> {
                    Logger.d("PromocodeDetailViewModel") { "User authenticated: ${authState.user.id.value}" }
                    authState.user.id
                }
                is AuthState.Unauthenticated -> {
                    Logger.d("PromocodeDetailViewModel") { "User not authenticated" }
                    null
                }
                AuthState.Loading -> {
                    Logger.d("PromocodeDetailViewModel") { "Auth state still loading, waiting..." }
                    // If auth is still loading, wait for a non-loading state
                    getAuthStateUseCase().first { it != AuthState.Loading }.let { finalState ->
                        Logger.d("PromocodeDetailViewModel") { "Final auth state after loading: $finalState" }
                        when (finalState) {
                            is AuthState.Authenticated -> finalState.user.id
                            is AuthState.Unauthenticated -> null
                            AuthState.Loading -> {
                                Logger.w("PromocodeDetailViewModel") { "Auth state still loading after wait" }
                                null
                            }
                            else -> {
                                Logger.w("PromocodeDetailViewModel") { "Unknown final auth state: $finalState" }
                                null
                            }
                        }
                    }
                }
                else -> {
                    Logger.w("PromocodeDetailViewModel") { "Unknown auth state: $authState" }
                    null
                }
            }
        } catch (e: Exception) {
            Logger.w("PromocodeDetailViewModel") { "Failed to get auth state: ${e.message}" }
            null
        }

    private fun handleVote(isUpvote: Boolean) {
        val currentState = _uiState.value
        val promoCode = currentState.promoCode ?: return

        if (currentState.isVoting) return

        // Determine what action to take based on current vote state
        val currentlyUpvoted = promoCode.isUpvotedByCurrentUser
        val currentlyDownvoted = promoCode.isDownvotedByCurrentUser

        // Convert Boolean UI input to RepositoryVoteType? for the use case
        val repositoryVoteType: RepositoryVoteType? = when {
            isUpvote && currentlyUpvoted -> null // Remove upvote
            isUpvote && currentlyDownvoted -> RepositoryVoteType.UPVOTE // Switch to upvote
            isUpvote && !currentlyUpvoted && !currentlyDownvoted -> RepositoryVoteType.UPVOTE // Add upvote
            !isUpvote && currentlyDownvoted -> null // Remove downvote
            !isUpvote && currentlyUpvoted -> RepositoryVoteType.DOWNVOTE // Switch to downvote
            !isUpvote && !currentlyUpvoted && !currentlyDownvoted -> RepositoryVoteType.DOWNVOTE // Add downvote
            else -> null
        }

        val actionDescription = when {
            isUpvote && currentlyUpvoted -> "remove upvote"
            isUpvote && currentlyDownvoted -> "switch to upvote"
            isUpvote && !currentlyUpvoted && !currentlyDownvoted -> "add upvote"
            !isUpvote && currentlyDownvoted -> "remove downvote"
            !isUpvote && currentlyUpvoted -> "switch to downvote"
            !isUpvote && !currentlyUpvoted && !currentlyDownvoted -> "add downvote"
            else -> "vote"
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isVoting = true,
                    showVoteAnimation = true,
                    lastVoteType = if (isUpvote) VoteType.UPVOTE else VoteType.DOWNVOTE,
                )
            }

            // Optimistically update UI state
            val optimisticPromoCode = updatePromoCodeOptimistically(promoCode, isUpvote)
            _uiState.update { it.copy(promoCode = optimisticPromoCode) }

            try {
                // Get authenticated user ID
                val userId = getCurrentUserId()
                if (userId == null) {
                    // User is not authenticated - this should not happen if auth integration is correct
                    _events.emit(PromocodeDetailEvent.ShowSnackbar("Please sign in to vote"))
                    _uiState.update { it.copy(promoCode = promoCode, isVoting = false) }
                    return@launch
                }

                voteOnPromoCodeUseCase(promoCode.id, userId, repositoryVoteType).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            // Refresh to get actual server state (in case of conflicts)
                            refreshCurrentPromocode()

                            _events.emit(PromocodeDetailEvent.ShowVoteFeedback(isUpvote))

                            // Track analytics
                            val analyticsVoteType = if (isUpvote) "upvote" else "downvote"
                            analyticsHelper.logVote(promoCode.id.value, analyticsVoteType)

                            Logger.d("PromocodeDetailViewModel") { "Successfully $actionDescription" }
                        }
                        is Result.Loading -> {
                            // Loading state already handled by isVoting flag
                        }
                        is Result.Error -> {
                            // Rollback optimistic update
                            _uiState.update { it.copy(promoCode = promoCode) }

                            val errorMessage = when {
                                result.exception.message?.contains("already voted") == true -> {
                                    "You have already voted on this promocode"
                                }
                                result.exception.message?.contains("network") == true -> {
                                    "Network error. Please check your connection"
                                }
                                else -> "Failed to vote. Please try again"
                            }

                            _events.emit(PromocodeDetailEvent.ShowSnackbar(errorMessage))
                            Logger.e("PromocodeDetailViewModel") {
                                "Failed to $actionDescription: ${result.exception.message}"
                            }
                        }
                    }
                    _uiState.update { it.copy(isVoting = false) }
                }
            } catch (e: Exception) {
                // Rollback optimistic update
                _uiState.update { it.copy(promoCode = promoCode, isVoting = false) }
                _events.emit(
                    PromocodeDetailEvent.ShowSnackbar(
                        "Voting failed. Please check your connection.",
                    ),
                )
                Logger.e("PromocodeDetailViewModel") { "Vote error: ${e.message}" }
            }

            // Hide vote animation after delay
            delay(VOTE_ANIMATION_DURATION)
            _uiState.update { it.copy(showVoteAnimation = false, lastVoteType = null) }
        }
    }

    private fun updatePromoCodeOptimistically(
        promoCode: PromoCode,
        isUpvote: Boolean
    ): PromoCode {
        val currentlyUpvoted = promoCode.isUpvotedByCurrentUser
        val currentlyDownvoted = promoCode.isDownvotedByCurrentUser

        return when (promoCode) {
            is PromoCode.PercentagePromoCode -> {
                when {
                    isUpvote && currentlyUpvoted -> {
                        // Remove upvote
                        promoCode.copy(
                            upvotes = (promoCode.upvotes - 1).coerceAtLeast(0),
                            isUpvotedByCurrentUser = false,
                            isDownvotedByCurrentUser = false,
                        )
                    }
                    isUpvote && currentlyDownvoted -> {
                        // Switch from downvote to upvote
                        promoCode.copy(
                            upvotes = promoCode.upvotes + 1,
                            downvotes = (promoCode.downvotes - 1).coerceAtLeast(0),
                            isUpvotedByCurrentUser = true,
                            isDownvotedByCurrentUser = false,
                        )
                    }
                    isUpvote && !currentlyUpvoted && !currentlyDownvoted -> {
                        // Add upvote
                        promoCode.copy(
                            upvotes = promoCode.upvotes + 1,
                            isUpvotedByCurrentUser = true,
                            isDownvotedByCurrentUser = false,
                        )
                    }
                    !isUpvote && currentlyDownvoted -> {
                        // Remove downvote
                        promoCode.copy(
                            downvotes = (promoCode.downvotes - 1).coerceAtLeast(0),
                            isUpvotedByCurrentUser = false,
                            isDownvotedByCurrentUser = false,
                        )
                    }
                    !isUpvote && currentlyUpvoted -> {
                        // Switch from upvote to downvote
                        promoCode.copy(
                            upvotes = (promoCode.upvotes - 1).coerceAtLeast(0),
                            downvotes = promoCode.downvotes + 1,
                            isUpvotedByCurrentUser = false,
                            isDownvotedByCurrentUser = true,
                        )
                    }
                    !isUpvote && !currentlyUpvoted && !currentlyDownvoted -> {
                        // Add downvote
                        promoCode.copy(
                            downvotes = promoCode.downvotes + 1,
                            isUpvotedByCurrentUser = false,
                            isDownvotedByCurrentUser = true,
                        )
                    }
                    else -> promoCode
                }
            }
            is PromoCode.FixedAmountPromoCode -> {
                when {
                    isUpvote && currentlyUpvoted -> {
                        // Remove upvote
                        promoCode.copy(
                            upvotes = (promoCode.upvotes - 1).coerceAtLeast(0),
                            isUpvotedByCurrentUser = false,
                            isDownvotedByCurrentUser = false,
                        )
                    }
                    isUpvote && currentlyDownvoted -> {
                        // Switch from downvote to upvote
                        promoCode.copy(
                            upvotes = promoCode.upvotes + 1,
                            downvotes = (promoCode.downvotes - 1).coerceAtLeast(0),
                            isUpvotedByCurrentUser = true,
                            isDownvotedByCurrentUser = false,
                        )
                    }
                    isUpvote && !currentlyUpvoted && !currentlyDownvoted -> {
                        // Add upvote
                        promoCode.copy(
                            upvotes = promoCode.upvotes + 1,
                            isUpvotedByCurrentUser = true,
                            isDownvotedByCurrentUser = false,
                        )
                    }
                    !isUpvote && currentlyDownvoted -> {
                        // Remove downvote
                        promoCode.copy(
                            downvotes = (promoCode.downvotes - 1).coerceAtLeast(0),
                            isUpvotedByCurrentUser = false,
                            isDownvotedByCurrentUser = false,
                        )
                    }
                    !isUpvote && currentlyUpvoted -> {
                        // Switch from upvote to downvote
                        promoCode.copy(
                            upvotes = (promoCode.upvotes - 1).coerceAtLeast(0),
                            downvotes = promoCode.downvotes + 1,
                            isUpvotedByCurrentUser = false,
                            isDownvotedByCurrentUser = true,
                        )
                    }
                    !isUpvote && !currentlyUpvoted && !currentlyDownvoted -> {
                        // Add downvote
                        promoCode.copy(
                            downvotes = promoCode.downvotes + 1,
                            isUpvotedByCurrentUser = false,
                            isDownvotedByCurrentUser = true,
                        )
                    }
                    else -> promoCode
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

            // Track analytics
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

            // Track analytics - use generic event for share as no specific extension exists
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

            delay(1000) // Brief delay for UI feedback
            _uiState.update { it.copy(isSharing = false) }
        }
    }

    private fun handleBookmarkToggle() {
        // TODO: Implement bookmark functionality
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

    private fun dismissError() {
        _uiState.update { it.copy(errorType = null) }
    }
}
