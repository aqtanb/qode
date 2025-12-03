package com.qodein.qode.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.feature.home.navigation.HomeBaseRoute
import com.qodein.feature.post.navigation.FeedRoute
import kotlin.reflect.KClass
import com.qodein.feature.home.R as homeR
import com.qodein.feature.post.R as postR

enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unSelectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
    val route: KClass<*>
) {
    HOME(
        selectedIcon = QodeIcons.HomeFilled,
        unSelectedIcon = QodeIcons.Home,
        iconTextId = homeR.string.feature_home_title,
        route = HomeBaseRoute::class,
    ),
    FEED(
        selectedIcon = QodeIcons.FeedFilled,
        unSelectedIcon = QodeIcons.Feed,
        iconTextId = postR.string.feed_title,
        route = FeedRoute::class,
    )
}
