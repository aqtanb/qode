package com.qodein.qode.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.qodein.core.ui.AuthPromptAction
import com.qodein.feature.auth.navigation.authSection
import com.qodein.feature.auth.navigation.navigateToAuthBottomSheet
import com.qodein.feature.home.navigation.HomeBaseRoute
import com.qodein.feature.home.navigation.homeSection
import com.qodein.feature.post.navigation.feedSection
import com.qodein.feature.post.navigation.navigateToPostDetail
import com.qodein.feature.post.navigation.postDetailSection
import com.qodein.feature.post.navigation.postSubmissionSection
import com.qodein.feature.profile.navigation.navigateToProfile
import com.qodein.feature.profile.navigation.profileSection
import com.qodein.feature.promocode.navigation.navigateToPromocodeDetail
import com.qodein.feature.promocode.navigation.promocodeDetailSection
import com.qodein.feature.promocode.navigation.promocodeSubmissionSection
import com.qodein.feature.settings.navigation.navigateToAbout
import com.qodein.feature.settings.navigation.navigateToLicenses
import com.qodein.feature.settings.navigation.navigateToSettings
import com.qodein.feature.settings.navigation.settingsSection
import com.qodein.qode.ui.QodeAppState
import com.qodein.shared.model.UserId

@Composable
fun QodeNavHost(
    appState: QodeAppState,
    userId: UserId?,
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
            onPromoCodeClick = { promocodeId ->
                navController.navigateToPromocodeDetail(promocodeId)
            },
            scrollStateRegistry = appState,
        )

        promocodeDetailSection(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAuth = { authPromptAction ->
                navController.navigateToAuthBottomSheet(authPromptAction)
            },
        )

        feedSection(
            userId = userId,
            onProfileClick = {
                if (userId != null) {
                    navController.navigateToProfile()
                } else {
                    navController.navigateToAuthBottomSheet(AuthPromptAction.Profile)
                }
            },
            onSettingsClick = { navController.navigateToSettings() },
            onPostClick = { postId ->
                navController.navigateToPostDetail(postId)
            },
        )

        profileSection(
            onBackClick = {
                appState.navigateToTopLevelDestination(selectedTabDestination)
            },
            onSignOut = {
                appState.navigateToTopLevelDestination(TopLevelDestination.HOME)
            },
        )

        authSection(
            navController = navController,
        )

        promocodeSubmissionSection(
            onNavigateBack = {
                navController.popBackStack()
            },
            onNavigateToAuth = { authPromptAction ->
                navController.navigateToAuthBottomSheet(authPromptAction)
            },
        )

        postSubmissionSection(
            onNavigateBack = navController::popBackStack,
            onNavigateToAuth = { authPromptAction ->
                navController.navigateToAuthBottomSheet(authPromptAction)
            },
        )

        settingsSection(
            onBackClick = {
                navController.popBackStack()
            },
            onNavigateToLicenses = {
                navController.navigateToLicenses()
            },
            onNavigateToAbout = {
                navController.navigateToAbout()
            },
        )

        postDetailSection(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAuth = { authPromptAction ->
                navController.navigateToAuthBottomSheet(authPromptAction)
            },
        )
    }
}
