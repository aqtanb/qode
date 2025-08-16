package com.qodein.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.domain.repository.PromoCodeSortBy
import com.qodein.core.domain.usecase.promocode.GetPromoCodesUseCase
import com.qodein.core.domain.usecase.promocode.VoteOnPromoCodeUseCase
import com.qodein.core.model.PromoCode
import com.qodein.core.model.PromoCodeId
import com.qodein.core.model.UserId
import com.qodein.core.ui.component.HeroBannerItem
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
    private val voteOnPromoCodeUseCase: VoteOnPromoCodeUseCase
    // TODO: Inject other repositories when available
    // private val bannerRepository: BannerRepository,
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
            is HomeAction.BannerItemClicked -> onBannerItemClicked(action.item)
            is HomeAction.PromoCodeClicked -> onPromoCodeClicked(action.promoCode)
            is HomeAction.UpvotePromoCode -> onUpvotePromoCode(action.promoCodeId)
            is HomeAction.DownvotePromoCode -> onDownvotePromoCode(action.promoCodeId)
            is HomeAction.CopyPromoCode -> onCopyPromoCode(action.promoCode)
            is HomeAction.LoadMorePromoCodes -> loadMorePromoCodes()
            is HomeAction.RetryClicked -> loadHomeData()
            is HomeAction.ErrorDismissed -> dismissError()
        }
    }

    private suspend fun loadPromoCodesWithFallback(banners: List<HeroBannerItem>) {
        // Try POPULARITY first (preferred for established apps with voted content)
        Log.d(TAG, "loadPromoCodesWithFallback: Trying POPULARITY sort first")
        try {
            getPromoCodesUseCase(
                sortBy = PromoCodeSortBy.POPULARITY,
                limit = 20,
            ).catch { e ->
                Log.w(TAG, "loadPromoCodesWithFallback: POPULARITY query failed, trying NEWEST fallback", e)
                loadPromoCodesWithSort(PromoCodeSortBy.NEWEST, banners)
                return@catch
            }.collect { result ->
                result.fold(
                    onSuccess = { promoCodes ->
                        if (promoCodes.isNotEmpty()) {
                            Log.d(TAG, "loadPromoCodesWithFallback: POPULARITY SUCCESS - Retrieved ${promoCodes.size} promo codes")
                            updateSuccessState(banners, promoCodes)
                        } else {
                            Log.d(TAG, "loadPromoCodesWithFallback: POPULARITY returned empty, trying NEWEST fallback")
                            loadPromoCodesWithSort(PromoCodeSortBy.NEWEST, banners)
                        }
                    },
                    onFailure = { error ->
                        Log.w(TAG, "loadPromoCodesWithFallback: POPULARITY failed, trying NEWEST fallback", error)
                        loadPromoCodesWithSort(PromoCodeSortBy.NEWEST, banners)
                    },
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "loadPromoCodesWithFallback: Exception during POPULARITY query, trying NEWEST fallback", e)
            loadPromoCodesWithSort(PromoCodeSortBy.NEWEST, banners)
        }
    }

    private suspend fun loadPromoCodesWithSort(
        sortBy: PromoCodeSortBy,
        banners: List<HeroBannerItem>
    ) {
        Log.d(TAG, "loadPromoCodesWithSort: Starting query with $sortBy sort")
        getPromoCodesUseCase(
            sortBy = sortBy,
            limit = 20,
        ).catch { e ->
            Log.e(TAG, "loadPromoCodesWithSort: Query failed with exception", e)
            _uiState.value = HomeUiState.Error(
                exception = Exception("Failed to load promocodes: ${e.message}"),
                isRetryable = true,
            )
        }.collect { result ->
            Log.d(TAG, "loadPromoCodesWithSort: Query completed, processing result")
            result.fold(
                onSuccess = { promoCodes ->
                    Log.d(TAG, "loadPromoCodesWithSort: SUCCESS - Retrieved ${promoCodes.size} promo codes")
                    updateSuccessState(banners, promoCodes)
                },
                onFailure = { error ->
                    Log.e(TAG, "loadPromoCodesWithSort: FAILURE - Query returned error", error)
                    _uiState.value = HomeUiState.Error(
                        exception = Exception("Failed to load promocodes: ${error.message}"),
                        isRetryable = true,
                    )
                },
            )
        }
    }

    private fun updateSuccessState(
        banners: List<HeroBannerItem>,
        promoCodes: List<PromoCode>
    ) {
        promoCodes.forEachIndexed { index, promoCode ->
            Log.d(TAG, "  [$index] PromoCode: ${promoCode.code} for ${promoCode.serviceName}")
        }
        _uiState.value = HomeUiState.Success(
            bannerItems = banners,
            promoCodes = promoCodes,
            hasMorePromoCodes = promoCodes.size >= 20,
        )
    }

    private fun loadHomeData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "loadHomeData: Starting data load, isRefresh=$isRefresh")
                _uiState.value = if (isRefresh) HomeUiState.Refreshing else HomeUiState.Loading

                // Load banners (mock for now)
                val banners = getMockBannerItems()
                Log.d(TAG, "loadHomeData: Loaded ${banners.size} banner items")

                // Load promocodes with smart fallback strategy
                loadPromoCodesWithFallback(banners)
            } catch (exception: Exception) {
                _uiState.value = HomeUiState.Error(
                    exception = Exception("Failed to load data. Please try again."),
                    isRetryable = true,
                )
            }
        }
    }

    private fun emitEvent(event: HomeEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    private fun onBannerItemClicked(item: HeroBannerItem) {
        // TODO: Track analytics
        // analyticsRepository.trackBannerClick(item.id)

        emitEvent(HomeEvent.BannerDetailRequested(item))
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
                    _uiState.value = HomeUiState.Error(
                        exception = Exception("Failed to vote: ${e.message}"),
                        isRetryable = false,
                    )
                }.collect { vote ->
                    // Success - the optimistic update is already in place
                }
            } catch (exception: Exception) {
                // Revert to original state on error
                _uiState.value = currentState
                _uiState.value = HomeUiState.Error(
                    exception = Exception("Failed to upvote. Please try again."),
                    isRetryable = false,
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
                _uiState.value = HomeUiState.Error(
                    exception = Exception("Failed to load more promo codes."),
                    isRetryable = true,
                )
            }
        }
    }

    private fun dismissError() {
        // Return to loading state to retry
        _uiState.value = HomeUiState.Loading
    }

    // Mock data - TODO: Remove when repositories are implemented
    private fun getMockBannerItems(): List<HeroBannerItem> {
        // Return mock banner items
        return emptyList() // Implementation would go here
    }

    private fun getMockPromoCodes(): List<PromoCode> {
        // Return mock promo codes
        return emptyList() // Implementation would go here
    }
}
