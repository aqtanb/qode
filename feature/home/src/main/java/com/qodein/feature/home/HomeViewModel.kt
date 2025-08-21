package com.qodein.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPromoCodesUseCase: GetPromoCodesUseCase,
    private val voteOnPromoCodeUseCase: VoteOnPromoCodeUseCase,
    private val getBannersUseCase: GetBannersUseCase
    // TODO: Inject other repositories when available
    // private val userRepository: UserRepository,
    // private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

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

    private suspend fun loadPromoCodesWithFallback() {
        // Try POPULARITY first (preferred for established apps with voted content)
        Log.d(TAG, "loadPromoCodesWithFallback: Trying POPULARITY sort first")
        try {
            getPromoCodesUseCase(
                sortBy = PromoCodeSortBy.POPULARITY,
                limit = 20,
            ).catch { e ->
                Log.w(TAG, "loadPromoCodesWithFallback: POPULARITY query failed, trying NEWEST fallback", e)
                loadPromoCodesWithSort(PromoCodeSortBy.NEWEST)
                return@catch
            }.collect { result ->
                when (result) {
                    is Result.Loading -> { /* Loading already handled above */ }
                    is Result.Success -> {
                        if (result.data.isNotEmpty()) {
                            Log.d(TAG, "loadPromoCodesWithFallback: POPULARITY SUCCESS - Retrieved ${result.data.size} promo codes")
                            updateSuccessState(result.data)
                        } else {
                            Log.d(TAG, "loadPromoCodesWithFallback: POPULARITY returned empty, trying NEWEST fallback")
                            loadPromoCodesWithSort(PromoCodeSortBy.NEWEST)
                        }
                    }
                    is Result.Error -> {
                        Log.w(TAG, "loadPromoCodesWithFallback: POPULARITY failed, trying NEWEST fallback", result.exception)
                        loadPromoCodesWithSort(PromoCodeSortBy.NEWEST)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "loadPromoCodesWithFallback: Exception during POPULARITY query, trying NEWEST fallback", e)
            loadPromoCodesWithSort(PromoCodeSortBy.NEWEST)
        }
    }

    private suspend fun loadPromoCodesWithSort(sortBy: PromoCodeSortBy) {
        Log.d(TAG, "loadPromoCodesWithSort: Starting query with $sortBy sort")
        getPromoCodesUseCase(
            sortBy = sortBy,
            limit = 20,
        ).catch { e ->
            Log.e(TAG, "loadPromoCodesWithSort: Query failed with exception", e)
            val exception = Exception("Failed to load promocodes: ${e.message}")
            _uiState.value = HomeUiState.Error(
                errorType = exception.toErrorType(),
                isRetryable = exception.isRetryable(),
                shouldShowSnackbar = exception.shouldShowSnackbar(),
                errorCode = exception.getErrorCode(),
            )
        }.collect { result ->
            Log.d(TAG, "loadPromoCodesWithSort: Query completed, processing result")
            when (result) {
                is Result.Loading -> { /* Loading already handled above */ }
                is Result.Success -> {
                    Log.d(TAG, "loadPromoCodesWithSort: SUCCESS - Retrieved ${result.data.size} promo codes")
                    updateSuccessState(result.data)
                }
                is Result.Error -> {
                    Log.e(TAG, "loadPromoCodesWithSort: FAILURE - Query returned error", result.exception)
                    _uiState.value = HomeUiState.Error(
                        errorType = result.exception.toErrorType(),
                        isRetryable = result.exception.isRetryable(),
                        shouldShowSnackbar = result.exception.shouldShowSnackbar(),
                        errorCode = result.exception.getErrorCode(),
                    )
                }
            }
        }
    }

    private fun updateSuccessState(
        promoCodes: List<PromoCode>,
        banners: List<Banner> = emptyList()
    ) {
        promoCodes.forEachIndexed { index, promoCode ->
            Log.d(TAG, "  [$index] PromoCode: ${promoCode.code} for ${promoCode.serviceName}")
        }
        banners.forEachIndexed { index, banner ->
            Log.d(TAG, "  [$index] Banner: ${banner.title} for ${banner.brandName}")
        }
        _uiState.value = HomeUiState.Success(
            banners = banners,
            promoCodes = promoCodes,
            hasMorePromoCodes = promoCodes.size >= 20,
        )
    }

    private fun loadHomeData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "loadHomeData: Starting data load, isRefresh=$isRefresh")
                val currentState = _uiState.value
                _uiState.value = if (isRefresh) {
                    HomeUiState.Refreshing(
                        previousData = if (currentState is HomeUiState.Success) currentState else null,
                    )
                } else {
                    HomeUiState.Loading
                }

                // Load banners and promocodes concurrently
                loadBannersAndPromoCodes()
            } catch (exception: Exception) {
                val errorException = Exception("Failed to load data. Please try again.")
                _uiState.value = HomeUiState.Error(
                    errorType = errorException.toErrorType(),
                    isRetryable = errorException.isRetryable(),
                    shouldShowSnackbar = errorException.shouldShowSnackbar(),
                    errorCode = errorException.getErrorCode(),
                )
            }
        }
    }

    private suspend fun loadBannersAndPromoCodes() {
        try {
            Log.d(TAG, "loadBannersAndPromoCodes: Loading banners first, then promo codes")

            // Load banners first, then pass them to promo code loading
            getBannersUseCase(limit = 10).collect { bannersResult ->
                when (bannersResult) {
                    is Result.Loading -> {
                        Log.d(TAG, "loadBannersAndPromoCodes: Loading banners...")
                        // Continue with loading state, banners will update when ready
                    }
                    is Result.Success -> {
                        Log.d(
                            TAG,
                            "loadBannersAndPromoCodes: Successfully loaded ${bannersResult.data.size} banners, now loading promo codes",
                        )
                        loadPromoCodesWithBanners(bannersResult.data)
                    }
                    is Result.Error -> {
                        Log.w(TAG, "loadBannersAndPromoCodes: Failed to load banners, proceeding with empty list", bannersResult.exception)
                        // Proceed with promo codes even if banners fail
                        loadPromoCodesWithBanners(emptyList())
                    }
                }
            }
        } catch (exception: Exception) {
            Log.e(TAG, "loadBannersAndPromoCodes: Failed to load data", exception)
            val errorException = Exception("Failed to load data. Please try again.")
            _uiState.value = HomeUiState.Error(
                errorType = errorException.toErrorType(),
                isRetryable = errorException.isRetryable(),
                shouldShowSnackbar = errorException.shouldShowSnackbar(),
                errorCode = errorException.getErrorCode(),
            )
        }
    }

    private suspend fun loadPromoCodesWithBanners(banners: List<Banner>) {
        // Try POPULARITY first (preferred for established apps with voted content)
        Log.d(TAG, "loadPromoCodesWithBanners: Trying POPULARITY sort first")
        try {
            getPromoCodesUseCase(
                sortBy = PromoCodeSortBy.POPULARITY,
                limit = 20,
            ).catch { e ->
                Log.w(TAG, "loadPromoCodesWithBanners: POPULARITY query failed, trying NEWEST fallback", e)
                loadPromoCodesWithSortAndBanners(PromoCodeSortBy.NEWEST, banners)
                return@catch
            }.collect { result ->
                when (result) {
                    is Result.Loading -> { /* Loading already handled above */ }
                    is Result.Success -> {
                        if (result.data.isNotEmpty()) {
                            Log.d(TAG, "loadPromoCodesWithBanners: POPULARITY SUCCESS - Retrieved ${result.data.size} promo codes")
                            updateSuccessState(result.data, banners)
                        } else {
                            Log.d(TAG, "loadPromoCodesWithBanners: POPULARITY returned empty, trying NEWEST fallback")
                            loadPromoCodesWithSortAndBanners(PromoCodeSortBy.NEWEST, banners)
                        }
                    }
                    is Result.Error -> {
                        Log.w(TAG, "loadPromoCodesWithBanners: POPULARITY failed, trying NEWEST fallback", result.exception)
                        loadPromoCodesWithSortAndBanners(PromoCodeSortBy.NEWEST, banners)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "loadPromoCodesWithBanners: Exception during POPULARITY query, trying NEWEST fallback", e)
            loadPromoCodesWithSortAndBanners(PromoCodeSortBy.NEWEST, banners)
        }
    }

    private suspend fun loadPromoCodesWithSortAndBanners(
        sortBy: PromoCodeSortBy,
        banners: List<Banner>
    ) {
        Log.d(TAG, "loadPromoCodesWithSortAndBanners: Starting query with $sortBy sort")
        getPromoCodesUseCase(
            sortBy = sortBy,
            limit = 20,
        ).catch { e ->
            Log.e(TAG, "loadPromoCodesWithSortAndBanners: Query failed with exception", e)
            val exception = Exception("Failed to load promocodes: ${e.message}")
            _uiState.value = HomeUiState.Error(
                errorType = exception.toErrorType(),
                isRetryable = exception.isRetryable(),
                shouldShowSnackbar = exception.shouldShowSnackbar(),
                errorCode = exception.getErrorCode(),
            )
        }.collect { result ->
            Log.d(TAG, "loadPromoCodesWithSortAndBanners: Query completed, processing result")
            when (result) {
                is Result.Loading -> { /* Loading already handled above */ }
                is Result.Success -> {
                    Log.d(TAG, "loadPromoCodesWithSortAndBanners: SUCCESS - Retrieved ${result.data.size} promo codes")
                    updateSuccessState(result.data, banners)
                }
                is Result.Error -> {
                    Log.e(TAG, "loadPromoCodesWithSortAndBanners: FAILURE - Query returned error", result.exception)
                    _uiState.value = HomeUiState.Error(
                        errorType = result.exception.toErrorType(),
                        isRetryable = result.exception.isRetryable(),
                        shouldShowSnackbar = result.exception.shouldShowSnackbar(),
                        errorCode = result.exception.getErrorCode(),
                    )
                }
            }
        }
    }

    private fun emitEvent(event: HomeEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    private fun onBannerClicked(banner: Banner) {
        // TODO: Track analytics
        // analyticsRepository.trackBannerClick(banner.id)

        emitEvent(HomeEvent.BannerDetailRequested(banner))
    }

    private fun onPromoCodeClicked(promoCode: PromoCode) {
        // TODO: Track analytics
        // analyticsRepository.trackPromoCodeView(promoCode.id)

        emitEvent(HomeEvent.PromoCodeDetailRequested(promoCode))
    }

    private fun onUpvotePromoCode(promoCodeId: String) {
        // TODO: Check auth state from AuthState or use case
        // For now, allow voting

        val currentState = _uiState.value
        if (currentState !is HomeUiState.Success) return

        viewModelScope.launch {
            try {
                // Update UI optimistically first
                val wasUpvoted = false // TODO: track user votes from repository

                val updatedPromoCodes = currentState.promoCodes.map { promoCode ->
                    if (promoCode.id.value == promoCodeId) {
                        when (promoCode) {
                            is PromoCode.PercentagePromoCode -> promoCode.copy(
                                upvotes = if (wasUpvoted) promoCode.upvotes - 1 else promoCode.upvotes + 1,
                            )
                            is PromoCode.FixedAmountPromoCode -> promoCode.copy(
                                upvotes = if (wasUpvoted) promoCode.upvotes - 1 else promoCode.upvotes + 1,
                            )
                        }
                    } else {
                        promoCode
                    }
                }

                _uiState.value = currentState.copy(promoCodes = updatedPromoCodes)

                // Call the actual voting use case
                voteOnPromoCodeUseCase(
                    promoCodeId = PromoCodeId(promoCodeId),
                    userId = UserId("current_user"), // TODO: Get actual user ID from auth state
                    isUpvote = !wasUpvoted,
                ).catch { e ->
                    // Revert optimistic update on error
                    _uiState.value = currentState
                    val exception = Exception("Failed to vote: ${e.message}")
                    _uiState.value = HomeUiState.Error(
                        errorType = exception.toErrorType(),
                        isRetryable = exception.isRetryable(),
                        shouldShowSnackbar = exception.shouldShowSnackbar(),
                        errorCode = exception.getErrorCode(),
                    )
                }.collect { vote ->
                    // Success - the optimistic update is already in place
                }
            } catch (exception: Exception) {
                // Revert to original state on error
                _uiState.value = currentState
                val exception = Exception("Failed to upvote. Please try again.")
                _uiState.value = HomeUiState.Error(
                    errorType = exception.toErrorType(),
                    isRetryable = exception.isRetryable(),
                    shouldShowSnackbar = exception.shouldShowSnackbar(),
                    errorCode = exception.getErrorCode(),
                )
            }
        }
    }

    private fun onDownvotePromoCode(promoCodeId: String) {
        // TODO: Similar logic to upvote but for downvote
        // For now, placeholder implementation
    }

    private fun onCopyPromoCode(promoCode: PromoCode) {
        // TODO: Copy to clipboard
        // clipboardRepository.copyToClipboard(promoCode.code)

        // TODO: Track analytics
        // analyticsRepository.trackPromoCodeCopy(promoCode.id)

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
