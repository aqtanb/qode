package com.qodein.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.component.QodeColorScheme
import com.qodein.core.designsystem.icon.QodeCategoryIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
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
        Service.Companion.Categories.STREAMING to QodeCategoryIcons.Streaming,
        Service.Companion.Categories.FOOD to QodeCategoryIcons.Food,
        Service.Companion.Categories.TRANSPORT to QodeCategoryIcons.Bus,
        Service.Companion.Categories.SHOPPING to QodeCategoryIcons.Grocery,
        Service.Companion.Categories.GAMING to QodeCategoryIcons.Computers,
        Service.Companion.Categories.MUSIC to QodeCategoryIcons.Music,
        Service.Companion.Categories.EDUCATION to QodeCategoryIcons.Education,
        Service.Companion.Categories.FITNESS to QodeCategoryIcons.Fitness,
        Service.Companion.Categories.FINANCE to QodeCategoryIcons.Finance,
        Service.Companion.Categories.BEAUTY to QodeCategoryIcons.Cosmetics,
        Service.Companion.Categories.CLOTHING to QodeCategoryIcons.Clothing,
        Service.Companion.Categories.ELECTRONICS to QodeCategoryIcons.Electronics,
        Service.Companion.Categories.TRAVEL to QodeCategoryIcons.Travel,
        Service.Companion.Categories.PHARMACY to QodeCategoryIcons.Supplements,
        Service.Companion.Categories.JEWELRY to QodeCategoryIcons.Accessories,
        Service.Companion.Categories.HEALTH to QodeCategoryIcons.Medical,
        Service.Companion.Categories.ENTERTAINMENT to QodeCategoryIcons.Entertainment,
        Service.Companion.Categories.MARKETPLACE to QodeCategoryIcons.Investment,
        Service.Companion.Categories.SERVICES to QodeCategoryIcons.Services,
        Service.Companion.Categories.TELECOM to QodeCategoryIcons.Phones,
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
        when (promoCode) {
            is PromoCode.FixedAmountPromoCode -> QodeColorScheme.BannerPurple
            is PromoCode.PercentagePromoCode -> QodeColorScheme.BannerOrange
        }

    /**
     * Gets gradient colors for PromoCode based on type
     * Returns List<Color> for reusability across the app
     * Purple colors for fixed amount, Orange colors for percentage
     */
    fun getPromoCodeGradientColors(promoCode: PromoCode): List<Color> =
        when (promoCode) {
            is PromoCode.FixedAmountPromoCode -> listOf(
                Color(0xFF6C5CE7), // Purple start
                Color(0xFF8F4C34), // Brown end (from theme)
            )
            is PromoCode.PercentagePromoCode -> listOf(
                Color(0xFFFF6B35), // Orange start
                Color(0xFFE74C3C), // Reddish end
            )
        }
}
