package com.qodein.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.QodeCategoryIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.shared.model.Service
import kotlin.collections.get

/**
 * Android-specific category icon mapping utility.
 * Maps category strings to appropriate Material/QodeinIcons.
 */
object CategoryIconHelper {

    /**
     * Category to icon mapping using existing QodeinIcons
     */
    private val CATEGORY_ICONS: Map<String, ImageVector> = mapOf(
        Service.Companion.Categories.FOOD to QodeCategoryIcons.Food,
        Service.Companion.Categories.TRANSPORT to QodeCategoryIcons.Bus,
        Service.Companion.Categories.SHOPPING to QodeCategoryIcons.Grocery,
        Service.Companion.Categories.EDUCATION to QodeCategoryIcons.Education,
        Service.Companion.Categories.FITNESS to QodeCategoryIcons.Fitness,
        Service.Companion.Categories.BEAUTY to QodeCategoryIcons.Cosmetics,
        Service.Companion.Categories.CLOTHING to QodeCategoryIcons.Shirt,
        Service.Companion.Categories.ELECTRONICS to QodeCategoryIcons.Electronics,
        Service.Companion.Categories.TRAVEL to QodeCategoryIcons.Travel,
        Service.Companion.Categories.JEWELRY to QodeCategoryIcons.Accessories,
        Service.Companion.Categories.ENTERTAINMENT to QodeCategoryIcons.Entertainment,
        Service.Companion.Categories.OTHER to QodeCommerceIcons.Order,
        Service.Companion.Categories.UNSPECIFIED to QodeNavigationIcons.Help,
    )

    /**
     * Gets icon for category
     */
    @Composable
    fun getCategoryIcon(category: String?): ImageVector = CATEGORY_ICONS[category] ?: CATEGORY_ICONS[Service.Companion.Categories.OTHER]!!
}
