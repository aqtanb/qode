package com.qodein.feature.home.model

import androidx.compose.runtime.Stable
import com.qodein.shared.domain.repository.PromoCodeSortBy
import com.qodein.shared.model.Service

@Stable
sealed class PromoCodeTypeFilter {
    data object All : PromoCodeTypeFilter()
    data object Percentage : PromoCodeTypeFilter()
    data object FixedAmount : PromoCodeTypeFilter()
}

@Stable
sealed class CategoryFilter {
    data object All : CategoryFilter()
    data class Selected(val category: String) : CategoryFilter()
}

@Stable
sealed class ServiceFilter {
    data object All : ServiceFilter()
    data class Selected(val service: Service) : ServiceFilter()
}

@Stable
sealed class SortFilter {
    data class Selected(val sortBy: PromoCodeSortBy) : SortFilter()
}

@Stable
data class FilterState(
    val typeFilter: PromoCodeTypeFilter = PromoCodeTypeFilter.All,
    val categoryFilter: CategoryFilter = CategoryFilter.All,
    val serviceFilter: ServiceFilter = ServiceFilter.All,
    val sortFilter: SortFilter = SortFilter.Selected(PromoCodeSortBy.POPULARITY)
) {
    val hasActiveFilters: Boolean
        get() = typeFilter !is PromoCodeTypeFilter.All ||
            categoryFilter !is CategoryFilter.All ||
            serviceFilter !is ServiceFilter.All

    fun reset(): FilterState = FilterState()
}

@Stable
sealed class FilterDialogType {
    data object Type : FilterDialogType()
    data object Category : FilterDialogType()
    data object Service : FilterDialogType()
    data object Sort : FilterDialogType()
}
