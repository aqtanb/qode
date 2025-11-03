package com.qodein.core.ui.component

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.ui.R
import com.qodein.shared.model.ContentSortBy

/**
 * Centralized helper for sort-related icons and titles across all features
 * Uses QodeinIcons for consistent iconography
 */
object SortIconHelper {

    /**
     * Gets icon for generic content sort type
     */
    fun getSortIcon(sortBy: ContentSortBy): ImageVector =
        when (sortBy) {
            ContentSortBy.POPULARITY -> QodeNavigationIcons.Trending
            ContentSortBy.NEWEST -> QodeNavigationIcons.History
            ContentSortBy.EXPIRING_SOON -> QodeNavigationIcons.Calendar
        }

    /**
     * Gets section title resource for sort type
     */
    @StringRes
    fun getSortSectionTitleRes(sortBy: ContentSortBy): Int =
        when (sortBy) {
            ContentSortBy.POPULARITY -> R.string.sort_section_title_popularity
            ContentSortBy.NEWEST -> R.string.sort_section_title_newest
            ContentSortBy.EXPIRING_SOON -> R.string.sort_section_title_expiring
        }
}
