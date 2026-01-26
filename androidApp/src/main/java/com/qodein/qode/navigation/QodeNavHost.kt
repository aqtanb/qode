package com.qodein.qode.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import co.touchlab.kermit.Logger
import com.qodein.core.ui.navigation.PostKeys
import com.qodein.core.ui.navigation.ServiceSelectionResult
import com.qodein.feature.auth.navigation.authSection
import com.qodein.feature.auth.navigation.navigateToAuthBottomSheet
import com.qodein.feature.block.navigation.blockSection
import com.qodein.feature.block.navigation.blockedUsersSection
import com.qodein.feature.block.navigation.navigateToBlockUserDialog
import com.qodein.feature.block.navigation.navigateToBlockedUsers
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
import com.qodein.feature.report.navigation.navigateToReport
import com.qodein.feature.report.navigation.reportSection
import com.qodein.feature.service.selection.navigation.navigateToServiceSelection
import com.qodein.feature.service.selection.navigation.serviceSelectionSection
import com.qodein.feature.settings.navigation.navigateToAbout
import com.qodein.feature.settings.navigation.navigateToLicenses
import com.qodein.feature.settings.navigation.navigateToSettings
import com.qodein.feature.settings.navigation.settingsSection
import com.qodein.qode.ui.QodeAppState
import com.qodein.shared.model.ContentType

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
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(700)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(700)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(700)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(700)) },
    ) {
        homeSection(
            onPromoCodeClick = { promocodeId ->
                navController.navigateToPromocodeDetail(promocodeId)
            },
            onShowServiceSelection = { initialServiceIds ->
                navController.navigateToServiceSelection(
                    initialServiceIds = initialServiceIds,
                    isSingleSelection = false,
                )
            },
            registerScrollState = appState::registerScrollState,
        )

        feedSection(
            onProfileClick = { navController.navigateToProfile() },
            onSettingsClick = { navController.navigateToSettings() },
            onPostClick = { postId ->
                navController.navigateToPostDetail(postId)
            },
            onNavigateToAuth = { authPromptAction ->
                navController.navigateToAuthBottomSheet(authPromptAction)
            },
            registerScrollState = appState::registerScrollState,
        )

        promocodeDetailSection(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAuth = { authPromptAction ->
                navController.navigateToAuthBottomSheet(authPromptAction)
            },
            onNavigateToReport = { reportedItemId, itemTitle, itemAuthor ->
                navController.navigateToReport(
                    reportedItemId = reportedItemId,
                    reportedItemType = ContentType.PROMOCODE,
                    itemTitle = itemTitle,
                    itemAuthor = itemAuthor,
                )
            },
            onNavigateToBlockUser = { userId, username, photoUrl ->
                navController.navigateToBlockUserDialog(userId, username, photoUrl, ContentType.PROMOCODE)
            },
        )

        profileSection(
            onBackClick = { navController.popBackStack() },
            onSignOut = { navController.popBackStack() },
            onNavigateToBlockedUsers = { navController.navigateToBlockedUsers() },
            onNavigateToPostDetail = { postId -> navController.navigateToPostDetail(postId) },
            onNavigateToPromocodeDetail = { promocodeId -> navController.navigateToPromocodeDetail(promocodeId) },
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
            onShowServiceSelection = { initialServiceId ->
                navController.navigateToServiceSelection(
                    initialServiceIds = initialServiceId?.let { setOf(it) } ?: emptySet(),
                    isSingleSelection = true,
                )
            },
        )

        postSubmissionSection(
            onNavigateBack = navController::popBackStack,
            onPostSubmitted = {
                val handle = navController.previousBackStackEntry?.savedStateHandle
                handle?.set(PostKeys.KEY_POST_SUBMITTED, true)
                navController.popBackStack()
            },
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
            onNavigateToReport = { reportedItemId, itemTitle, itemAuthor ->
                navController.navigateToReport(
                    reportedItemId = reportedItemId.value,
                    reportedItemType = ContentType.POST,
                    itemTitle = itemTitle,
                    itemAuthor = itemAuthor,
                )
            },
            onNavigateToBlockUser = { userId, username, photoUrl ->
                navController.navigateToBlockUserDialog(userId, username, photoUrl, ContentType.POST)
            },

        )

        reportSection(
            onNavigateBack = { navController.popBackStack() },
            onReportSubmitted = { contentType ->
                when (contentType) {
                    ContentType.PROMOCODE -> appState.navigateToTopLevelDestination(TopLevelDestination.HOME, triggerRefresh = true)
                    ContentType.POST -> {
                        // Pop back to PostDetail
                        navController.popBackStack()
                        // Now previousBackStackEntry is Feed
                        val handle = navController.previousBackStackEntry?.savedStateHandle
                        handle?.set(PostKeys.KEY_POST_REPORTED, true)
                        // Pop back to Feed
                        navController.popBackStack()
                    }
                }
            },
        )

        blockSection(
            onNavigateBack = { navController.popBackStack() },
            onUserBlocked = { contentType ->
                when (contentType) {
                    ContentType.PROMOCODE -> appState.navigateToTopLevelDestination(TopLevelDestination.HOME, triggerRefresh = true)
                    ContentType.POST -> {
                        // Pop back to PostDetail
                        navController.popBackStack()
                        // Now previousBackStackEntry is Feed
                        val handle = navController.previousBackStackEntry?.savedStateHandle
                        handle?.set(PostKeys.KEY_POST_AUTHOR_BLOCKED, true)
                        // Pop back to Feed
                        navController.popBackStack()
                    }
                }
            },
        )

        blockedUsersSection(
            onNavigateBack = { navController.popBackStack() },
        )

        serviceSelectionSection(
            onNavigateBack = { navController.popBackStack() },
            onResult = { selectedServiceIds ->
                Logger.d("QodeNavHost") { "serviceSelectionSection onResult called with ${selectedServiceIds.size} services" }
                val previousEntry = navController.previousBackStackEntry
                Logger.d("QodeNavHost") { "previousBackStackEntry: ${previousEntry?.destination?.route}" }
                Logger.d("QodeNavHost") { "previousBackStackEntry ID: ${previousEntry?.id}" }
                previousEntry
                    ?.savedStateHandle
                    ?.set(ServiceSelectionResult.KEY_SELECTED_SERVICE_IDS, selectedServiceIds.map { it.value })
                Logger.d("QodeNavHost") { "savedStateHandle.set completed" }
            },
        )
    }
}
