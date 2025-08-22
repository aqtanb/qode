package com.qodein.qode.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.feature.feed.navigation.FeedRoute
import com.qodein.feature.home.navigation.HomeBaseRoute
import com.qodein.feature.inbox.navigation.InboxBaseRoute
import com.qodein.feature.inbox.navigation.InboxRoute
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
        selectedIcon = Icons.Filled.Home,
        unSelectedIcon = Icons.Outlined.Home,
        iconTextId = homeR.string.feature_home_title,
        titleTextId = R.string.app_name,
        route = HomeBaseRoute::class,
        baseRoute = HomeBaseRoute::class,
    ),
    SEARCH(
        selectedIcon = Icons.Filled.Search,
        unSelectedIcon = Icons.Outlined.Search,
        iconTextId = R.string.search_title,
        titleTextId = R.string.search_title,
        route = FeedRoute::class,
    ),
    INBOX(
        selectedIcon = Icons.Filled.Inbox,
        unSelectedIcon = Icons.Outlined.Inbox,
        iconTextId = R.string.inbox_title,
        titleTextId = R.string.inbox_title,
        route = InboxRoute::class,
        baseRoute = InboxBaseRoute::class,
    )
}
