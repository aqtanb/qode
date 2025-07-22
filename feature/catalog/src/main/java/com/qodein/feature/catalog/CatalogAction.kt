package com.qodein.feature.catalog

import com.qodein.core.ui.component.SortOption
import com.qodein.core.ui.model.Category
import com.qodein.core.ui.model.PromoCode
import com.qodein.core.ui.model.Store

sealed interface CatalogAction {
    // search
    data class SearchQueryChanged(val query: String) : CatalogAction
    data object SearchSubmitted : CatalogAction
    data object SearchCleared : CatalogAction
    data class SearchSuggestionSelected(val suggestion: String) : CatalogAction

    // filter
    data class QuickFilterSelected(val filterId: String) : CatalogAction
    data class CategoryFilterSelected(val categoryId: String?) : CatalogAction
    data class StoreFilterSelected(val storeId: String?) : CatalogAction
    data class SortOptionSelected(val sortOption: SortOption) : CatalogAction
    data object FiltersCleared : CatalogAction
    data object FiltersToggled : CatalogAction

    // promocode
    data class PromoCodeClicked(val promoCode: PromoCode) : CatalogAction
    data class PromoCodeUpvoted(val promoCode: PromoCode) : CatalogAction
    data class PromoCodeCopied(val promoCode: PromoCode) : CatalogAction

    // store
    data class StoreClicked(val store: Store) : CatalogAction
    data class StoreFollowToggled(val store: Store) : CatalogAction

    // category
    data class CategoryClicked(val category: Category) : CatalogAction
    data class CategoryFollowToggled(val category: Category) : CatalogAction

    // data loading
    data object Refresh : CatalogAction
    data object LoadMore : CatalogAction
    data object RetryLoad : CatalogAction

    // navigation
    data object MenuClicked : CatalogAction
    data object BackPressed : CatalogAction

    // ui state
    data class ErrorDismissed(val errorId: String) : CatalogAction
    data object SnackbarDismissed : CatalogAction
}
