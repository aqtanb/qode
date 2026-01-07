package com.qodein.feature.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.ui.refresh.RefreshTarget
import com.qodein.core.ui.refresh.ScreenRefreshCoordinator
import com.qodein.feature.home.ui.state.BannerState
import com.qodein.feature.home.ui.state.PromocodeUiState
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.usecase.banner.GetBannersUseCase
import com.qodein.shared.domain.usecase.preferences.ObserveLanguageUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodesUseCase
import com.qodein.shared.domain.usecase.service.GetServicesByIdsUseCase
import com.qodein.shared.model.Banner
import com.qodein.shared.model.CompleteFilterState
import com.qodein.shared.model.ContentSortBy
import com.qodein.shared.model.Discount
import com.qodein.shared.model.Language
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.ServiceFilter
import com.qodein.shared.model.ServiceId
import com.qodein.shared.ui.FilterDialogType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getBannersUseCase: GetBannersUseCase,
    private val getPromocodesUseCase: GetPromocodesUseCase,
    private val observeLanguageUseCase: ObserveLanguageUseCase,
    private val screenRefreshCoordinator: ScreenRefreshCoordinator,
    private val analyticsHelper: AnalyticsHelper,
    private val getServicesByIdsUseCase: GetServicesByIdsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    companion object {
        private const val PROMO_CODE_TYPE_PERCENTAGE = "percentage"
        private const val PROMO_CODE_TYPE_FIXED_AMOUNT = "fixed_amount"
        private const val CONTENT_TYPE_BANNER = "banner"
        private const val EVENT_COPY_PROMOCODE = "copy_promocode"
        private const val KEY_SCROLL_INDEX = "scroll_index"
        private const val KEY_SCROLL_OFFSET = "scroll_offset"
    }

    // Scroll state preservation
    fun saveScrollPosition(
        firstVisibleItemIndex: Int,
        firstVisibleItemScrollOffset: Int
    ) {
        savedStateHandle[KEY_SCROLL_INDEX] = firstVisibleItemIndex
        savedStateHandle[KEY_SCROLL_OFFSET] = firstVisibleItemScrollOffset
    }

    fun getSavedScrollIndex(): Int = savedStateHandle.get<Int>(KEY_SCROLL_INDEX) ?: 0

    fun getSavedScrollOffset(): Int = savedStateHandle.get<Int>(KEY_SCROLL_OFFSET) ?: 0

    init {
        observeLanguage()
        loadHomeData()
        observeRefreshTrigger()
    }

    fun applyServiceSelection(serviceIds: Set<ServiceId>) {
        Timber.d("applyServiceSelection: ${serviceIds.size} service IDs")
        val currentFilter = _uiState.value.currentFilters.serviceFilter

        if (serviceIds.isEmpty()) {
            // Only apply if current filter is not already "All"
            if (currentFilter !is ServiceFilter.All) {
                applyFilters(_uiState.value.currentFilters.applyServiceFilter(ServiceFilter.All))
            } else {
                Timber.d("applyServiceSelection: Already showing all services, no change needed")
            }
            return
        }

        viewModelScope.launch {
            val services = getServicesByIdsUseCase(serviceIds)
            Timber.d("getServicesByIdsUseCase returned ${services.size} services")

            // Check if the selection actually changed
            val selectionChanged = when (currentFilter) {
                is ServiceFilter.All -> true
                is ServiceFilter.Selected -> {
                    val currentIds = currentFilter.services.map { it.id }.toSet()
                    currentIds != serviceIds
                }
            }

            if (selectionChanged) {
                val serviceFilter = ServiceFilter.Selected(services)
                applyFilters(_uiState.value.currentFilters.applyServiceFilter(serviceFilter))
            } else {
                Timber.d("applyServiceSelection: Selection unchanged, no reload needed")
            }
        }
    }

    private fun observeRefreshTrigger() {
        viewModelScope.launch {
            screenRefreshCoordinator.refreshSignals
                .filter { it == RefreshTarget.HOME }
                .collect {
                    Timber.d("Refresh signal received for HOME")
                    loadHomeData()
                }
        }
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
            is HomeAction.ApplyServiceFilter -> {
                applyFilters(_uiState.value.currentFilters.applyServiceFilter(action.serviceFilter))
            }
            is HomeAction.ApplySortFilter -> applyFilters(_uiState.value.currentFilters.applySortFilter(action.sortFilter))
            is HomeAction.ResetFilters -> resetFilters()
        }
    }

    // MARK: Loading and Error Handling
    private fun loadHomeData() {
        loadBanners()
        loadInitialPage()
    }

    private fun observeLanguage() {
        Timber.d("observeLanguage() called")
        viewModelScope.launch {
            Timber.d("Starting to collect language flow")
            observeLanguageUseCase().collect { result ->
                Timber.d("Received language result: $result")
                when (result) {
                    is Result.Error -> {
                        Timber.d("Language error, defaulting to English")
                        _uiState.update { it.copy(userLanguage = Language.ENGLISH) }
                    }
                    is Result.Success -> {
                        Timber.d("Language changed to: ${result.data}")
                        _uiState.update { it.copy(userLanguage = result.data) }
                        Timber.d("UI state updated, new language: ${_uiState.value.userLanguage}")
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

    private fun loadPromocodes(paginationRequest: PaginationRequest<ContentSortBy>) {
        viewModelScope.launch {
            val filters = _uiState.value.currentFilters
            val isLoadMore = paginationRequest.cursor != null

            // Set loading state before starting operation
            if (!isLoadMore) {
                _uiState.update { state ->
                    state.copy(promocodeUiState = PromocodeUiState.Loading)
                }
            } else {
                _uiState.update { state ->
                    state.copy(isLoadingMore = true)
                }
            }

            val promocodeResult = getPromocodesUseCase(
                sortBy = filters.sortFilter.sortBy,
                filterByServices = when (val serviceFilter = filters.serviceFilter) {
                    ServiceFilter.All -> null
                    is ServiceFilter.Selected -> serviceFilter.services.map { it.name }
                },
                paginationRequest = paginationRequest,
            )

            _uiState.update { state ->
                val updatedState =
                    when (promocodeResult) {
                        is Result.Error -> handlePromoCodesError(promocodeResult, isLoadMore, state)
                        is Result.Success -> handlePromoCodesSuccess(promocodeResult.data, isLoadMore, state)
                    }

                updatedState.copy(isLoadingMore = false)
            }
        }
    }
    private fun loadInitialPage() {
        loadPromocodes(PaginationRequest.firstPage(GetPromocodesUseCase.DEFAULT_LIMIT))
    }

    private fun loadNextPage() {
        val currentState = _uiState.value
        val promoState = currentState.promocodeUiState
        if (promoState !is PromocodeUiState.Success || !promoState.hasMore || currentState.isLoadingMore) {
            return
        }

        val cursor = promoState.nextCursor ?: return
        loadPromocodes(PaginationRequest.nextPage(cursor, GetPromocodesUseCase.DEFAULT_LIMIT))
    }

    private fun handlePromoCodesSuccess(
        paginatedResult: PaginatedResult<Promocode, ContentSortBy>,
        isLoadMore: Boolean,
        currentState: HomeUiState
    ): HomeUiState =
        if (isLoadMore) {
            val currentPromoCodes = (currentState.promocodeUiState as? PromocodeUiState.Success)?.promocodes ?: emptyList()
            currentState.copy(
                promocodeUiState = PromocodeUiState.Success(
                    promocodes = currentPromoCodes + paginatedResult.data,
                    hasMore = paginatedResult.hasMore,
                    nextCursor = paginatedResult.nextCursor,
                ),
            )
        } else {
            currentState.copy(
                promocodeUiState = if (paginatedResult.data.isNotEmpty()) {
                    PromocodeUiState.Success(
                        promocodes = paginatedResult.data,
                        hasMore = paginatedResult.hasMore,
                        nextCursor = paginatedResult.nextCursor,
                    )
                } else {
                    PromocodeUiState.Empty
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
                promocodeUiState = PromocodeUiState.Error(error = result.error),
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

    private fun onCopyPromoCode(promoCode: Promocode) {
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

    private fun logPromoCodeCopyAnalytics(promoCode: Promocode) {
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

    private fun Promocode.getAnalyticsType(): String =
        when (this.discount) {
            is Discount.Percentage -> PROMO_CODE_TYPE_PERCENTAGE
            is Discount.FixedAmount -> PROMO_CODE_TYPE_FIXED_AMOUNT
        }

    private fun dismissError() {
        loadHomeData()
    }

    // MARK: - Specific Error Recovery Methods

    private fun retryBanners() {
        Timber.d("Retrying banner load")
        loadBanners()
    }

    private fun retryPromoCodes() {
        Timber.d("Retrying promocodes load")
        loadInitialPage()
    }

    // MARK: - Filter Actions

    private fun showFilterDialog(type: FilterDialogType) {
        if (type == FilterDialogType.Service) {
            val currentSelectedIds = when (val currentFilter = _uiState.value.currentFilters.serviceFilter) {
                is ServiceFilter.Selected -> currentFilter.services.map { it.id }.toSet()
                ServiceFilter.All -> emptySet()
            }

            viewModelScope.launch {
                _events.emit(HomeEvent.ShowServiceSelection(currentSelectedIds))
            }
        } else {
            _uiState.update { state ->
                state.copy(activeFilterDialog = type)
            }
        }
    }

    private fun dismissFilterDialog() {
        _uiState.update { state ->
            state.copy(activeFilterDialog = null)
        }
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
                promocodeUiState = PromocodeUiState.Loading,
            )
        }

        loadPromocodes(PaginationRequest.firstPage(GetPromocodesUseCase.DEFAULT_LIMIT))
    }
}
