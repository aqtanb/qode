package com.qodein.qode.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.qodein.feature.auth.navigation.authSection
import com.qodein.feature.home.navigation.HomeBaseRoute
import com.qodein.feature.home.navigation.homeSection
import com.qodein.feature.inbox.navigation.inboxSection
import com.qodein.feature.search.navigation.searchSection
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

        searchSection()

        inboxSection()

        authSection()
    }
}
