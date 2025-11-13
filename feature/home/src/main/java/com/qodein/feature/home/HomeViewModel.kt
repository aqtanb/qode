package com.qodein.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logPromoCodeView
import com.qodein.core.ui.state.ServiceSelectionUiState
import com.qodein.feature.home.ui.state.BannerState
import com.qodein.feature.home.ui.state.PromoCodeState
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.coordinator.ServiceSelectionCoordinator
import com.qodein.shared.domain.service.selection.SelectionState
import com.qodein.shared.domain.service.selection.ServiceSelectionAction
import com.qodein.shared.domain.usecase.banner.GetBannersUseCase
import com.qodein.shared.domain.usecase.preferences.ObserveLanguageUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodesUseCase
import com.qodein.shared.model.Banner
import com.qodein.shared.model.CompleteFilterState
import com.qodein.shared.model.ContentSortBy
import com.qodein.shared.model.Discount
import com.qodein.shared.model.Language
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.ServiceFilter
import com.qodein.shared.model.SortFilter
import com.qodein.shared.ui.FilterDialogType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getBannersUseCase: GetBannersUseCase,
    private val getPromoCodesUseCase: GetPromocodesUseCase,
    private val observeLanguageUseCase: ObserveLanguageUseCase,

    // Service selection coordinator
    private val serviceSelectionCoordinator: ServiceSelectionCoordinator,
    // Analytics
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    val serviceSelectionUiState: StateFlow<ServiceSelectionUiState> = uiState.map { state ->
        ServiceSelectionUiState(
            domainState = state.serviceSelectionState,
            isVisible = true,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ServiceSelectionUiState(),
    )

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
        private const val PROMO_CODE_TYPE_PERCENTAGE = "percentage"
        private const val PROMO_CODE_TYPE_FIXED_AMOUNT = "fixed_amount"
        private const val CONTENT_TYPE_BANNER = "banner"
        private const val EVENT_COPY_PROMOCODE = "copy_promocode"
    }

    init {
        observeLanguage()
        loadHomeData()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.RefreshData -> {
                _uiState.update { it.copy(isRefreshing = true) }
                loadHomeData()
                _uiState.update { it.copy(isRefreshing = false) }
            }
            is HomeAction.BannerClicked -> onBannerClicked(action.banner)
            is HomeAction.PromoCodeClicked -> onPromoCodeClicked(action.promocodeId)
            is HomeAction.CopyPromoCode -> onCopyPromoCode(action.promoCode)
            is HomeAction.LoadMorePromoCodes -> loadNextPage()
            is HomeAction.ErrorDismissed -> dismissError()
            is HomeAction.RetryBannersClicked -> retryBanners()
            is HomeAction.RetryPromoCodesClicked -> retryPromoCodes()

            is HomeAction.ShowFilterDialog -> showFilterDialog(action.type)
            is HomeAction.DismissFilterDialog -> dismissFilterDialog()
            is HomeAction.ApplyServiceFilter -> applyServiceFilter(action.serviceFilter)
            is HomeAction.ApplySortFilter -> applySortFilter(action.sortFilter)
            is HomeAction.ResetFilters -> resetFilters()
        }
    }

    fun onServiceSelectionAction(action: ServiceSelectionAction) {
        val currentState = _uiState.value.serviceSelectionState
        val newState = serviceSelectionCoordinator.handleAction(currentState, action)

        _uiState.update { state ->
            state.copy(serviceSelectionState = newState)
        }

        if (action is ServiceSelectionAction.ToggleService) {
            syncServiceFilterFromSelection()
        }
    }

    /**
     * Derives ServiceFilter from serviceSelectionState (single source of truth)
     * and updates currentFilters to keep them in sync
     */
    private fun syncServiceFilterFromSelection() {
        val selectionState = _uiState.value.serviceSelectionState
        val allServices = (
            selectionState.popular.services +
                (selectionState.search.status as? com.qodein.shared.domain.service.selection.SearchStatus.Success)?.services.orEmpty()
            ).associateBy { it.id }

        val serviceFilter = when (val selection = selectionState.selection) {
            is SelectionState.Multi -> {
                if (selection.selectedIds.isEmpty()) {
                    ServiceFilter.All
                } else {
                    val services = selection.selectedIds.mapNotNull { serviceId ->
                        allServices[serviceId]
                    }.toSet()
                    ServiceFilter.Selected(services)
                }
            }
            is SelectionState.Single -> {
                selection.selectedId?.let { serviceId ->
                    allServices[serviceId]?.let { service ->
                        ServiceFilter.Selected(setOf(service))
                    }
                } ?: ServiceFilter.All
            }
        }

        // Update the filter (which triggers promo code reload)
        onAction(HomeAction.ApplyServiceFilter(serviceFilter))
    }

    // MARK: Loading and Error Handling
    private fun loadHomeData() {
        loadBanners()
        loadInitialPage()
    }

    private fun observeLanguage() {
        Logger.d("HomeViewModel") { "observeLanguage() called" }
        viewModelScope.launch {
            Logger.d("HomeViewModel") { "Starting to collect language flow" }
            observeLanguageUseCase().collect { result ->
                Logger.d("HomeViewModel") { "Received language result: $result" }
                when (result) {
                    is Result.Error -> {
                        Logger.d("HomeViewModel") { "Language error, defaulting to English" }
                        _uiState.update { it.copy(userLanguage = Language.ENGLISH) }
                    }
                    is Result.Success -> {
                        Logger.d("HomeViewModel") { "Language changed to: ${result.data}" }
                        _uiState.update { it.copy(userLanguage = result.data) }
                        Logger.d("HomeViewModel") { "UI state updated, new language: ${_uiState.value.userLanguage}" }
                    }
                }
            }
        }
    }

    private fun loadBanners() {
        viewModelScope.launch {
            _uiState.update { it.copy(bannerState = BannerState.Loading) }

            val bannersResult = getBannersUseCase()
            when (bannersResult) {
                is Result.Error -> {
                    _uiState.update { it.copy(bannerState = BannerState.Error(bannersResult.error)) }
                }
                is Result.Success -> {
                    _uiState.update { it.copy(bannerState = BannerState.Success(bannersResult.data)) }
                }
            }
        }
    }

    private fun loadPromoCodes(paginationRequest: PaginationRequest<ContentSortBy>) {
        viewModelScope.launch {
            val filters = _uiState.value.currentFilters
            val isLoadMore = paginationRequest.cursor != null

            // Set loading state before starting operation
            if (!isLoadMore) {
                _uiState.update { state ->
                    state.copy(promoCodeState = PromoCodeState.Loading)
                }
            } else {
                _uiState.update { state ->
                    state.copy(isLoadingMore = true)
                }
            }

            getPromoCodesUseCase(
                sortBy = filters.sortFilter.sortBy,
                filterByServices = when (val serviceFilter = filters.serviceFilter) {
                    ServiceFilter.All -> null
                    is ServiceFilter.Selected -> serviceFilter.services.map { it.name }
                },
                paginationRequest = paginationRequest,
            ).collect { result ->
                _uiState.update { state ->
                    when (result) {
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
        paginatedResult: PaginatedResult<PromoCode, ContentSortBy>,
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
        result: Result.Error<OperationError>,
        isLoadMore: Boolean,
        currentState: HomeUiState
    ): HomeUiState =
        if (isLoadMore) {
            currentState
        } else {
            currentState.copy(
                promoCodeState = PromoCodeState.Error(
                    errorType = result.error,
                    isRetryable = true,
                    shouldShowSnackbar = false,
                    errorCode = null,
                ),
            )
        }

    private fun emitEvent(event: HomeEvent) {
        val success = _events.tryEmit(event)
        if (!success) {
            viewModelScope.launch {
                _events.emit(event)
            }
        }
    }

    private fun onBannerClicked(banner: Banner) {
        logBannerAnalytics(banner)
        emitEvent(HomeEvent.BannerDetailRequested(banner))
    }

    private fun onPromoCodeClicked(promocodeId: PromocodeId) {
        emitEvent(HomeEvent.PromoCodeDetailRequested(promocodeId))
    }

    private fun onCopyPromoCode(promoCode: PromoCode) {
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
        when (this.discount) {
            is Discount.Percentage -> PROMO_CODE_TYPE_PERCENTAGE
            is Discount.FixedAmount -> PROMO_CODE_TYPE_FIXED_AMOUNT
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

    // MARK: - Filter Actions

    private fun showFilterDialog(type: FilterDialogType) {
        _uiState.update { state ->
            state.copy(activeFilterDialog = type)
        }

        if (type == FilterDialogType.Service) {
            setupServiceSelection()
            syncSelectionFromFilter()
        }
    }

    /**
     * Initializes serviceSelectionState from currentFilters when opening dialog
     * This ensures the selection UI shows what's currently filtered
     */
    private fun syncSelectionFromFilter() {
        val currentFilter = _uiState.value.currentFilters.serviceFilter
        when (currentFilter) {
            ServiceFilter.All -> {
                // Clear selection
                _uiState.update { state ->
                    state.copy(
                        serviceSelectionState = state.serviceSelectionState.copy(
                            selection = SelectionState.Multi(selectedIds = emptySet()),
                        ),
                    )
                }
            }
            is ServiceFilter.Selected -> {
                // Set selection from filter
                val selectedIds = currentFilter.services.map { it.id }.toSet()
                _uiState.update { state ->
                    state.copy(
                        serviceSelectionState = state.serviceSelectionState.copy(
                            selection = SelectionState.Multi(selectedIds = selectedIds),
                        ),
                    )
                }
            }
        }
    }

    private fun dismissFilterDialog() {
        val currentDialog = _uiState.value.activeFilterDialog
        if (currentDialog == FilterDialogType.Service) {
            serviceSelectionCoordinator.deactivate()
        }
        _uiState.update { state ->
            state.copy(activeFilterDialog = null)
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
        )
    }

    override fun onCleared() {
        super.onCleared()
        serviceSelectionCoordinator.deactivate()
        Logger.d("HomeViewModel: Clearing resources")
    }
}
