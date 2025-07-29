package com.qodein.qode.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.feature.auth.navigation.AuthBaseRoute
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
        selectedIcon = Icons.Filled.Home,
        unSelectedIcon = Icons.Outlined.Home,
        iconTextId = homeR.string.feature_home_title,
        titleTextId = R.string.app_name,
        route = HomeBaseRoute::class,
        baseRoute = HomeBaseRoute::class,
    ),
    CATALOG(
        selectedIcon = Icons.Filled.Search,
        unSelectedIcon = Icons.Outlined.Search,
        iconTextId = R.string.catalog_title,
        titleTextId = R.string.catalog_title,
        route = CatalogRoute::class,
        baseRoute = CatalogBaseRoute::class,
    ),
    HISTORY(
        selectedIcon = Icons.Filled.Favorite,
        unSelectedIcon = Icons.Outlined.FavoriteBorder,
        iconTextId = R.string.history_title,
        titleTextId = R.string.history_title,
        route = HistoryRoute::class,
        baseRoute = HistoryBaseRoute::class,
    ),
    MORE(
        selectedIcon = Icons.Filled.Menu,
        unSelectedIcon = Icons.Outlined.Menu,
        iconTextId = R.string.more_title,
        titleTextId = R.string.more_title,
        route = AuthBaseRoute::class,
        baseRoute = AuthBaseRoute::class,
    )
}
