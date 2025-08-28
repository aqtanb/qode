package com.qodein.feature.home.model

import androidx.compose.runtime.Stable
import com.qodein.shared.domain.repository.PromoCodeSortBy

@Stable
sealed class SortFilter {
    data class Selected(val sortBy: PromoCodeSortBy) : SortFilter()
}
