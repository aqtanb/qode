package com.qodein.feature.home

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
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

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.RefreshData -> loadHomeData()
            is HomeAction.BannerItemClicked -> onBannerItemClicked(action.item)
            is HomeAction.PromoCodeClicked -> onPromoCodeClicked(action.promoCode)
            is HomeAction.UpvotePromoCode -> onUpvotePromoCode(action.promoCodeId)
            is HomeAction.FollowStore -> onFollowStore(action.storeId)
            is HomeAction.CopyPromoCode -> onCopyPromoCode(action.promoCode)
            is HomeAction.LoadMorePromoCodes -> loadMorePromoCodes()
            is HomeAction.ErrorDismissed -> dismissError()
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Load banners (mock for now)
                val banners = getMockBannerItems()

                // Load trending promocodes from Firestore
                getPromoCodesUseCase(
                    sortBy = PromoCodeSortBy.POPULARITY,
                    limit = 20,
                ).catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load promocodes: ${e.message}",
                        )
                    }
                }.collect { result ->
                    result.fold(
                        onSuccess = { promoCodes ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    bannerItems = banners,
                                    promoCodes = promoCodes,
                                    hasMorePromoCodes = promoCodes.size >= 20,
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Failed to load promocodes: ${error.message}",
                                )
                            }
                        },
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load data. Please try again.",
                    )
                }
            }
        }
    }

    private fun onBannerItemClicked(item: HeroBannerItem) {
        viewModelScope.launch {
            // TODO: Track analytics
            // analyticsRepository.trackBannerClick(item.id)

            // TODO: Navigate to appropriate screen
            // navigationRepository.navigateToBannerDetail(item.id)
        }
    }

    private fun onPromoCodeClicked(promoCode: PromoCode) {
        viewModelScope.launch {
            // TODO: Track analytics
            // analyticsRepository.trackPromoCodeView(promoCode.id)

            // TODO: Navigate to promo code detail
            // navigationRepository.navigateToPromoCodeDetail(promoCode.id)
        }
    }

    private fun onUpvotePromoCode(promoCodeId: String) {
        if (!_uiState.value.isLoggedIn) {
            // TODO: Show login prompt
            return
        }

        viewModelScope.launch {
            try {
                // Update UI optimistically first
                val wasUpvoted = _uiState.value.promoCodes
                    .find { it.id.value == promoCodeId }?.let { false } ?: false // TODO: track user votes

                _uiState.update { currentState ->
                    currentState.copy(
                        promoCodes = currentState.promoCodes.map { promoCode ->
                            if (promoCode.id.value == promoCodeId) {
                                when (promoCode) {
                                    is PromoCode.PercentagePromoCode -> promoCode.copy(
                                        upvotes = if (wasUpvoted) promoCode.upvotes - 1 else promoCode.upvotes + 1,
                                    )
                                    is PromoCode.FixedAmountPromoCode -> promoCode.copy(
                                        upvotes = if (wasUpvoted) promoCode.upvotes - 1 else promoCode.upvotes + 1,
                                    )
                                    is PromoCode.PromoPromoCode -> promoCode.copy(
                                        upvotes = if (wasUpvoted) promoCode.upvotes - 1 else promoCode.upvotes + 1,
                                    )
                                }
                            } else {
                                promoCode
                            }
                        },
                    )
                }

                // Call the actual voting use case
                voteOnPromoCodeUseCase(
                    promoCodeId = PromoCodeId(promoCodeId),
                    userId = UserId("current_user"), // TODO: Get actual user ID
                    isUpvote = !wasUpvoted,
                ).catch { e ->
                    // Revert optimistic update on error
                    _uiState.update { currentState ->
                        currentState.copy(
                            promoCodes = currentState.promoCodes.map { promoCode ->
                                if (promoCode.id.value == promoCodeId) {
                                    when (promoCode) {
                                        is PromoCode.PercentagePromoCode -> promoCode.copy(
                                            upvotes = if (wasUpvoted) promoCode.upvotes + 1 else promoCode.upvotes - 1,
                                        )
                                        is PromoCode.FixedAmountPromoCode -> promoCode.copy(
                                            upvotes = if (wasUpvoted) promoCode.upvotes + 1 else promoCode.upvotes - 1,
                                        )
                                        is PromoCode.PromoPromoCode -> promoCode.copy(
                                            upvotes = if (wasUpvoted) promoCode.upvotes + 1 else promoCode.upvotes - 1,
                                        )
                                    }
                                } else {
                                    promoCode
                                }
                            },
                            error = "Failed to vote: ${e.message}",
                        )
                    }
                }.collect { vote ->
                    // Success - the optimistic update is already in place
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to upvote. Please try again.")
                }
            }
        }
    }

    private fun onFollowStore(storeId: String) {
        if (!_uiState.value.isLoggedIn) {
            // TODO: Show login prompt
            return
        }

        viewModelScope.launch {
            try {
                // TODO: Call repository to follow/unfollow store
                // storeRepository.toggleFollowStore(storeId)

                // Update UI optimistically
                // Note: Domain model doesn't have store following concept,
                // this would need to be handled separately in a store repository
                _uiState.update { currentState ->
                    currentState.copy(
                        successMessage = "Store follow/unfollow functionality pending - needs store repository implementation",
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to follow store. Please try again.")
                }
            }
        }
    }

    private fun onCopyPromoCode(promoCode: PromoCode) {
        viewModelScope.launch {
            // TODO: Copy to clipboard
            // clipboardRepository.copyToClipboard(promoCode.code)

            // TODO: Track analytics
            // analyticsRepository.trackPromoCodeCopy(promoCode.id)

            // Show success message
            _uiState.update {
                it.copy(successMessage = "Promo code copied to clipboard!")
            }
        }
    }

    private fun loadMorePromoCodes() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMorePromoCodes) return

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoadingMore = true) }

                // TODO: Load more promo codes from repository
                // val morePromoCodes = promoCodeRepository.getPromoCodes(
                //     offset = _uiState.value.promoCodes.size
                // )

                // Mock additional promo codes
                val morePromoCodes = getMockPromoCodes().take(5)

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoadingMore = false,
                        promoCodes = currentState.promoCodes + morePromoCodes,
                        hasMorePromoCodes = morePromoCodes.isNotEmpty(),
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        error = "Failed to load more promo codes.",
                    )
                }
            }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
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
