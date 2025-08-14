package com.qodein.feature.home

import androidx.compose.runtime.Immutable
import com.qodein.core.model.PromoCode
import com.qodein.core.ui.component.HeroBannerItem

/**
 * UI state for the Home screen
 */
@Immutable
data class HomeUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isLoggedIn: Boolean = false,
    val bannerItems: List<HeroBannerItem> = emptyList(),
    val promoCodes: List<PromoCode> = emptyList(),
    val hasMorePromoCodes: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null,
    val isRefreshing: Boolean = false
) {
    // Indicates if the screen is in an empty state
    val isEmpty: Boolean
        get() = !isLoading && promoCodes.isEmpty() && error == null

    // Indicates if the screen has content to display
    val hasContent: Boolean
        get() = promoCodes.isNotEmpty() || bannerItems.isNotEmpty()

    // Indicates if there's an error state
    val hasError: Boolean
        get() = error != null

    // Indicates if there's a success message to show
    val hasSuccessMessage: Boolean
        get() = successMessage != null
}
