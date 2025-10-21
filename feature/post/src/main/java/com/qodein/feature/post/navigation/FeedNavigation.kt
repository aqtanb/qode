package com.qodein.feature.post.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.qodein.feature.post.feed.FeedScreen
import com.qodein.feature.post.submission.PostSubmissionScreen
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
fun NavGraphBuilder.feedSection() {
    composable<FeedRoute> {
        FeedScreen()
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
