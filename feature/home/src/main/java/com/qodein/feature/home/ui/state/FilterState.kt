package com.qodein.feature.home.ui.state

import androidx.compose.runtime.Stable
import com.qodein.feature.home.model.CategoryFilter
import com.qodein.feature.home.model.ServiceFilter
import com.qodein.feature.home.model.SortFilter
import com.qodein.shared.domain.repository.PromoCodeSortBy

@Stable
data class FilterState(
    val categoryFilter: CategoryFilter = CategoryFilter.All,
    val serviceFilter: ServiceFilter = ServiceFilter.All,
    val sortFilter: SortFilter = SortFilter.Selected(PromoCodeSortBy.POPULARITY)
) {
    val hasActiveFilters: Boolean
        get() = categoryFilter !is CategoryFilter.All ||
            serviceFilter !is ServiceFilter.All

    fun reset(): FilterState = FilterState()
}
