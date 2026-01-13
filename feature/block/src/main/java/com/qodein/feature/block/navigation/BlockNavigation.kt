package com.qodein.feature.block.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.qodein.feature.block.blocked.BlockedUsersRoute
import com.qodein.feature.block.blocking.BlockConfirmationDialog
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.UserId
import kotlinx.serialization.Serializable

@Serializable
data class BlockUserDialogRoute(val userId: String, val username: String?, val photoUrl: String?, val contentType: ContentType)

@Serializable
object BlockedUsersRoute

fun NavController.navigateToBlockUserDialog(
    userId: UserId,
    username: String?,
    photoUrl: String?,
    contentType: ContentType,
    navOptions: NavOptions? = null
) {
    navigate(route = BlockUserDialogRoute(userId.value, username, photoUrl, contentType), navOptions)
}

fun NavController.navigateToBlockedUsers(navOptions: NavOptions? = null) {
    navigate(route = BlockedUsersRoute, navOptions)
}

fun NavGraphBuilder.blockSection(
    onNavigateBack: () -> Unit,
    onUserBlocked: (ContentType) -> Unit
) {
    dialog<BlockUserDialogRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<BlockUserDialogRoute>()
        BlockConfirmationDialog(
            userId = UserId(args.userId),
            username = args.username,
            photoUrl = args.photoUrl,
            onNavigateBack = onNavigateBack,
            onUserBlocked = { onUserBlocked(args.contentType) },
        )
    }
}

fun NavGraphBuilder.blockedUsersSection(onNavigateBack: () -> Unit) {
    composable<BlockedUsersRoute> {
        BlockedUsersRoute(
            onBackClick = onNavigateBack,
        )
    }
}
