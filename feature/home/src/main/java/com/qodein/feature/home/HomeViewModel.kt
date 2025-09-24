package com.qodein.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logPromoCodeView
import com.qodein.core.data.coordinator.ServiceSelectionCoordinator
import com.qodein.feature.home.ui.state.BannerState
import com.qodein.feature.home.ui.state.PromoCodeState
import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.getErrorCode
import com.qodein.shared.common.result.isRetryable
import com.qodein.shared.common.result.shouldShowSnackbar
import com.qodein.shared.common.result.toErrorType
import com.qodein.shared.domain.service.selection.ServiceSelectionAction
import com.qodein.shared.domain.usecase.banner.GetBannersUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodesUseCase
import com.qodein.shared.model.Banner
import com.qodein.shared.model.CategoryFilter
import com.qodein.shared.model.CompleteFilterState
import com.qodein.shared.model.Language
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.ServiceFilter
import com.qodein.shared.model.SortFilter
import com.qodein.shared.ui.FilterDialogType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    // Single-responsibility use cases
    private val getBannersUseCase: GetBannersUseCase,
    private val getPromoCodesUseCase: GetPromocodesUseCase,
    // Service selection coordinator
    private val serviceSelectionCoordinator: ServiceSelectionCoordinator,
    // Analytics
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
        private const val SEARCH_DEBOUNCE_MILLIS = 300L
        private const val MIN_SEARCH_QUERY_LENGTH = 2

        // Analytics constants
        private const val PROMO_CODE_TYPE_PERCENTAGE = "percentage"
        private const val PROMO_CODE_TYPE_FIXED_AMOUNT = "fixed_amount"
        private const val CONTENT_TYPE_BANNER = "banner"
        private const val EVENT_COPY_PROMOCODE = "copy_promocode"
    }

    init {
        loadHomeData()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.RefreshData -> loadHomeData()
            is HomeAction.BannerClicked -> onBannerClicked(action.banner)
            is HomeAction.PromoCodeClicked -> onPromoCodeClicked(action.promoCode)
            is HomeAction.CopyPromoCode -> onCopyPromoCode(action.promoCode)
            is HomeAction.LoadMorePromoCodes -> loadNextPage()
            is HomeAction.ErrorDismissed -> dismissError()
            is HomeAction.RetryBannersClicked -> retryBanners()
            is HomeAction.RetryPromoCodesClicked -> retryPromoCodes()
            is HomeAction.RetryServicesClicked -> retryServices()

            // Filter Actions
            is HomeAction.ShowFilterDialog -> showFilterDialog(action.type)
            is HomeAction.DismissFilterDialog -> dismissFilterDialog()
            is HomeAction.ApplyCategoryFilter -> applyCategoryFilter(action.categoryFilter)
            is HomeAction.ApplyServiceFilter -> applyServiceFilter(action.serviceFilter)
            is HomeAction.ApplySortFilter -> applySortFilter(action.sortFilter)
            is HomeAction.ResetFilters -> resetFilters()
            is HomeAction.SearchServices -> searchServices(action.query)
        }
    }

    // MARK: Loading and Error Handling
    private fun loadHomeData() {
        loadBanners()
        loadInitialPage()
    }

    private fun loadBanners(userLanguage: Language? = null) {
        viewModelScope.launch {
            getBannersUseCase.getBanners(userLanguage = userLanguage, limit = DEFAULT_PAGE_SIZE).collect { result ->
                _uiState.update { state ->
                    state.copy(
                        bannerState = when (result) {
                            is Result.Loading -> BannerState.Loading
                            is Result.Success -> BannerState.Success(result.data)
                            is Result.Error -> BannerState.Error(
                                errorType = result.exception.toErrorType(),
                                isRetryable = result.exception.isRetryable(),
                                shouldShowSnackbar = result.exception.shouldShowSnackbar(),
                                errorCode = result.exception.getErrorCode(),
                            )
                        },
                    )
                }
            }
        }
    }

    private fun loadPromoCodes(paginationRequest: PaginationRequest) {
        viewModelScope.launch {
            val filters = _uiState.value.currentFilters
            val isLoadMore = paginationRequest.cursor != null

            getPromoCodesUseCase(
                sortBy = filters.sortFilter.sortBy,
                filterByCategories = when (val categoryFilter = filters.categoryFilter) {
                    CategoryFilter.All -> null
                    is CategoryFilter.Selected -> categoryFilter.categories.toList()
                },
                filterByServices = when (val serviceFilter = filters.serviceFilter) {
                    ServiceFilter.All -> null
                    is ServiceFilter.Selected -> serviceFilter.services.map { it.name }
                },
                paginationRequest = paginationRequest,
            ).collect { result ->
                _uiState.update { state ->
                    when (result) {
                        is Result.Loading -> {
                            if (isLoadMore) {
                                state.copy(isLoadingMore = true)
                            } else {
                                state.copy(promoCodeState = PromoCodeState.Loading)
                            }
                        }
                        is Result.Success -> {
                            val updatedState = handlePromoCodesSuccess(result.data, isLoadMore, state)
                            updatedState.copy(isLoadingMore = false)
                        }
                        is Result.Error -> {
                            val updatedState = handlePromoCodesError(result, isLoadMore, state)
                            updatedState.copy(isLoadingMore = false)
                        }
                    }
                }
            }
        }
    }
    private fun loadInitialPage() {
        loadPromoCodes(PaginationRequest.firstPage(DEFAULT_PAGE_SIZE))
    }

    private fun loadNextPage() {
        val currentState = _uiState.value
        val promoState = currentState.promoCodeState
        if (promoState !is PromoCodeState.Success || !promoState.hasMore || currentState.isLoadingMore) {
            return
        }

        val cursor = promoState.nextCursor ?: return
        loadPromoCodes(PaginationRequest.nextPage(cursor, DEFAULT_PAGE_SIZE))
    }

    private fun handlePromoCodesSuccess(
        paginatedResult: PaginatedResult<PromoCode>,
        isLoadMore: Boolean,
        currentState: HomeUiState
    ): HomeUiState =
        if (isLoadMore) {
            val currentPromoCodes = (currentState.promoCodeState as? PromoCodeState.Success)?.promoCodes ?: emptyList()
            currentState.copy(
                promoCodeState = PromoCodeState.Success(
                    promoCodes = currentPromoCodes + paginatedResult.data,
                    hasMore = paginatedResult.hasMore,
                    nextCursor = paginatedResult.nextCursor,
                ),
            )
        } else {
            currentState.copy(
                promoCodeState = if (paginatedResult.data.isNotEmpty()) {
                    PromoCodeState.Success(
                        promoCodes = paginatedResult.data,
                        hasMore = paginatedResult.hasMore,
                        nextCursor = paginatedResult.nextCursor,
                    )
                } else {
                    PromoCodeState.Empty
                },
            )
        }

    private fun handlePromoCodesError(
        result: Result.Error,
        isLoadMore: Boolean,
        currentState: HomeUiState
    ): HomeUiState =
        if (isLoadMore) {
            // For load more errors, keep existing data but stop loading
            currentState
        } else {
            currentState.copy(
                promoCodeState = PromoCodeState.Error(
                    errorType = result.exception.toErrorType(),
                    isRetryable = result.exception.isRetryable(),
                    shouldShowSnackbar = result.exception.shouldShowSnackbar(),
                    errorCode = result.exception.getErrorCode(),
                ),
            )
        }

    private fun emitEvent(event: HomeEvent) {
        val success = _events.tryEmit(event)
        if (!success) {
            // Fallback to coroutine if buffer is full (unlikely but safer)
            viewModelScope.launch {
                _events.emit(event)
            }
        }
    }

    private fun onBannerClicked(banner: Banner) {
        logBannerAnalytics(banner)
        emitEvent(HomeEvent.BannerDetailRequested(banner))
    }

    private fun onPromoCodeClicked(promoCode: PromoCode) {
        logPromoCodeViewAnalytics(promoCode)
        emitEvent(HomeEvent.PromoCodeDetailRequested(promoCode))
    }

    private fun onCopyPromoCode(promoCode: PromoCode) {
        // Note: Clipboard functionality not yet implemented
        logPromoCodeCopyAnalytics(promoCode)
        emitEvent(HomeEvent.PromoCodeCopied(promoCode))
    }

    // MARK: - Analytics Helpers

    private fun logBannerAnalytics(banner: Banner) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                type = AnalyticsEvent.Types.SELECT_CONTENT,
                extras = listOf(
                    AnalyticsEvent.Param("content_type", CONTENT_TYPE_BANNER),
                    AnalyticsEvent.Param("item_id", banner.id.value),
                ),
            ),
        )
    }

    private fun logPromoCodeViewAnalytics(promoCode: PromoCode) {
        analyticsHelper.logPromoCodeView(
            promocodeId = promoCode.id.value,
            promocodeType = promoCode.getAnalyticsType(),
        )
    }

    private fun logPromoCodeCopyAnalytics(promoCode: PromoCode) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                type = EVENT_COPY_PROMOCODE,
                extras = listOf(
                    AnalyticsEvent.Param("promocode_id", promoCode.id.value),
                    AnalyticsEvent.Param("promocode_type", promoCode.getAnalyticsType()),
                ),
            ),
        )
    }

    private fun PromoCode.getAnalyticsType(): String =
        when (this) {
            is PromoCode.PercentagePromoCode -> PROMO_CODE_TYPE_PERCENTAGE
            is PromoCode.FixedAmountPromoCode -> PROMO_CODE_TYPE_FIXED_AMOUNT
        }

    private fun dismissError() {
        loadHomeData()
    }

    // MARK: - Specific Error Recovery Methods

    private fun retryBanners() {
        Logger.d("HomeViewModel: Retrying banner load")
        loadBanners()
    }

    private fun retryPromoCodes() {
        Logger.d("HomeViewModel: Retrying promo codes load")
        loadInitialPage()
    }

    private fun retryServices() {
        Logger.d("HomeViewModel: Retrying services load")
        handleServiceSelectionAction(ServiceSelectionAction.RetryPopularServices)
    }

    // MARK: - Filter Actions

    private fun showFilterDialog(type: FilterDialogType) {
        _uiState.update { state ->
            state.copy(activeFilterDialog = type)
        }

        // Activate and load popular services when service dialog is opened
        if (type == FilterDialogType.Service) {
            setupServiceSelection()
            handleServiceSelectionAction(ServiceSelectionAction.LoadPopularServices)
        }
    }

    private fun dismissFilterDialog() {
        _uiState.update { state ->
            state.copy(activeFilterDialog = null)
        }
    }

    private fun applyCategoryFilter(categoryFilter: CategoryFilter) {
        applyFilterChange { currentFilters ->
            currentFilters.applyCategoryFilter(categoryFilter)
        }
    }

    private fun applyServiceFilter(serviceFilter: ServiceFilter) {
        applyFilterChange { currentFilters ->
            currentFilters.applyServiceFilter(serviceFilter)
        }
    }

    private fun applySortFilter(sortFilter: SortFilter) {
        applyFilterChange { currentFilters ->
            currentFilters.applySortFilter(sortFilter)
        }
    }

    private inline fun applyFilterChange(filterChange: (CompleteFilterState) -> CompleteFilterState) {
        val currentFilters = _uiState.value.currentFilters
        val newFilters = filterChange(currentFilters)
        applyFilters(newFilters)
    }

    private fun resetFilters() {
        val currentFilters = _uiState.value.currentFilters
        val resetFilters = currentFilters.reset()
        applyFilters(resetFilters)
    }

    private fun applyFilters(newFilters: CompleteFilterState) {
        _uiState.update {
            it.copy(
                currentFilters = newFilters,
                promoCodeState = PromoCodeState.Loading,
            )
        }

        loadPromoCodes(PaginationRequest.firstPage(DEFAULT_PAGE_SIZE))
    }

    // MARK: - Service Selection

    private fun setupServiceSelection() {
        serviceSelectionCoordinator.setupServiceSelection(
            scope = viewModelScope,
            getCurrentState = { _uiState.value.serviceSelectionState },
            onStateUpdate = { newState ->
                _uiState.update { state ->
                    state.copy(serviceSelectionState = newState)
                }
            },
            onCachedServicesUpdate = { cachedServices ->
                _uiState.update { state ->
                    state.copy(cachedServices = cachedServices)
                }
            },
        )
    }

    private fun handleServiceSelectionAction(action: ServiceSelectionAction) {
        val currentState = _uiState.value.serviceSelectionState
        val newState = serviceSelectionCoordinator.handleAction(currentState, action)

        _uiState.update { state ->
            state.copy(serviceSelectionState = newState)
        }
    }

    private fun searchServices(query: String) {
        handleServiceSelectionAction(ServiceSelectionAction.UpdateQuery(query))
    }

    override fun onCleared() {
        super.onCleared()
        serviceSelectionCoordinator.deactivate()
        Logger.d("HomeViewModel: Clearing resources")
    }
}
