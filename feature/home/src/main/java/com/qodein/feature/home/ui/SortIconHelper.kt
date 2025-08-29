package com.qodein.feature.home.ui

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.feature.home.R
import com.qodein.shared.domain.repository.PromoCodeSortBy

/**
 * Helper for sort-related icons and titles in the home feature
 */
object SortIconHelper {

    /**
     * Gets icon for sort type
     */
    fun getSortIcon(sortBy: PromoCodeSortBy): ImageVector =
        when (sortBy) {
            PromoCodeSortBy.POPULARITY -> QodeNavigationIcons.Trending
            PromoCodeSortBy.NEWEST -> QodeNavigationIcons.History
            PromoCodeSortBy.EXPIRING_SOON -> QodeNavigationIcons.Calendar
        }

    /**
     * Gets section title resource for sort type
     */
    @StringRes
    fun getSortSectionTitleRes(sortBy: PromoCodeSortBy): Int =
        when (sortBy) {
            PromoCodeSortBy.POPULARITY -> R.string.home_section_title_popularity
            PromoCodeSortBy.NEWEST -> R.string.home_section_title_newest
            PromoCodeSortBy.EXPIRING_SOON -> R.string.home_section_title_expiring
        }
}
