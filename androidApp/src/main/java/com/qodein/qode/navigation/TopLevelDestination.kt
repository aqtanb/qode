package com.qodein.qode.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.QodeCategoryIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.feature.feed.navigation.FeedRoute
import com.qodein.feature.home.navigation.HomeBaseRoute
import com.qodein.qode.R
import kotlin.reflect.KClass
import com.qodein.feature.home.R as homeR

enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unSelectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
    @StringRes val titleTextId: Int,
    val route: KClass<*>,
    val baseRoute: KClass<*> = route
) {
    HOME(
        selectedIcon = QodeCommerceIcons.Coupon,
        unSelectedIcon = QodeCommerceIcons.Coupon,
        iconTextId = homeR.string.feature_home_title,
        titleTextId = R.string.app_name,
        route = HomeBaseRoute::class,
        baseRoute = HomeBaseRoute::class,
    ),
    FEED(
        selectedIcon = QodeCategoryIcons.Consulting,
        unSelectedIcon = QodeCategoryIcons.Consulting,
        iconTextId = R.string.feed_title,
        titleTextId = R.string.feed_title,
        route = FeedRoute::class,
    )
}
