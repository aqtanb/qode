package com.qodein.core.ui.component

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.UIIcons
import com.qodein.core.ui.R
import com.qodein.shared.model.PromocodeSortBy

/**
 * Centralized helper for sort-related icons and titles across all features
 * Uses QodeinIcons for consistent iconography
 */
object SortIconHelper {

    /**
     * Gets icon for generic content sort type
     */
    fun getSortIcon(sortBy: PromocodeSortBy): ImageVector =
        when (sortBy) {
            PromocodeSortBy.POPULARITY -> UIIcons.Popular
            PromocodeSortBy.NEWEST -> UIIcons.Newest
            PromocodeSortBy.EXPIRING_SOON -> UIIcons.Expiring
        }

    /**
     * Gets section title resource for sort type
     */
    @StringRes
    fun getSortSectionTitleRes(sortBy: PromocodeSortBy): Int =
        when (sortBy) {
            PromocodeSortBy.POPULARITY -> R.string.sort_section_title_popularity
            PromocodeSortBy.NEWEST -> R.string.sort_section_title_newest
            PromocodeSortBy.EXPIRING_SOON -> R.string.sort_section_title_expiring
        }
}
