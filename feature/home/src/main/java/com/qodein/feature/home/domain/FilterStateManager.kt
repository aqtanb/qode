package com.qodein.feature.home.domain

import com.qodein.feature.home.model.CategoryFilter
import com.qodein.feature.home.model.ServiceFilter
import com.qodein.feature.home.model.SortFilter
import com.qodein.feature.home.ui.state.FilterState
import com.qodein.shared.domain.repository.PromoCodeSortBy
import com.qodein.shared.model.Service
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Domain service responsible for managing filter state and business rules
 * Centralizes all filter logic away from UI components
 */

@Singleton
class FilterStateManager @Inject constructor() {

    /**
     * Creates initial filter state with default values
     */
    fun createDefaultFilters(): FilterState = FilterState()

    /**
     * Applies category filter with business rules
     * Rule: Categories and Services are mutually exclusive
     */
    fun applyCategoryFilter(
        currentFilters: FilterState,
        categoryFilter: CategoryFilter
    ): FilterState =
        currentFilters.copy(
            categoryFilter = categoryFilter,
            serviceFilter = if (categoryFilter !is CategoryFilter.All) {
                ServiceFilter.All // Clear services when category is selected
            } else {
                currentFilters.serviceFilter
            },
        )

    /**
     * Applies service filter with business rules
     * Rule: Categories and Services are mutually exclusive
     */
    fun applyServiceFilter(
        currentFilters: FilterState,
        serviceFilter: ServiceFilter
    ): FilterState =
        currentFilters.copy(
            serviceFilter = serviceFilter,
            categoryFilter = if (serviceFilter !is ServiceFilter.All) {
                CategoryFilter.All // Clear categories when service is selected
            } else {
                currentFilters.categoryFilter
            },
        )

    /**
     * Applies sort filter - does not affect other filters
     */
    fun applySortFilter(
        currentFilters: FilterState,
        sortFilter: SortFilter
    ): FilterState = currentFilters.copy(sortFilter = sortFilter)

    /**
     * Resets all filters to default state
     */
    fun resetFilters(): FilterState = FilterState()

    /**
     * Gets selected category names for filtering
     */
    fun getSelectedCategories(filters: FilterState): List<String>? =
        when (val filter = filters.categoryFilter) {
            CategoryFilter.All -> null
            is CategoryFilter.Selected -> filter.categories.toList()
        }

    /**
     * Gets selected service names for filtering
     */
    fun getSelectedServiceNames(filters: FilterState): List<String>? =
        when (val filter = filters.serviceFilter) {
            ServiceFilter.All -> null
            is ServiceFilter.Selected -> filter.services.map { it.name }
        }

    /**
     * Gets selected services for UI display
     */
    fun getSelectedServices(filters: FilterState): List<Service> =
        when (val filter = filters.serviceFilter) {
            ServiceFilter.All -> emptyList()
            is ServiceFilter.Selected -> filter.services.toList()
        }

    /**
     * Gets current sort criteria
     */
    fun getCurrentSortBy(filters: FilterState): PromoCodeSortBy = (filters.sortFilter as SortFilter.Selected).sortBy

    /**
     * Toggles category in multi-selection
     */
    fun toggleCategory(
        currentFilters: FilterState,
        category: String
    ): FilterState {
        val newCategoryFilter = when (val filter = currentFilters.categoryFilter) {
            CategoryFilter.All -> CategoryFilter.Selected(setOf(category))
            is CategoryFilter.Selected -> filter.toggle(category)
        }
        return applyCategoryFilter(currentFilters, newCategoryFilter)
    }

    /**
     * Toggles service in multi-selection
     */
    fun toggleService(
        currentFilters: FilterState,
        service: Service
    ): FilterState {
        val newServiceFilter = when (val filter = currentFilters.serviceFilter) {
            ServiceFilter.All -> ServiceFilter.Selected(setOf(service))
            is ServiceFilter.Selected -> filter.toggle(service)
        }
        return applyServiceFilter(currentFilters, newServiceFilter)
    }

    /**
     * Validates filter state consistency
     */
    fun validateFilters(filters: FilterState): Boolean {
        // Rule 1: Categories and Services are mutually exclusive
        if (filters.categoryFilter is CategoryFilter.Selected &&
            filters.serviceFilter is ServiceFilter.Selected
        ) {
            return false
        }

        // Rule 2: If categories are selected, the list cannot be empty
        if (filters.categoryFilter is CategoryFilter.Selected &&
            filters.categoryFilter.categories.isEmpty()
        ) {
            return false
        }

        // Rule 3: If services are selected, the list cannot be empty
        if (filters.serviceFilter is ServiceFilter.Selected &&
            filters.serviceFilter.services.isEmpty()
        ) {
            return false
        }

        return true
    }
}
