package com.qodein.qode.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.ui.R
import com.qodein.feature.home.navigation.HomeBaseRoute
import com.qodein.feature.post.navigation.FeedRoute
import kotlin.reflect.KClass

enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unSelectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
    val route: KClass<*>
) {
    HOME(
        selectedIcon = QodeIcons.HomeFilled,
        unSelectedIcon = QodeIcons.Home,
        iconTextId = R.string.ui_home,
        route = HomeBaseRoute::class,
    ),
    FEED(
        selectedIcon = QodeIcons.FeedFilled,
        unSelectedIcon = QodeIcons.Feed,
        iconTextId = R.string.ui_feed,
        route = FeedRoute::class,
    )
}
