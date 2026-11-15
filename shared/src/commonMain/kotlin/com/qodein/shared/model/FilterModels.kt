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
     * Whether anything narrows the results. Sort alone doesn't count.
     */
    val hasActiveFilters: Boolean get() = serviceFilter !is ServiceFilter.All

    val selectedServiceIds: Set<ServiceId>
        get() = (serviceFilter as? ServiceFilter.Selected)?.services?.map { it.id }?.toSet().orEmpty()

    fun applyServiceFilter(serviceFilter: ServiceFilter): CompleteFilterState = copy(serviceFilter = serviceFilter)

    /**
     * Adds [service] to the selection, or removes it if it was already selected.
     */
    fun toggleService(service: Service): CompleteFilterState {
        val current = (serviceFilter as? ServiceFilter.Selected)?.services.orEmpty()
        val next = if (current.any { it.id == service.id }) current.filterNot { it.id == service.id }.toSet() else current + service
        return copy(serviceFilter = if (next.isEmpty()) ServiceFilter.All else ServiceFilter.Selected(next))
    }

    fun applySortBy(sortBy: PromocodeSortBy): CompleteFilterState = copy(sortBy = sortBy)

    /**
     * Clears every filter but keeps the sort.
     */
    fun clearFilters(): CompleteFilterState = CompleteFilterState(sortBy = sortBy)

    /**
     * Reset all filters to default state
     */
    fun reset(): CompleteFilterState = CompleteFilterState()
}
