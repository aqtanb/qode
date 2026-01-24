package com.qodein.feature.post.detail.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.component.QodeinBackIconButton
import com.qodein.core.designsystem.component.QodeinDropdownMenuGrouped
import com.qodein.core.designsystem.component.QodeinMenuGroup
import com.qodein.core.designsystem.component.QodeinMenuItem
import com.qodein.core.designsystem.component.QodeinMoreIconButton
import com.qodein.core.designsystem.component.QodeinTopAppBar
import com.qodein.core.designsystem.icon.NavigationIcons
import com.qodein.core.designsystem.icon.UIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.shared.model.PostId
import com.qodein.shared.model.UserId
import com.qodein.core.ui.R as CoreUiR

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PostDetailTopAppBar(
    title: String,
    postId: PostId?,
    currentUserId: UserId?,
    authorId: UserId?,
    onNavigateBack: () -> Unit,
    onBlockUserClick: (UserId) -> Unit,
    onReportPostClick: (PostId) -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val isOwnPost = currentUserId != null && currentUserId == authorId

    val groups = buildList {
        if (!isOwnPost) {
            add(
                QodeinMenuGroup(
                    listOf(
                        QodeinMenuItem(
                            text = stringResource(CoreUiR.string.action_block),
                            onClick = {
                                val userId = authorId ?: return@QodeinMenuItem
                                onBlockUserClick(userId)
                            },
                            leadingIcon = UIIcons.Block,
                            trailingIcon = NavigationIcons.ChevronRight,
                            enabled = authorId != null,
                        ),
                        QodeinMenuItem(
                            text = stringResource(CoreUiR.string.action_report),
                            onClick = {
                                val id = postId ?: return@QodeinMenuItem
                                onReportPostClick(id)
                            },
                            leadingIcon = UIIcons.Report,
                            trailingIcon = NavigationIcons.ChevronRight,
                        ),
                    ),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            )
        }
    }

    QodeinTopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = { QodeinBackIconButton(onClick = onNavigateBack) },
        actions = {
            if (groups.isNotEmpty()) {
                Box {
                    QodeinMoreIconButton(onClick = { isMenuExpanded = true })
                    QodeinDropdownMenuGrouped(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false },
                        groups = groups,
                    )
                }
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun PostDetailTopAppBarPreview() {
    QodeTheme {
        Surface {
            PostDetailTopAppBar(
                title = "Post Title",
                postId = PostId("test-id"),
                currentUserId = UserId("user-1"),
                authorId = UserId("author-1"),
                onNavigateBack = {},
                onBlockUserClick = {},
                onReportPostClick = {},
            )
        }
    }
}
