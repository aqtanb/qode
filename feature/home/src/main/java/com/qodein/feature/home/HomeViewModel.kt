package com.qodein.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logPromoCodeView
import com.qodein.core.analytics.logVote
import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.getErrorCode
import com.qodein.shared.common.result.isRetryable
import com.qodein.shared.common.result.shouldShowSnackbar
import com.qodein.shared.common.result.toErrorType
import com.qodein.shared.domain.repository.PromoCodeSortBy
import com.qodein.shared.domain.usecase.banner.GetBannersUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromoCodesUseCase
import com.qodein.shared.domain.usecase.promocode.VoteOnPromoCodeUseCase
import com.qodein.shared.model.Banner
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import com.qodein.shared.model.UserId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPromoCodesUseCase: GetPromoCodesUseCase,
    private val voteOnPromoCodeUseCase: VoteOnPromoCodeUseCase,
    private val getBannersUseCase: GetBannersUseCase,
    private val analyticsHelper: AnalyticsHelper
    // TODO: Inject other repositories when available
    // private val userRepository: UserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    init {
        loadHomeData()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.RefreshData -> loadHomeData(isRefresh = true)
            is HomeAction.BannerClicked -> onBannerClicked(action.banner)
            is HomeAction.PromoCodeClicked -> onPromoCodeClicked(action.promoCode)
            is HomeAction.UpvotePromoCode -> onUpvotePromoCode(action.promoCodeId)
            is HomeAction.DownvotePromoCode -> onDownvotePromoCode(action.promoCodeId)
            is HomeAction.CopyPromoCode -> onCopyPromoCode(action.promoCode)
            is HomeAction.LoadMorePromoCodes -> loadMorePromoCodes()
            is HomeAction.RetryClicked -> loadHomeData()
            is HomeAction.ErrorDismissed -> dismissError()
        }
    }

    private suspend fun loadPromoCodesWithFallback(banners: List<Banner> = emptyList()) {
        suspend fun loadWithSort(sortBy: PromoCodeSortBy): Boolean {
            try {
                getPromoCodesUseCase(sortBy = sortBy, limit = 20).collect { result ->
                    when (result) {
                        is Result.Loading -> { /* Continue loading */ }
                        is Result.Success -> {
                            if (result.data.isNotEmpty()) {
                                updateSuccessState(result.data, banners)
                                return@collect
                            }
                        }
                        is Result.Error -> return@collect
                    }
                }
                return true
            } catch (e: Exception) {
                return false
            }
        }

        // Try POPULARITY first, fallback to NEWEST
        if (!loadWithSort(PromoCodeSortBy.POPULARITY)) {
            if (!loadWithSort(PromoCodeSortBy.NEWEST)) {
                _uiState.value = HomeUiState.Error(
                    errorType = Exception("Failed to load data").toErrorType(),
                    isRetryable = true,
                    shouldShowSnackbar = true,
                    errorCode = null,
                )
            }
        }
    }

    private fun updateSuccessState(
        promoCodes: List<PromoCode>,
        banners: List<Banner> = emptyList()
    ) {
        Timber.d("Success: ${promoCodes.size} promo codes, ${banners.size} banners")
        _uiState.value = HomeUiState.Success(
            banners = banners,
            promoCodes = promoCodes,
            hasMorePromoCodes = promoCodes.size >= 20,
        )
    }

    private fun loadHomeData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = if (isRefresh) {
                HomeUiState.Refreshing(
                    previousData = if (currentState is HomeUiState.Success) currentState else null,
                )
            } else {
                HomeUiState.Loading
            }

            loadBannersAndPromoCodes()
        }
    }

    private suspend fun loadBannersAndPromoCodes() {
        withContext(Dispatchers.IO) {
            var banners = emptyList<Banner>()
            // Simple banner loading - return@collect already ensures single emission
            getBannersUseCase(limit = 10).collect { bannersResult ->
                when (bannersResult) {
                    is Result.Success -> {
                        banners = bannersResult.data
                        Timber.d("HomeViewModel: Loaded ${banners.size} banners successfully")
                        // Only update state for successful banner results
                        loadPromoCodesWithFallback(banners)
                    }
                    is Result.Loading -> {
                        // Don't update state for loading - wait for success/error
                    }
                    is Result.Error -> {
                        Timber.w("HomeViewModel: Banner loading failed: ${bannersResult.exception}")
                        // Continue with empty banners on error
                        loadPromoCodesWithFallback(emptyList())
                    }
                }
                return@collect
            }
        }
    }

    private fun emitEvent(event: HomeEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    private fun onBannerClicked(banner: Banner) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                type = AnalyticsEvent.Types.SELECT_CONTENT,
                extras = listOf(
                    AnalyticsEvent.Param("content_type", "banner"),
                    AnalyticsEvent.Param("item_id", banner.id.value),
                ),
            ),
        )

        emitEvent(HomeEvent.BannerDetailRequested(banner))
    }

    private fun onPromoCodeClicked(promoCode: PromoCode) {
        analyticsHelper.logPromoCodeView(
            promocodeId = promoCode.id.value,
            promocodeType = when (promoCode) {
                is PromoCode.PercentagePromoCode -> "percentage"
                is PromoCode.FixedAmountPromoCode -> "fixed_amount"
            },
        )

        emitEvent(HomeEvent.PromoCodeDetailRequested(promoCode))
    }

    private fun onUpvotePromoCode(promoCodeId: String) {
        voteOnPromoCode(promoCodeId, isUpvote = true)
    }

    private fun onDownvotePromoCode(promoCodeId: String) {
        voteOnPromoCode(promoCodeId, isUpvote = false)
    }

    private fun voteOnPromoCode(
        promoCodeId: String,
        isUpvote: Boolean
    ) {
        val currentState = _uiState.value
        if (currentState !is HomeUiState.Success) return

        viewModelScope.launch(Dispatchers.IO) {
            // Optimistic update
            val wasVoted = false // TODO: track user votes
            val updatedPromoCodes = currentState.promoCodes.map { promoCode ->
                if (promoCode.id.value == promoCodeId) {
                    val delta = if (wasVoted) -1 else 1
                    when (promoCode) {
                        is PromoCode.PercentagePromoCode -> promoCode.copy(
                            upvotes = if (isUpvote) promoCode.upvotes + delta else promoCode.upvotes,
                            downvotes = if (!isUpvote) promoCode.downvotes + delta else promoCode.downvotes,
                        )
                        is PromoCode.FixedAmountPromoCode -> promoCode.copy(
                            upvotes = if (isUpvote) promoCode.upvotes + delta else promoCode.upvotes,
                            downvotes = if (!isUpvote) promoCode.downvotes + delta else promoCode.downvotes,
                        )
                    }
                } else {
                    promoCode
                }
            }

            _uiState.value = currentState.copy(promoCodes = updatedPromoCodes)

            try {
                voteOnPromoCodeUseCase(
                    promoCodeId = PromoCodeId(promoCodeId),
                    userId = UserId("current_user"), // TODO: Get actual user ID
                    isUpvote = isUpvote,
                ).collect {
                    analyticsHelper.logVote(
                        promocodeId = promoCodeId,
                        voteType = if (isUpvote) "upvote" else "downvote",
                    )
                }
            } catch (e: Exception) {
                // Revert on error
                _uiState.value = currentState
            }
        }
    }

    private fun onCopyPromoCode(promoCode: PromoCode) {
        // TODO: Copy to clipboard
        // clipboardRepository.copyToClipboard(promoCode.code)

        analyticsHelper.logEvent(
            AnalyticsEvent(
                type = "copy_promocode",
                extras = listOf(
                    AnalyticsEvent.Param("promocode_id", promoCode.id.value),
                    AnalyticsEvent.Param(
                        "promocode_type",
                        when (promoCode) {
                            is PromoCode.PercentagePromoCode -> "percentage"
                            is PromoCode.FixedAmountPromoCode -> "fixed_amount"
                        },
                    ),
                ),
            ),
        )

        emitEvent(HomeEvent.PromoCodeCopied(promoCode))
    }

    private fun loadMorePromoCodes() {
        val currentState = _uiState.value
        if (currentState !is HomeUiState.Success) return
        if (currentState.isLoadingMore || !currentState.hasMorePromoCodes) return

        viewModelScope.launch {
            try {
                _uiState.value = currentState.copy(isLoadingMore = true)

                // TODO: Load more promo codes from repository
                // val morePromoCodes = promoCodeRepository.getPromoCodes(
                //     offset = currentState.promoCodes.size
                // )

                // Mock additional promo codes
                val morePromoCodes = getMockPromoCodes().take(5)

                _uiState.value = currentState.copy(
                    isLoadingMore = false,
                    promoCodes = currentState.promoCodes + morePromoCodes,
                    hasMorePromoCodes = morePromoCodes.isNotEmpty(),
                )
            } catch (exception: Exception) {
                val errorException = Exception("Failed to load more promo codes.")
                _uiState.value = HomeUiState.Error(
                    errorType = errorException.toErrorType(),
                    isRetryable = errorException.isRetryable(),
                    shouldShowSnackbar = errorException.shouldShowSnackbar(),
                    errorCode = errorException.getErrorCode(),
                )
            }
        }
    }

    private fun dismissError() {
        // Return to loading state to retry
        _uiState.value = HomeUiState.Loading
    }

    // Mock data - TODO: Remove when repositories are implemented

    private fun getMockPromoCodes(): List<PromoCode> {
        // Return mock promo codes
        return emptyList() // Implementation would go here
    }
}
