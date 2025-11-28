package com.qodein.shared.model

/**
 * Base interface for all filter types
 */
sealed interface FilterState {
    val isEmpty: Boolean
}

/**
 * Service filter for content filtering
 */
sealed class ServiceFilter : FilterState {
    data object All : ServiceFilter() {
        override val isEmpty: Boolean = true
    }

    data class Selected(val services: Set<Service>) : ServiceFilter() {
        override val isEmpty: Boolean get() = services.isEmpty()

        fun contains(service: Service): Boolean = services.contains(service)
    }
}

/**
 * Sort filter for content ordering
 */
data class SortFilter(val sortBy: ContentSortBy) : FilterState {
    override val isEmpty: Boolean = false // Sort is always selected
}

/**
 * Complete filter state for a feature
 */
data class CompleteFilterState(
    val serviceFilter: ServiceFilter = ServiceFilter.All,
    val sortFilter: SortFilter = SortFilter(ContentSortBy.POPULARITY)
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
    fun applySortFilter(sortFilter: SortFilter): CompleteFilterState = copy(sortFilter = sortFilter)

    /**
     * Reset all filters to default state
     */
    fun reset(): CompleteFilterState = CompleteFilterState()
}
