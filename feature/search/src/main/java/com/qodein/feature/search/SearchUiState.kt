package com.qodein.feature.search

import androidx.compose.runtime.Stable
import com.qodein.core.ui.component.PromoCodeListState
import com.qodein.core.ui.component.SortOption
import com.qodein.core.ui.model.Category
import com.qodein.core.ui.model.PromoCode
import com.qodein.core.ui.model.Store

/**
 * UI State for the Catalog screen
 */
@Stable
data class SearchUiState(
    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMoreItems: Boolean = false,

    // Content data
    val promoCodesState: PromoCodeListState = PromoCodeListState.Loading,
    val categories: List<Category> = emptyList(),
    val stores: List<Store> = emptyList(),
    val searchSuggestions: List<String> = emptyList(),

    // Search and filters
    val searchQuery: String = "",
    val quickFilter: String = "all",
    val selectedCategoryId: String? = null,
    val selectedStoreId: String? = null,
    val sortOption: SortOption = SortOption.Recent,
    val showFilters: Boolean = false,
    val hasActiveFilters: Boolean = false,

    // User state
    val isLoggedIn: Boolean = false,

    // Error handling
    val errorMessages: List<ErrorMessage> = emptyList(),
    val snackbarMessage: String? = null,

    // UI state
    val showSearchSuggestions: Boolean = false
) {

    val promoCodes: List<PromoCode>
        get() = when (promoCodesState) {
            is PromoCodeListState.Success -> promoCodesState.promoCodes
            else -> emptyList()
        }

    val isError: Boolean
        get() = promoCodesState is PromoCodeListState.Error

    val isEmpty: Boolean
        get() = promoCodesState is PromoCodeListState.Empty ||
            (promoCodesState is PromoCodeListState.Success && promoCodesState.promoCodes.isEmpty())

    fun getSelectedCategory(): Category? =
        selectedCategoryId?.let { id ->
            categories.find { it.id == id }
        }

    fun getSelectedStore(): Store? =
        selectedStoreId?.let { id ->
            stores.find { it.id == id }
        }

    fun isFilterActive(): Boolean =
        searchQuery.isNotEmpty() ||
            quickFilter != "all" ||
            selectedCategoryId != null ||
            selectedStoreId != null ||
            sortOption != SortOption.Recent

    fun getActiveFiltersCount(): Int {
        var count = 0
        if (searchQuery.isNotEmpty()) count++
        if (quickFilter != "all") count++
        if (selectedCategoryId != null) count++
        if (selectedStoreId != null) count++
        if (sortOption != SortOption.Recent) count++
        return count
    }

    /**
     * Get filter summary text
     */
    fun getFilterSummary(): String {
        val filters = mutableListOf<String>()

        if (searchQuery.isNotEmpty()) {
            filters.add("\"$searchQuery\"")
        }

        getSelectedCategory()?.let { category ->
            filters.add(category.name)
        }

        getSelectedStore()?.let { store ->
            filters.add(store.name)
        }

        if (quickFilter != "all") {
            filters.add(quickFilter.replaceFirstChar { it.uppercase() })
        }

        return when {
            filters.isEmpty() -> "All promo codes"
            filters.size == 1 -> filters.first()
            else -> "${filters.first()} +${filters.size - 1} more"
        }
    }
}

/**
 * Error message data class with unique ID for dismissal
 */
data class ErrorMessage(val id: String, val message: String, val type: ErrorType = ErrorType.GENERAL)

/**
 * Types of errors that can occur
 */
enum class ErrorType {
    NETWORK,
    GENERAL,
    VALIDATION
}

/**
 * Filter state data class for complex filter management
 */
data class FilterState(
    val quickFilters: List<QuickFilterItem> = getDefaultQuickFilters(),
    val categories: List<Category> = emptyList(),
    val stores: List<Store> = emptyList(),
    val sortOptions: List<SortOption> = SortOption.values().toList()
)

/**
 * Quick filter item data class
 */
data class QuickFilterItem(val id: String, val label: String, val isSelected: Boolean = false, val count: Int? = null)

/**
 * Default quick filters
 */
private fun getDefaultQuickFilters(): List<QuickFilterItem> =
    listOf(
        QuickFilterItem(id = "all", label = "All"),
        QuickFilterItem(id = "popular", label = "Popular"),
        QuickFilterItem(id = "new", label = "New"),
        QuickFilterItem(id = "trending", label = "Trending"),
        QuickFilterItem(id = "verified", label = "Verified"),
        QuickFilterItem(id = "expiring", label = "Expiring Soon"),
    )
