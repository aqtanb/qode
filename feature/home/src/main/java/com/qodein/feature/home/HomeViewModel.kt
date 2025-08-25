package com.qodein.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logPromoCodeView
import com.qodein.core.analytics.logVote
import com.qodein.feature.home.model.CategoryFilter
import com.qodein.feature.home.model.FilterDialogType
import com.qodein.feature.home.model.FilterState
import com.qodein.feature.home.model.PromoCodeTypeFilter
import com.qodein.feature.home.model.ServiceFilter
import com.qodein.feature.home.model.SortFilter
import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.isRetryable
import com.qodein.shared.common.result.shouldShowSnackbar
import com.qodein.shared.common.result.toErrorType
import com.qodein.shared.domain.repository.PromoCodeSortBy
import com.qodein.shared.domain.usecase.banner.GetBannersUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromoCodesUseCase
import com.qodein.shared.domain.usecase.promocode.VoteOnPromoCodeUseCase
import com.qodein.shared.domain.usecase.service.GetPopularServicesUseCase
import com.qodein.shared.domain.usecase.service.SearchServicesUseCase
import com.qodein.shared.model.Banner
import com.qodein.shared.model.PaginationCursor
import com.qodein.shared.model.PaginationRequest
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPromoCodesUseCase: GetPromoCodesUseCase,
    private val voteOnPromoCodeUseCase: VoteOnPromoCodeUseCase,
    private val getBannersUseCase: GetBannersUseCase,
    private val getPopularServicesUseCase: GetPopularServicesUseCase,
    private val searchServicesUseCase: SearchServicesUseCase,
    private val analyticsHelper: AnalyticsHelper
    // TODO: Inject other repositories when available
    // private val userRepository: UserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    private var currentCursor: PaginationCursor? = null
    private var currentSortBy = PromoCodeSortBy.POPULARITY
    private val pageSize = 20

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

            // Filter Actions
            is HomeAction.ShowFilterDialog -> showFilterDialog(action.type)
            is HomeAction.DismissFilterDialog -> dismissFilterDialog()
            is HomeAction.ApplyTypeFilter -> applyTypeFilter(action.typeFilter)
            is HomeAction.ApplyCategoryFilter -> applyCategoryFilter(action.categoryFilter)
            is HomeAction.ApplyServiceFilter -> applyServiceFilter(action.serviceFilter)
            is HomeAction.ApplySortFilter -> applySortFilter(action.sortFilter)
            is HomeAction.ResetFilters -> resetFilters()
            is HomeAction.SearchServices -> searchServices(action.query)
        }
    }

    private suspend fun loadPromoCodesWithFallback(banners: List<Banner> = emptyList()) {
        suspend fun loadWithSort(sortBy: PromoCodeSortBy): Boolean {
            try {
                var success = false
                getPromoCodesUseCase(
                    sortBy = sortBy,
                    paginationRequest = PaginationRequest.firstPage(pageSize),
                ).collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            Timber.d("HomeViewModel: Loading promo codes with sort $sortBy")
                        }
                        is Result.Success -> {
                            if (result.data.data.isNotEmpty()) {
                                Timber.d("HomeViewModel: Successfully loaded ${result.data.data.size} promo codes with $sortBy")
                                currentSortBy = sortBy
                                currentCursor = result.data.nextCursor
                                updateSuccessState(
                                    promoCodes = result.data.data,
                                    banners = banners,
                                    hasMore = result.data.hasMore,
                                )
                                success = true
                            } else {
                                Timber.w("HomeViewModel: No promo codes returned for sort $sortBy")
                            }
                            return@collect
                        }
                        is Result.Error -> {
                            Timber.w("HomeViewModel: Error loading promo codes with $sortBy: ${result.exception}")
                            return@collect
                        }
                    }
                }
                return success
            } catch (e: Exception) {
                Timber.e("HomeViewModel: Exception loading promo codes with $sortBy: $e")
                return false
            }
        }

        Timber.d("HomeViewModel: Starting promo code loading with ${banners.size} banners")

        // Load with POPULARITY sorting (now optimized with voteScore and caching)
        if (!loadWithSort(PromoCodeSortBy.POPULARITY)) {
            Timber.e("HomeViewModel: Failed to load promo codes with popularity sorting")
            _uiState.value = HomeUiState.Error(
                errorType = Exception("Failed to load promo codes").toErrorType(),
                isRetryable = true,
                shouldShowSnackbar = true,
                errorCode = null,
            )
        }
    }

    private fun updateSuccessState(
        promoCodes: List<PromoCode>,
        banners: List<Banner> = emptyList(),
        hasMore: Boolean = false
    ) {
        // Preserve existing services state - don't reset it
        val currentState = _uiState.value as? HomeUiState.Success
        Timber.d("Success: ${promoCodes.size} promo codes, ${banners.size} banners")
        _uiState.value = HomeUiState.Success(
            banners = banners,
            promoCodes = promoCodes,
            hasMorePromoCodes = hasMore,
            availableServices = currentState?.availableServices ?: emptyList(),
            popularServices = currentState?.popularServices ?: emptyList(),
            serviceSearchResults = currentState?.serviceSearchResults ?: emptyList(),
            isSearchingServices = currentState?.isSearchingServices ?: false,
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

            // Reset pagination state on refresh
            if (isRefresh) {
                currentCursor = null
                currentSortBy = PromoCodeSortBy.POPULARITY
            }

            loadBannersAndPromoCodes()
        }
    }

    private suspend fun loadBannersAndPromoCodes() {
        withContext(Dispatchers.IO) {
            var banners = emptyList<Banner>()

            // Load banners first
            // Fixed: Use firstOrNull instead of first() to wait for actual data, not just Result.Loading from asResult()
            // Issue: first() was capturing Result.Loading immediately and terminating flow before Firebase query executed
            Timber.d("HomeViewModel: About to start loading banners")
            try {
                Timber.d("HomeViewModel: Calling getBannersUseCase().firstOrNull { it !is Result.Loading }")
                val bannersResult = getBannersUseCase(limit = 10).firstOrNull { it !is Result.Loading } ?: Result.Loading
                Timber.d("HomeViewModel: getBannersUseCase() completed with result: $bannersResult")
                when (bannersResult) {
                    is Result.Loading -> {
                        Timber.d("HomeViewModel: Loading banners")
                    }
                    is Result.Success -> {
                        banners = bannersResult.data
                        Timber.d("HomeViewModel: Loaded ${banners.size} banners successfully")
                    }
                    is Result.Error -> {
                        Timber.w("HomeViewModel: Banner loading failed: ${bannersResult.exception}")
                        banners = emptyList()
                    }
                }
            } catch (e: Exception) {
                Timber.w("HomeViewModel: Banner loading failed with exception: $e")
                banners = emptyList()
            }
            Timber.d("HomeViewModel: Banner loading phase complete, banners.size = ${banners.size}")

            Timber.d("HomeViewModel: Got ${banners.size} banners, loading promo codes")
            loadPromoCodesWithFallback(banners)
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

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = currentState.copy(isLoadingMore = true)

                Timber.d("HomeViewModel: Loading more promo codes with cursor=${currentCursor?.documentId}, sortBy=$currentSortBy")

                val paginationRequest = if (currentCursor != null) {
                    PaginationRequest.nextPage(currentCursor!!, pageSize)
                } else {
                    PaginationRequest.firstPage(pageSize)
                }

                getPromoCodesUseCase(
                    sortBy = currentSortBy,
                    paginationRequest = paginationRequest,
                ).collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            Timber.d("HomeViewModel: Loading more promo codes...")
                        }
                        is Result.Success -> {
                            val paginatedResult = result.data
                            val morePromoCodes = paginatedResult.data
                            Timber.d("HomeViewModel: Loaded ${morePromoCodes.size} more promo codes")

                            // Update cursor for next pagination
                            currentCursor = paginatedResult.nextCursor

                            _uiState.value = currentState.copy(
                                isLoadingMore = false,
                                promoCodes = currentState.promoCodes + morePromoCodes,
                                hasMorePromoCodes = paginatedResult.hasMore,
                            )
                            return@collect
                        }
                        is Result.Error -> {
                            Timber.w("HomeViewModel: Error loading more promo codes: ${result.exception}")
                            _uiState.value = currentState.copy(isLoadingMore = false)
                            return@collect
                        }
                    }
                }
            } catch (exception: Exception) {
                Timber.e("HomeViewModel: Exception loading more promo codes: $exception")
                _uiState.value = currentState.copy(isLoadingMore = false)
            }
        }
    }

    private fun dismissError() {
        // Return to loading state to retry
        _uiState.value = HomeUiState.Loading
    }

    // MARK: - Filter Actions

    private fun showFilterDialog(type: FilterDialogType) {
        Timber.d("HomeViewModel: showFilterDialog called with type: $type")
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(activeFilterDialog = type)

            // Load popular services when service dialog is opened
            if (type == FilterDialogType.Service) {
                Timber.d("HomeViewModel: Service dialog opened, loading popular services")
                loadPopularServicesIfNeeded()
            }
        }
    }

    private fun dismissFilterDialog() {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(activeFilterDialog = null)
        }
    }

    private fun applyTypeFilter(typeFilter: PromoCodeTypeFilter) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            val newFilters = currentState.currentFilters.copy(typeFilter = typeFilter)
            val filteredPromoCodes = applyFiltersToPromoCodes(currentState.promoCodes, newFilters)
            _uiState.value = currentState.copy(
                currentFilters = newFilters,
                promoCodes = filteredPromoCodes,
            )

            // Log analytics
            analyticsHelper.logEvent(
                AnalyticsEvent(
                    type = "filter_applied",
                    extras = listOf(
                        AnalyticsEvent.Param("filter_type", "type"),
                        AnalyticsEvent.Param(
                            "filter_value",
                            when (typeFilter) {
                                PromoCodeTypeFilter.All -> "all"
                                PromoCodeTypeFilter.Percentage -> "percentage"
                                PromoCodeTypeFilter.FixedAmount -> "fixed_amount"
                            },
                        ),
                    ),
                ),
            )
        }
    }

    private fun applyCategoryFilter(categoryFilter: CategoryFilter) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            val newFilters = currentState.currentFilters.copy(categoryFilter = categoryFilter)
            val filteredPromoCodes = applyFiltersToPromoCodes(currentState.promoCodes, newFilters)
            _uiState.value = currentState.copy(
                currentFilters = newFilters,
                promoCodes = filteredPromoCodes,
            )

            // Log analytics
            analyticsHelper.logEvent(
                AnalyticsEvent(
                    type = "filter_applied",
                    extras = listOf(
                        AnalyticsEvent.Param("filter_type", "category"),
                        AnalyticsEvent.Param(
                            "filter_value",
                            when (categoryFilter) {
                                CategoryFilter.All -> "all"
                                is CategoryFilter.Selected -> categoryFilter.category
                            },
                        ),
                    ),
                ),
            )
        }
    }

    private fun applyServiceFilter(serviceFilter: ServiceFilter) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            val newFilters = currentState.currentFilters.copy(serviceFilter = serviceFilter)
            val filteredPromoCodes = applyFiltersToPromoCodes(currentState.promoCodes, newFilters)
            _uiState.value = currentState.copy(
                currentFilters = newFilters,
                promoCodes = filteredPromoCodes,
            )

            // Log analytics
            analyticsHelper.logEvent(
                AnalyticsEvent(
                    type = "filter_applied",
                    extras = listOf(
                        AnalyticsEvent.Param("filter_type", "service"),
                        AnalyticsEvent.Param(
                            "filter_value",
                            when (serviceFilter) {
                                ServiceFilter.All -> "all"
                                is ServiceFilter.Selected -> serviceFilter.service.name
                            },
                        ),
                    ),
                ),
            )
        }
    }

    private fun applySortFilter(sortFilter: SortFilter) {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            val newFilters = currentState.currentFilters.copy(sortFilter = sortFilter)
            val sortBy = (sortFilter as SortFilter.Selected).sortBy
            val sortedPromoCodes = applySortToPromoCodes(currentState.promoCodes, sortBy)
            _uiState.value = currentState.copy(
                currentFilters = newFilters,
                promoCodes = sortedPromoCodes,
            )

            // Log analytics
            analyticsHelper.logEvent(
                AnalyticsEvent(
                    type = "filter_applied",
                    extras = listOf(
                        AnalyticsEvent.Param("filter_type", "sort"),
                        AnalyticsEvent.Param("filter_value", sortBy.name.lowercase()),
                    ),
                ),
            )
        }
    }

    private fun resetFilters() {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(currentFilters = FilterState())

            // Reload data with default sort
            loadHomeData(isRefresh = true)

            // Log analytics
            analyticsHelper.logEvent(
                AnalyticsEvent(
                    type = "filters_reset",
                    extras = emptyList(),
                ),
            )
        }
    }

    // MARK: - Filter Helper Functions

    private fun applyFiltersToPromoCodes(
        promoCodes: List<PromoCode>,
        filters: FilterState
    ): List<PromoCode> =
        promoCodes
            .filter { promoCode -> matchesTypeFilter(promoCode, filters.typeFilter) }
            .filter { promoCode -> matchesCategoryFilter(promoCode, filters.categoryFilter) }
            .filter { promoCode -> matchesServiceFilter(promoCode, filters.serviceFilter) }
            .let { filteredCodes ->
                val sortBy = (filters.sortFilter as SortFilter.Selected).sortBy
                applySortToPromoCodes(filteredCodes, sortBy)
            }

    private fun matchesTypeFilter(
        promoCode: PromoCode,
        typeFilter: PromoCodeTypeFilter
    ): Boolean =
        when (typeFilter) {
            PromoCodeTypeFilter.All -> true
            PromoCodeTypeFilter.Percentage -> promoCode is PromoCode.PercentagePromoCode
            PromoCodeTypeFilter.FixedAmount -> promoCode is PromoCode.FixedAmountPromoCode
        }

    private fun matchesCategoryFilter(
        promoCode: PromoCode,
        categoryFilter: CategoryFilter
    ): Boolean =
        when (categoryFilter) {
            CategoryFilter.All -> true
            is CategoryFilter.Selected -> {
                promoCode.category?.equals(categoryFilter.category, ignoreCase = true) == true
            }
        }

    private fun matchesServiceFilter(
        promoCode: PromoCode,
        serviceFilter: ServiceFilter
    ): Boolean =
        when (serviceFilter) {
            ServiceFilter.All -> true
            is ServiceFilter.Selected -> {
                promoCode.serviceName.equals(serviceFilter.service.name, ignoreCase = true)
            }
        }

    private fun applySortToPromoCodes(
        promoCodes: List<PromoCode>,
        sortBy: PromoCodeSortBy
    ): List<PromoCode> =
        when (sortBy) {
            PromoCodeSortBy.POPULARITY -> promoCodes.sortedByDescending { it.voteScore }
            PromoCodeSortBy.NEWEST -> promoCodes.sortedByDescending { it.createdAt }
            PromoCodeSortBy.OLDEST -> promoCodes.sortedBy { it.createdAt }
            PromoCodeSortBy.EXPIRING_SOON -> promoCodes.sortedBy { it.endDate }
            PromoCodeSortBy.ALPHABETICAL -> promoCodes.sortedBy { it.serviceName }
        }

    private fun searchServices(query: String) {
        val currentState = _uiState.value
        if (currentState !is HomeUiState.Success) return

        if (query.length < 2) {
            // Clear search results for short queries
            _uiState.value = currentState.copy(
                serviceSearchResults = emptyList(),
                isSearchingServices = false,
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = currentState.copy(isSearchingServices = true)

                val searchResult = searchServicesUseCase(query = query, limit = 20).first()

                when (searchResult) {
                    is Result.Success -> {
                        _uiState.value = currentState.copy(
                            serviceSearchResults = searchResult.data,
                            isSearchingServices = false,
                        )
                    }
                    is Result.Error -> {
                        Timber.e("Error searching services: ${searchResult.exception}")
                        _uiState.value = currentState.copy(
                            serviceSearchResults = emptyList(),
                            isSearchingServices = false,
                        )
                    }
                    is Result.Loading -> {
                        // Keep loading state
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in searchServices")
                _uiState.value = currentState.copy(
                    serviceSearchResults = emptyList(),
                    isSearchingServices = false,
                )
            }
        }
    }

    private fun loadPopularServicesIfNeeded() {
        val currentState = _uiState.value as? HomeUiState.Success ?: return

        Timber.d("HomeViewModel: loadPopularServicesIfNeeded - current popular services count: ${currentState.popularServices.size}")

        // Only load if we don't have popular services yet
        if (currentState.popularServices.isEmpty()) {
            Timber.d("HomeViewModel: Popular services empty, starting to load...")
            viewModelScope.launch {
                try {
                    Timber.d("HomeViewModel: Calling getPopularServicesUseCase...")
                    // Use firstOrNull to skip Result.Loading and get actual data
                    val servicesResult = getPopularServicesUseCase(limit = 20).firstOrNull { it !is Result.Loading } ?: Result.Loading
                    Timber.d("HomeViewModel: getPopularServicesUseCase result: $servicesResult")
                    when (servicesResult) {
                        is Result.Success -> {
                            Timber.d("HomeViewModel: Loaded ${servicesResult.data.size} popular services")
                            if (servicesResult.data.isEmpty()) {
                                Timber.w("HomeViewModel: Popular services query returned empty - trying fallback")
                                // Fallback: try to load any services without sorting
                                try {
                                    val fallbackResult = searchServicesUseCase(query = "", limit = 20).first()
                                    when (fallbackResult) {
                                        is Result.Success -> {
                                            Timber.d("HomeViewModel: Fallback loaded ${fallbackResult.data.size} services")
                                            fallbackResult.data.forEachIndexed { index, service ->
                                                Timber.d(
                                                    "HomeViewModel: Fallback service $index: ${service.name} (${service.promoCodeCount} codes)",
                                                )
                                            }
                                            _uiState.value = currentState.copy(
                                                popularServices = fallbackResult.data,
                                            )
                                        }
                                        else -> Timber.w("HomeViewModel: Fallback also failed: $fallbackResult")
                                    }
                                } catch (e: Exception) {
                                    Timber.w("HomeViewModel: Fallback failed: $e")
                                }
                            } else {
                                servicesResult.data.forEachIndexed { index, service ->
                                    Timber.d("HomeViewModel: Popular service $index: ${service.name} (${service.promoCodeCount} codes)")
                                }
                                _uiState.value = currentState.copy(
                                    popularServices = servicesResult.data,
                                )
                            }
                        }
                        is Result.Error -> {
                            Timber.w("HomeViewModel: Popular services loading failed: ${servicesResult.exception}")
                            // Fallback: try to load any services without sorting
                            Timber.d("HomeViewModel: Trying fallback - loading services without promoCodeCount sorting...")
                            try {
                                val fallbackResult = searchServicesUseCase(query = "", limit = 20).first()
                                when (fallbackResult) {
                                    is Result.Success -> {
                                        Timber.d("HomeViewModel: Fallback loaded ${fallbackResult.data.size} services")
                                        _uiState.value = currentState.copy(
                                            popularServices = fallbackResult.data,
                                        )
                                    }
                                    else -> Timber.w("HomeViewModel: Fallback also failed: $fallbackResult")
                                }
                            } catch (e: Exception) {
                                Timber.w("HomeViewModel: Fallback failed: $e")
                            }
                        }
                        is Result.Loading -> {
                            Timber.d("HomeViewModel: Popular services still loading...")
                        }
                    }
                } catch (e: Exception) {
                    Timber.w("HomeViewModel: Popular services loading failed: $e")
                }
            }
        } else {
            Timber.d("HomeViewModel: Popular services already loaded, skipping")
        }
    }
}
