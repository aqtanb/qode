package com.qodein.qode.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.qodein.feature.catalog.navigation.catalogSection
import com.qodein.feature.home.navigation.HomeBaseRoute
import com.qodein.feature.home.navigation.homeSection
import com.qodein.qode.ui.QodeAppState

@Composable
fun QodeNavHost(
    appState: QodeAppState,
    modifier: Modifier = Modifier
) {
    val navController = appState.navController
    NavHost(
        navController = navController,
        startDestination = HomeBaseRoute,
        modifier = modifier,
    ) {
        homeSection(
            onPromoCodeClick = {},
            promoCodeDetail = {},
        )

        catalogSection()

        historySection()

        moreSection()
    }
}
