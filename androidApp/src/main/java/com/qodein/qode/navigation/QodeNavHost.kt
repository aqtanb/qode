package com.qodein.qode.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import co.touchlab.kermit.Logger
import com.qodein.core.ui.AuthPromptAction
import com.qodein.core.ui.navigation.ServiceSelectionResult
import com.qodein.feature.auth.navigation.authSection
import com.qodein.feature.auth.navigation.navigateToAuthBottomSheet
import com.qodein.feature.block.navigation.blockSection
import com.qodein.feature.block.navigation.navigateToBlockUserDialog
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
import com.qodein.shared.model.UserId

@Composable
fun QodeNavHost(
    appState: QodeAppState,
    userId: UserId?,
    modifier: Modifier = Modifier
) {
    val navController = appState.navController
    val selectedTabDestination = appState.selectedTabDestination

    NavHost(
        navController = navController,
        startDestination = HomeBaseRoute,
        modifier = modifier,
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
            scrollStateRegistry = appState,
        )

        promocodeDetailSection(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAuth = { authPromptAction ->
                navController.navigateToAuthBottomSheet(authPromptAction)
            },
            onNavigateToReport = { reportedItemId, itemTitle, itemAuthor ->
                navController.navigateToReport(
                    reportedItemId = reportedItemId,
                    reportedItemType = ContentType.PROMO_CODE,
                    itemTitle = itemTitle,
                    itemAuthor = itemAuthor,
                )
            },
            onNavigateToBlockUser = { userId, username, photoUrl ->
                navController.navigateToBlockUserDialog(userId, username, photoUrl, ContentType.PROMO_CODE)
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
            onShowServiceSelection = { initialServiceId ->
                navController.navigateToServiceSelection(
                    initialServiceIds = initialServiceId?.let { setOf(it) } ?: emptySet(),
                    isSingleSelection = true,
                )
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

        reportSection(
            onNavigateBack = { navController.popBackStack() },
            onReportSubmitted = { contentType ->
                when (contentType) {
                    ContentType.PROMO_CODE -> appState.navigateToTopLevelDestination(TopLevelDestination.HOME, triggerRefresh = true)
                    ContentType.POST -> appState.navigateToTopLevelDestination(TopLevelDestination.FEED, triggerRefresh = true)
                    else -> {}
                }
            },
        )

        blockSection(
            onNavigateBack = { navController.popBackStack() },
            onUserBlocked = { contentType ->
                when (contentType) {
                    ContentType.PROMO_CODE -> appState.navigateToTopLevelDestination(TopLevelDestination.HOME, triggerRefresh = true)
                    ContentType.POST -> appState.navigateToTopLevelDestination(TopLevelDestination.FEED, triggerRefresh = true)
                    else -> {}
                }
            },
        )

        serviceSelectionSection(
            onNavigateBack = { navController.popBackStack() },
            onResult = { selectedServiceIds ->
                Logger.d("QodeNavHost") { "serviceSelectionSection onResult called with ${selectedServiceIds.size} services" }
                val previousEntry = navController.previousBackStackEntry
                Logger.d("QodeNavHost") { "previousBackStackEntry: ${previousEntry?.destination?.route}" }
                Logger.d("QodeNavHost") { "previousBackStackEntry ID: ${previousEntry?.id}" }
                Logger.d("QodeNavHost") {
                    "Full backstack: ${navController.currentBackStack.value.map { "${it.destination.route} (id=${it.id})" }}"
                }
                previousEntry
                    ?.savedStateHandle
                    ?.set(ServiceSelectionResult.KEY_SELECTED_SERVICE_IDS, selectedServiceIds.map { it.value })
                Logger.d("QodeNavHost") { "savedStateHandle.set completed" }
            },
        )
    }
}
