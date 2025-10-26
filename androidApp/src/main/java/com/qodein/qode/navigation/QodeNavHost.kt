package com.qodein.qode.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.qodein.feature.auth.navigation.authSection
import com.qodein.feature.home.navigation.HomeBaseRoute
import com.qodein.feature.home.navigation.homeSection
import com.qodein.feature.post.navigation.feedSection
import com.qodein.feature.post.navigation.postSubmissionSection
import com.qodein.feature.profile.navigation.navigateToProfile
import com.qodein.feature.profile.navigation.profileSection
import com.qodein.feature.promocode.navigation.navigateToPromocodeDetail
import com.qodein.feature.promocode.navigation.promocodeSubmissionSection
import com.qodein.feature.settings.navigation.navigateToSettings
import com.qodein.feature.settings.navigation.settingsSection
import com.qodein.qode.ui.QodeAppState
import com.qodein.shared.model.Language
import com.qodein.shared.model.User

@Composable
fun QodeNavHost(
    appState: QodeAppState,
    userLanguage: Language,
    user: User?,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean
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
            onPromoCodeClick = { promoCode ->
                navController.navigateToPromocodeDetail(promoCode.id)
            },
            promoCodeDetail = { promoCodeId ->
                navController.navigateToPromocodeDetail(promoCodeId)
            },
            scrollStateRegistry = appState,
        )

        feedSection(
            user = user,
            onProfileClick = { navController.navigateToProfile() },
            onSettingsClick = { navController.navigateToSettings() },
        )

        profileSection(
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
            isDarkTheme = isDarkTheme,
        )

        promocodeSubmissionSection(
            onNavigateBack = {
                navController.popBackStack()
            },
            isDarkTheme = isDarkTheme,
        )

        postSubmissionSection(
            onNavigateBack = navController::popBackStack,
            isDarkTheme = isDarkTheme,
        )

        settingsSection(
            onBackClick = {
                navController.popBackStack()
            },
        )
    }
}
