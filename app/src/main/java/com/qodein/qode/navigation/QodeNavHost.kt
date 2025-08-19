package com.qodein.qode.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.qodein.core.model.Language
import com.qodein.feature.auth.navigation.authSection
import com.qodein.feature.home.navigation.HomeBaseRoute
import com.qodein.feature.home.navigation.homeSection
import com.qodein.feature.inbox.navigation.inboxSection
import com.qodein.feature.profile.navigation.profileSection
import com.qodein.feature.promocode.navigation.submissionSection
import com.qodein.feature.search.navigation.searchSection
import com.qodein.feature.settings.navigation.settingsSection
import com.qodein.qode.navigation.TopLevelDestination
import com.qodein.qode.ui.QodeAppState

@Composable
fun QodeNavHost(
    appState: QodeAppState,
    userLanguage: Language,
    modifier: Modifier = Modifier
) {
    val navController = appState.navController
    // Capture the selectedTabDestination at composable level
    val selectedTabDestination = appState.selectedTabDestination ?: TopLevelDestination.HOME

    NavHost(
        navController = navController,
        startDestination = HomeBaseRoute,
        modifier = modifier,
    ) {
        homeSection(
            userLanguage = userLanguage,
            onPromoCodeClick = {},
            promoCodeDetail = {},
        )

        searchSection()

        inboxSection()

        profileSection(
            scrollState = appState.profileScrollState,
            onBackClick = {
                // Navigate back to the last top-level destination instead of just popping
                appState.navigateToTopLevelDestination(selectedTabDestination)
            },
            onSignOut = {
                appState.navigateToTopLevelDestination(TopLevelDestination.HOME)
            },
        )

        authSection(
            onAuthSuccess = {
                // Navigate back to the original tab after successful auth
                appState.navigateToTopLevelDestination(selectedTabDestination)
            },
            onBackClick = {
                // Navigate back to the last top-level destination instead of empty profile
                appState.navigateToTopLevelDestination(selectedTabDestination)
            },
        )

        submissionSection(
            onNavigateBack = {
                navController.popBackStack()
            },
        )

        settingsSection(
            onBackClick = {
                navController.popBackStack()
            },
        )
    }
}
