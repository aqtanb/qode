package com.qodein.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.component.QodeColorScheme
import com.qodein.core.designsystem.icon.QodeCategoryIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.shared.model.Discount
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.Service
import kotlin.collections.get

/**
 * Android-specific category icon mapping utility.
 * Maps category strings to appropriate Material/QodeIcons.
 */
object CategoryIconHelper {

    /**
     * Category to icon mapping using existing QodeIcons
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

    /**
     * Checks if category has icon mapping
     */
    fun hasIcon(category: String?): Boolean = CATEGORY_ICONS.containsKey(category)

    /**
     * Gets gradient color scheme for PromoCode based on type
     * Purple gradient for FixedAmountPromoCode, Orange gradient for PercentagePromoCode
     */
    fun getPromoCodeGradient(promoCode: PromoCode): QodeColorScheme =
        when (promoCode.discount) {
            is Discount.Percentage -> QodeColorScheme.BannerPurple
            is Discount.FixedAmount -> QodeColorScheme.BannerOrange
        }
}
