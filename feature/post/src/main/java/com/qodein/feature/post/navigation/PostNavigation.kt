package com.qodein.feature.post.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.qodein.core.ui.AuthPromptAction
import com.qodein.feature.post.detail.PostDetailRoute
import com.qodein.feature.post.feed.FeedRoute
import com.qodein.feature.post.submission.PostSubmissionScreen
import com.qodein.shared.model.UserId
import kotlinx.serialization.Serializable

/**
 * Feed navigation routes using type-safe navigation with Kotlin Serialization
 */
@Serializable data object FeedRoute

@Serializable data object PostSubmissionRoute

@Serializable data class PostDetailRoute(val postId: String)

fun NavController.navigateToPostSubmission(navOptions: NavOptions? = null) {
    navigate(route = PostSubmissionRoute, navOptions)
}

fun NavController.navigateToPostDetail(
    postId: String,
    navOptions: NavOptions? = null
) {
    navigate(route = PostDetailRoute(postId), navOptions)
}

/**
 * Navigation graph builder extension for feed feature
 */
fun NavGraphBuilder.feedSection(
    userId: UserId?,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPostClick: (String) -> Unit
) {
    composable<FeedRoute> {
        FeedRoute(
            userId = userId,
            onProfileClick = onProfileClick,
            onSettingsClick = onSettingsClick,
            onPostClick = onPostClick,
        )
    }
}

fun NavGraphBuilder.postSubmissionSection(
    onNavigateBack: () -> Unit,
    onNavigateToAuth: (AuthPromptAction) -> Unit
) {
    composable<PostSubmissionRoute> {
        PostSubmissionScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToAuth = onNavigateToAuth,
        )
    }
}

fun NavGraphBuilder.postDetailSection(
    onNavigateBack: () -> Unit,
    onNavigateToAuth: (AuthPromptAction) -> Unit
) {
    composable<PostDetailRoute> {
        PostDetailRoute(
            onNavigateBack = onNavigateBack,
            onNavigateToAuth = onNavigateToAuth,
        )
    }
}
