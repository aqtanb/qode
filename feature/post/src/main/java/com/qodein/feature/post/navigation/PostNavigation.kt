package com.qodein.feature.post.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.qodein.core.ui.AuthPromptAction
import com.qodein.feature.post.detail.PostDetailRoute
import com.qodein.feature.post.feed.FeedRoute
import com.qodein.feature.post.submission.PostSubmissionScreen
import com.qodein.shared.model.PostId
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
    postId: PostId,
    navOptions: NavOptions? = null
) {
    navigate(route = PostDetailRoute(postId.value), navOptions)
}

/**
 * Navigation graph builder extension for feed feature
 */
fun NavGraphBuilder.feedSection(
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPostClick: (PostId) -> Unit,
    onNavigateToAuth: (AuthPromptAction) -> Unit
) {
    composable<FeedRoute> { backStackEntry ->
        FeedRoute(
            onProfileClick = onProfileClick,
            onSettingsClick = onSettingsClick,
            onPostClick = onPostClick,
            onNavigateToAuth = onNavigateToAuth,
            backStackEntry = backStackEntry,
        )
    }
}

fun NavGraphBuilder.postSubmissionSection(
    onNavigateBack: () -> Unit,
    onPostSubmitted: () -> Unit,
    onNavigateToAuth: (AuthPromptAction) -> Unit
) {
    composable<PostSubmissionRoute> {
        PostSubmissionScreen(
            onNavigateBack = onNavigateBack,
            onPostSubmitted = onPostSubmitted,
            onNavigateToAuth = onNavigateToAuth,
        )
    }
}

fun NavGraphBuilder.postDetailSection(
    onNavigateBack: () -> Unit,
    onNavigateToAuth: (AuthPromptAction) -> Unit,
    onNavigateToReport: (String, String, String?) -> Unit,
    onNavigateToBlockUser: (UserId, String?, String?) -> Unit
) {
    composable<PostDetailRoute> {
        PostDetailRoute(
            onNavigateBack = onNavigateBack,
            onNavigateToAuth = onNavigateToAuth,
            onNavigateToReport = onNavigateToReport,
            onNavigateToBlockUser = onNavigateToBlockUser,
        )
    }
}
