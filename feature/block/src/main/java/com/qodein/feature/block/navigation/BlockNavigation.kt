package com.qodein.feature.block.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.qodein.feature.block.BlockConfirmationDialog
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.UserId
import kotlinx.serialization.Serializable

@Serializable
data class BlockUserDialogRoute(val userId: String, val username: String?, val photoUrl: String?, val contentType: ContentType)

fun NavController.navigateToBlockUserDialog(
    userId: UserId,
    username: String?,
    photoUrl: String?,
    contentType: ContentType,
    navOptions: NavOptions? = null
) {
    navigate(route = BlockUserDialogRoute(userId.value, username, photoUrl, contentType), navOptions)
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
            contentType = args.contentType,
            onNavigateBack = onNavigateBack,
            onUserBlocked = { onUserBlocked(args.contentType) },
        )
    }
}
