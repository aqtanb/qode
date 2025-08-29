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
import com.qodein.shared.domain.usecase.promocode.GetPromoCodeByIdUseCase
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PromocodeDetailViewModel @Inject constructor(
    private val getPromoCodeByIdUseCase: GetPromoCodeByIdUseCase,
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
                            _uiState.update { currentState ->
                                currentState.copy(
                                    promoCode = result.data,
                                    isLoading = false,
                                    errorType = null,
                                    isBookmarked = result.data?.isBookmarkedByCurrentUser ?: false,
                                )
                            }
                            // Track analytics
                            result.data?.let { promoCode ->
                                val promoType = when (promoCode) {
                                    is PromoCode.PercentagePromoCode -> "percentage"
                                    is PromoCode.FixedAmountPromoCode -> "fixed_amount"
                                }
                                analyticsHelper.logPromoCodeView(promoCode.id.value, promoType)
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

    private fun refreshCurrentPromocode() {
        val currentPromoCode = _uiState.value.promoCode
        currentPromoCode?.let {
            loadPromocode(it.id)
        }
    }

    private fun handleVote(isUpvote: Boolean) {
        val currentState = _uiState.value
        val promoCode = currentState.promoCode ?: return

        if (currentState.isVoting) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isVoting = true,
                    showVoteAnimation = true,
                    lastVoteType = if (isUpvote) VoteType.UPVOTE else VoteType.DOWNVOTE,
                )
            }

            try {
                // TODO: Get actual user ID from authentication
                val userId = UserId("current_user")

                voteOnPromoCodeUseCase(promoCode.id, userId, isUpvote).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            // Refresh the promocode to get updated vote counts
                            refreshCurrentPromocode()

                            _events.emit(PromocodeDetailEvent.ShowVoteFeedback(isUpvote))

                            // Track analytics
                            val voteType = if (isUpvote) "upvote" else "downvote"
                            analyticsHelper.logVote(promoCode.id.value, voteType)
                        }
                        is Result.Loading -> {
                            // Loading state already handled by isVoting flag
                        }
                        is Result.Error -> {
                            _events.emit(
                                PromocodeDetailEvent.ShowSnackbar(
                                    "Failed to vote. Please try again.",
                                ),
                            )
                            Logger.e("PromocodeDetailViewModel") {
                                "Failed to vote: ${result.exception.message}"
                            }
                        }
                    }
                    _uiState.update { it.copy(isVoting = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isVoting = false) }
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
