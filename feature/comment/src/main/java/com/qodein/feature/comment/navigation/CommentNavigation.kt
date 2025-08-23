package com.qodein.feature.comment.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.qodein.feature.comment.CommentScreen
import kotlinx.serialization.Serializable

/**
 * Navigation route for comment screen with type-safe parameters
 */
@Serializable
data class CommentRoute(
    val parentId: String,
    val parentType: String, // "post" or "promo_code"
    val postTitle: String? = null, // The title of the post being commented on
    val postContent: String? = null // The content of the post being commented on
)

/**
 * Base route for comment navigation graph
 */
@Serializable
data object CommentGraphRoute

/**
 * Navigation extensions for comment feature
 */
fun NavController.navigateToComments(
    parentId: String,
    parentType: String
) {
    navigate(CommentRoute(parentId = parentId, parentType = parentType))
}

/**
 * Navigation graph definition for comment feature
 */
fun NavGraphBuilder.commentScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit
) {
    composable<CommentRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<CommentRoute>()
        CommentScreen(
            parentId = route.parentId,
            parentType = route.parentType,
            postTitle = route.postTitle,
            postContent = route.postContent,
            onNavigateBack = onNavigateBack,
            onNavigateToProfile = onNavigateToProfile,
        )
    }
}
