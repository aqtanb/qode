package com.qodein.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.ui.component.HeroBannerItem
import com.qodein.core.ui.model.PromoCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    // TODO: Inject repositories when available
    // private val promoCodeRepository: PromoCodeRepository,
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

                // TODO: Replace with actual repository calls
                // val banners = bannerRepository.getFeaturedBanners()
                // val promoCodes = promoCodeRepository.getTrendingPromoCodes()
                // val userPreferences = userRepository.getUserPreferences()

                // Mock data for now
                val banners = getMockBannerItems()
                val promoCodes = getMockPromoCodes()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        bannerItems = banners,
                        promoCodes = promoCodes,
                        hasMorePromoCodes = true,
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
                // TODO: Call repository to upvote
                // promoCodeRepository.upvotePromoCode(promoCodeId)

                // Update UI optimistically
                _uiState.update { currentState ->
                    currentState.copy(
                        promoCodes = currentState.promoCodes.map { promoCode ->
                            if (promoCode.id == promoCodeId) {
                                promoCode.copy(
                                    isUpvoted = !promoCode.isUpvoted,
                                    upvotes = if (promoCode.isUpvoted) {
                                        promoCode.upvotes - 1
                                    } else {
                                        promoCode.upvotes + 1
                                    },
                                )
                            } else {
                                promoCode
                            }
                        },
                    )
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
                _uiState.update { currentState ->
                    currentState.copy(
                        promoCodes = currentState.promoCodes.map { promoCode ->
                            if (promoCode.store.id == storeId) {
                                promoCode.copy(
                                    store = promoCode.store.copy(
                                        isFollowed = !promoCode.store.isFollowed,
                                        followersCount = if (promoCode.store.isFollowed) {
                                            promoCode.store.followersCount - 1
                                        } else {
                                            promoCode.store.followersCount + 1
                                        },
                                    ),
                                )
                            } else {
                                promoCode
                            }
                        },
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
