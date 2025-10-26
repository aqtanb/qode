package com.qodein.feature.post.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.qodein.feature.post.feed.FeedRoute
import com.qodein.feature.post.submission.PostSubmissionScreen
import com.qodein.shared.model.User
import kotlinx.serialization.Serializable

/**
 * Feed navigation routes using type-safe navigation with Kotlin Serialization
 */
@Serializable object FeedRoute

@Serializable object PostSubmissionRoute

/**
 * Extension function to navigate to feed screen
 */
fun NavController.navigateToFeed(navOptions: NavOptions? = null) {
    navigate(route = FeedRoute, navOptions)
}

fun NavController.navigateToPostSubmission(navOptions: NavOptions? = null) {
    navigate(route = PostSubmissionRoute, navOptions)
}

/**
 * Navigation graph builder extension for feed feature
 */
fun NavGraphBuilder.feedSection(
    user: User?,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    composable<FeedRoute> {
        FeedRoute(
            user = user,
            onProfileClick = onProfileClick,
            onSettingsClick = onSettingsClick,
        )
    }
}

fun NavGraphBuilder.postSubmissionSection(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean
) {
    composable<PostSubmissionRoute> {
        PostSubmissionScreen(onNavigateBack = onNavigateBack, isDarkTheme = isDarkTheme)
    }
}
