package com.qodein.shared.model

/**
 * Service filter for content filtering
 */
sealed interface ServiceFilter {
    val isEmpty: Boolean get() = this is All

    data object All : ServiceFilter

    data class Selected(val services: Set<Service>) : ServiceFilter
}

/**
 * Complete filter state for a feature
 */
data class CompleteFilterState(
    val serviceFilter: ServiceFilter = ServiceFilter.All,
    val sortBy: PromocodeSortBy = PromocodeSortBy.POPULARITY
) {
    /**
     * Apply service filter with validation
     * Categories and services are mutually exclusive
     */
    fun applyServiceFilter(serviceFilter: ServiceFilter): CompleteFilterState =
        if (serviceFilter is ServiceFilter.Selected && serviceFilter.services.isNotEmpty()) {
            // Clear category filter when service is selected
            copy(serviceFilter = serviceFilter)
        } else {
            copy(serviceFilter = serviceFilter)
        }

    /**
     * Apply sort filter
     */
    fun applySortBy(sortBy: PromocodeSortBy): CompleteFilterState = copy(sortBy = sortBy)

    /**
     * Reset all filters to default state
     */
    fun reset(): CompleteFilterState = CompleteFilterState()
}
