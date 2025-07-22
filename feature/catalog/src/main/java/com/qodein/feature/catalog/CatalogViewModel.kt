package com.qodein.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.ui.component.PromoCodeListState
import com.qodein.core.ui.component.SortOption
import com.qodein.core.ui.model.Category
import com.qodein.core.ui.model.PromoCode
import com.qodein.core.ui.model.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    // TODO: Inject real repositories when available
    // private val promoCodeRepository: PromoCodeRepository,
    // private val storeRepository: StoreRepository,
    // private val categoryRepository: CategoryRepository,
    // private val userRepository: UserRepository,
    // private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var loadDataJob: Job? = null

    private val currentPage = MutableStateFlow(0)
    private val pageSize = 20

    init {
        loadInitialData()
    }

    // Handle actions from the UI
    fun handleAction(action: CatalogAction) {
        when (action) {
            // Search actions
            is CatalogAction.SearchQueryChanged -> handleSearchQueryChanged(action.query)
            is CatalogAction.SearchSubmitted -> handleSearchSubmitted()
            is CatalogAction.SearchCleared -> handleSearchCleared()
            is CatalogAction.SearchSuggestionSelected -> handleSearchSuggestionSelected(action.suggestion)

            // Filter actions
            is CatalogAction.QuickFilterSelected -> handleQuickFilterSelected(action.filterId)
            is CatalogAction.CategoryFilterSelected -> handleCategoryFilterSelected(action.categoryId)
            is CatalogAction.StoreFilterSelected -> handleStoreFilterSelected(action.storeId)
            is CatalogAction.SortOptionSelected -> handleSortOptionSelected(action.sortOption)
            is CatalogAction.FiltersCleared -> handleFiltersCleared()
            is CatalogAction.FiltersToggled -> handleFiltersToggled()

            // PromoCode actions
            is CatalogAction.PromoCodeClicked -> handlePromoCodeClicked(action.promoCode)
            is CatalogAction.PromoCodeUpvoted -> handlePromoCodeUpvoted(action.promoCode)
            is CatalogAction.PromoCodeCopied -> handlePromoCodeCopied(action.promoCode)

            // Store actions
            is CatalogAction.StoreClicked -> handleStoreClicked(action.store)
            is CatalogAction.StoreFollowToggled -> handleStoreFollowToggled(action.store)

            // Category actions
            is CatalogAction.CategoryClicked -> handleCategoryClicked(action.category)
            is CatalogAction.CategoryFollowToggled -> handleCategoryFollowToggled(action.category)

            // Data loading actions
            is CatalogAction.Refresh -> handleRefresh()
            is CatalogAction.LoadMore -> handleLoadMore()
            is CatalogAction.RetryLoad -> handleRetryLoad()

            // Navigation actions
            is CatalogAction.MenuClicked -> handleMenuClicked()
            is CatalogAction.BackPressed -> handleBackPressed()

            // UI state actions
            is CatalogAction.ErrorDismissed -> handleErrorDismissed(action.errorId)
            is CatalogAction.SnackbarDismissed -> handleSnackbarDismissed()
        }
    }

    private fun loadInitialData() {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Load categories and stores in parallel
                val categories = loadCategories()
                val stores = loadStores()

                _uiState.update {
                    it.copy(
                        categories = categories,
                        stores = stores,
                        isLoading = false,
                    )
                }

                loadPromoCodes(reset = true)
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        promoCodesState = PromoCodeListState.Error(
                            exception.message ?: "Failed to load data",
                        ),
                    )
                }
            }
        }
    }

    private fun loadPromoCodes(reset: Boolean = false) {
        if (reset) {
            currentPage.value = 0
            _uiState.update { it.copy(promoCodesState = PromoCodeListState.Loading) }
        } else {
            _uiState.update { it.copy(isLoadingMore = true) }
        }

        viewModelScope.launch {
            try {
                val page = if (reset) 0 else currentPage.value + 1
                val promoCodes = fetchPromoCodes(
                    query = _uiState.value.searchQuery,
                    categoryId = _uiState.value.selectedCategoryId,
                    storeId = _uiState.value.selectedStoreId,
                    quickFilter = _uiState.value.quickFilter,
                    sortOption = _uiState.value.sortOption,
                    page = page,
                    pageSize = pageSize,
                )

                val existingCodes = if (reset) {
                    emptyList()
                } else {
                    _uiState.value.promoCodes
                }

                val allCodes = existingCodes + promoCodes
                val hasMore = promoCodes.size == pageSize

                _uiState.update {
                    it.copy(
                        promoCodesState = if (allCodes.isEmpty()) {
                            PromoCodeListState.Empty
                        } else {
                            PromoCodeListState.Success(allCodes)
                        },
                        isLoadingMore = false,
                        hasMoreItems = hasMore,
                        hasActiveFilters = it.isFilterActive(),
                    )
                }

                if (!reset) {
                    currentPage.value = page
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        promoCodesState = if (reset) {
                            PromoCodeListState.Error(exception.message ?: "Failed to load promo codes")
                        } else {
                            it.promoCodesState
                        },
                        isLoadingMore = false,
                        errorMessages = it.errorMessages + ErrorMessage(
                            id = UUID.randomUUID().toString(),
                            message = exception.message ?: "Failed to load more codes",
                            type = ErrorType.NETWORK,
                        ),
                    )
                }
            }
        }
    }

    // Search action handlers
    private fun handleSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                showSearchSuggestions = query.isNotEmpty(),
            )
        }

        // Debounced search
        searchJob?.cancel()
        if (query.isNotEmpty()) {
            searchJob = viewModelScope.launch {
                delay(300) // Debounce delay
                loadSearchSuggestions(query)
                if (query.length >= 2) {
                    loadPromoCodes(reset = true)
                }
            }
        } else {
            _uiState.update { it.copy(searchSuggestions = emptyList()) }
            loadPromoCodes(reset = true)
        }
    }

    private fun handleSearchSubmitted() {
        _uiState.update { it.copy(showSearchSuggestions = false) }
        loadPromoCodes(reset = true)
        // TODO: Track search analytics
    }

    private fun handleSearchCleared() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                searchSuggestions = emptyList(),
                showSearchSuggestions = false,
            )
        }
        loadPromoCodes(reset = true)
    }

    private fun handleSearchSuggestionSelected(suggestion: String) {
        _uiState.update {
            it.copy(
                searchQuery = suggestion,
                showSearchSuggestions = false,
            )
        }
        loadPromoCodes(reset = true)
    }

    // Filter action handlers
    private fun handleQuickFilterSelected(filterId: String) {
        _uiState.update { it.copy(quickFilter = filterId) }
        loadPromoCodes(reset = true)
    }

    private fun handleCategoryFilterSelected(categoryId: String?) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
        loadPromoCodes(reset = true)
    }

    private fun handleStoreFilterSelected(storeId: String?) {
        _uiState.update { it.copy(selectedStoreId = storeId) }
        loadPromoCodes(reset = true)
    }

    private fun handleSortOptionSelected(sortOption: SortOption) {
        _uiState.update { it.copy(sortOption = sortOption) }
        loadPromoCodes(reset = true)
    }

    private fun handleFiltersCleared() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                quickFilter = "all",
                selectedCategoryId = null,
                selectedStoreId = null,
                sortOption = SortOption.Recent,
                showSearchSuggestions = false,
            )
        }
        loadPromoCodes(reset = true)
    }

    private fun handleFiltersToggled() {
        _uiState.update { it.copy(showFilters = !it.showFilters) }
    }

    // PromoCode action handlers
    private fun handlePromoCodeClicked(promoCode: PromoCode) {
        // TODO: Navigate to promo code details
        // TODO: Track click analytics
    }

    private fun handlePromoCodeUpvoted(promoCode: PromoCode) {
        if (!_uiState.value.isLoggedIn) {
            showSnackbar("Please log in to upvote promo codes")
            return
        }

        viewModelScope.launch {
            try {
                // TODO: Call repository to toggle upvote
                // val updatedCode = promoCodeRepository.toggleUpvote(promoCode.id)

                // For now, simulate the update locally
                val updatedCodes = _uiState.value.promoCodes.map { code ->
                    if (code.id == promoCode.id) {
                        code.copy(
                            isUpvoted = !code.isUpvoted,
                            upvotes = if (code.isUpvoted) code.upvotes - 1 else code.upvotes + 1,
                        )
                    } else {
                        code
                    }
                }

                _uiState.update {
                    it.copy(
                        promoCodesState = PromoCodeListState.Success(updatedCodes),
                    )
                }

                val message = if (promoCode.isUpvoted) "Upvote removed" else "Code upvoted!"
                showSnackbar(message)
            } catch (exception: Exception) {
                showError("Failed to upvote code: ${exception.message}")
            }
        }
    }

    private fun handlePromoCodeCopied(promoCode: PromoCode) {
        // TODO: Copy to clipboard
        showSnackbar("Code ${promoCode.code} copied to clipboard")
        // TODO: Track copy analytics
    }

    // Store action handlers
    private fun handleStoreClicked(store: Store) {
        // TODO: Navigate to store details
    }

    private fun handleStoreFollowToggled(store: Store) {
        if (!_uiState.value.isLoggedIn) {
            showSnackbar("Please log in to follow stores")
            return
        }

        viewModelScope.launch {
            try {
                // TODO: Call repository to toggle follow
                val message = if (store.isFollowed) "Unfollowed ${store.name}" else "Following ${store.name}"
                showSnackbar(message)
            } catch (exception: Exception) {
                showError("Failed to ${if (store.isFollowed) "unfollow" else "follow"} store")
            }
        }
    }

    // Category action handlers
    private fun handleCategoryClicked(category: Category) {
        _uiState.update { it.copy(selectedCategoryId = category.id) }
        loadPromoCodes(reset = true)
    }

    private fun handleCategoryFollowToggled(category: Category) {
        if (!_uiState.value.isLoggedIn) {
            showSnackbar("Please log in to follow categories")
            return
        }

        viewModelScope.launch {
            try {
                // TODO: Call repository to toggle follow
                val message = if (category.isFollowed) "Unfollowed ${category.name}" else "Following ${category.name}"
                showSnackbar(message)
            } catch (exception: Exception) {
                showError("Failed to ${if (category.isFollowed) "unfollow" else "follow"} category")
            }
        }
    }

    // Data loading action handlers
    private fun handleRefresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            try {
                loadPromoCodes(reset = true)
            } finally {
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun handleLoadMore() {
        if (!_uiState.value.isLoadingMore && _uiState.value.hasMoreItems) {
            loadPromoCodes(reset = false)
        }
    }

    private fun handleRetryLoad() {
        loadPromoCodes(reset = true)
    }

    // Navigation action handlers
    private fun handleMenuClicked() {
        // TODO: Open navigation drawer or menu
    }

    private fun handleBackPressed() {
        // TODO: Handle back navigation
    }

    // UI state action handlers
    private fun handleErrorDismissed(errorId: String) {
        _uiState.update {
            it.copy(errorMessages = it.errorMessages.filter { error -> error.id != errorId })
        }
    }

    private fun handleSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    // Helper methods
    private fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    private fun showError(message: String) {
        val error = ErrorMessage(
            id = UUID.randomUUID().toString(),
            message = message,
            type = ErrorType.GENERAL,
        )
        _uiState.update { it.copy(errorMessages = it.errorMessages + error) }
    }

    // Mock data loading methods - replace with real repository calls
    private suspend fun loadCategories(): List<Category> {
        delay(500) // Simulate network delay
        return getMockCategories()
    }

    private suspend fun loadStores(): List<Store> {
        delay(500) // Simulate network delay
        return getMockStores()
    }

    private suspend fun loadSearchSuggestions(query: String) {
        delay(200) // Simulate network delay
        val suggestions = getMockSearchSuggestions().filter {
            it.contains(query, ignoreCase = true)
        }.take(5)

        _uiState.update { it.copy(searchSuggestions = suggestions) }
    }

    private suspend fun fetchPromoCodes(
        query: String,
        categoryId: String?,
        storeId: String?,
        quickFilter: String,
        sortOption: SortOption,
        page: Int,
        pageSize: Int
    ): List<PromoCode> {
        delay(800) // Simulate network delay

        // TODO: Replace with real API call
        var codes = getMockPromoCodes()

        // Apply filters
        if (query.isNotEmpty()) {
            codes = codes.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true) ||
                    it.code.contains(query, ignoreCase = true) ||
                    it.store.name.contains(query, ignoreCase = true)
            }
        }

        categoryId?.let { id ->
            codes = codes.filter { it.category.id == id }
        }

        storeId?.let { id ->
            codes = codes.filter { it.store.id == id }
        }

        // Apply quick filters
        when (quickFilter) {
            "popular" -> codes = codes.sortedByDescending { it.upvotes }
            "new" -> codes = codes.sortedByDescending { it.createdAt }
            "trending" -> codes = codes.sortedByDescending { it.upvotes * 2 + (it.createdAt.dayOfYear * 0.1).toInt() }
            "verified" -> codes = codes.filter { it.isVerified }

            // danger because of !! operator
            "expiring" -> codes = codes.filter {
                it.expiryDate != null && it.expiryDate!!.isBefore(java.time.LocalDate.now().plusDays(7))
            }
        }

        // Apply sorting
        codes = when (sortOption) {
            SortOption.Recent -> codes.sortedByDescending { it.createdAt }
            SortOption.Popular -> codes.sortedByDescending { it.upvotes }
            SortOption.ExpiringFirst -> codes.sortedBy { it.expiryDate ?: java.time.LocalDate.MAX }
            SortOption.DiscountAmount -> codes.sortedByDescending {
                it.discountPercentage ?: it.discountAmount ?: 0
            }
            SortOption.StoreAZ -> codes.sortedBy { it.store.name }
        }

        // Apply pagination
        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, codes.size)

        return if (startIndex < codes.size) {
            codes.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }

    // Mock data - replace with real data sources
    private fun getMockPromoCodes(): List<PromoCode> = com.qodein.core.ui.preview.PreviewData.samplePromoCodes

    private fun getMockCategories(): List<Category> = com.qodein.core.ui.preview.PreviewData.sampleCategories

    private fun getMockStores(): List<Store> = com.qodein.core.ui.preview.PreviewData.sampleStores

    private fun getMockSearchSuggestions(): List<String> =
        listOf(
            "electronics deals",
            "kaspi bank promo",
            "fashion discounts",
            "food delivery codes",
            "beauty products",
            "fitness equipment",
            "books and education",
            "travel and hotels",
            "mobile accessories",
            "home appliances",
        )
}
