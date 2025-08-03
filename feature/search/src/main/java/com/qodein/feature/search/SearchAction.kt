package com.qodein.feature.search

import com.qodein.core.ui.component.SortOption
import com.qodein.core.ui.model.Category
import com.qodein.core.ui.model.PromoCode
import com.qodein.core.ui.model.Store

sealed interface SearchAction {
    // search
    data class SearchQueryChanged(val query: String) : SearchAction
    data object SearchSubmitted : SearchAction
    data object SearchCleared : SearchAction
    data class SearchSuggestionSelected(val suggestion: String) : SearchAction

    // filter
    data class QuickFilterSelected(val filterId: String) : SearchAction
    data class CategoryFilterSelected(val categoryId: String?) : SearchAction
    data class StoreFilterSelected(val storeId: String?) : SearchAction
    data class SortOptionSelected(val sortOption: SortOption) : SearchAction
    data object FiltersCleared : SearchAction
    data object FiltersToggled : SearchAction

    // promo code
    data class PromoCodeClicked(val promoCode: PromoCode) : SearchAction
    data class PromoCodeUpvoted(val promoCode: PromoCode) : SearchAction
    data class PromoCodeCopied(val promoCode: PromoCode) : SearchAction

    // store
    data class StoreClicked(val store: Store) : SearchAction
    data class StoreFollowToggled(val store: Store) : SearchAction

    // category
    data class CategoryClicked(val category: Category) : SearchAction
    data class CategoryFollowToggled(val category: Category) : SearchAction

    // data loading
    data object Refresh : SearchAction
    data object LoadMore : SearchAction
    data object RetryLoad : SearchAction

    // navigation
    data object MenuClicked : SearchAction
    data object BackPressed : SearchAction

    // ui state
    data class ErrorDismissed(val errorId: String) : SearchAction
    data object SnackbarDismissed : SearchAction
}
