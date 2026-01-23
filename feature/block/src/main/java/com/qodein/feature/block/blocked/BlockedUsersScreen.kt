package com.qodein.feature.block.blocked

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.component.ButtonSize
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeinBackIconButton
import com.qodein.core.designsystem.component.QodeinOutlinedCard
import com.qodein.core.designsystem.component.QodeinTopAppBar
import com.qodein.core.designsystem.icon.UIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.EmptyState
import com.qodein.core.ui.component.ProfileAvatar
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.preview.UserPreviewData
import com.qodein.feature.block.R
import com.qodein.shared.model.User
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.koin.androidx.compose.koinViewModel

@Composable
fun BlockedUsersRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BlockedUsersViewModel = koinViewModel()
) {
    TrackScreenViewEvent(screenName = "BlockedUsers")
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BlockedUsersScreen(
        onBackClick = onBackClick,
        onAction = viewModel::onAction,
        uiState = uiState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlockedUsersScreen(
    onBackClick: () -> Unit,
    onAction: (BlockedUsersAction) -> Unit,
    uiState: BlockedUsersUiState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            QodeinTopAppBar(
                title = stringResource(R.string.blocked_users_title),
                navigationIcon = { QodeinBackIconButton({ onBackClick() }) },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when (uiState) {
                is BlockedUsersUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is BlockedUsersUiState.Success -> {
                    if (uiState.blockedUsers.isEmpty()) {
                        EmptyState(
                            icon = UIIcons.Block,
                            title = stringResource(R.string.blocked_users_empty),
                            description = stringResource(R.string.blocked_users_empty_description),
                        )
                    } else {
                        BlockedUsersList(
                            blockedUsers = uiState.blockedUsers,
                            hasMore = uiState.hasMore,
                            isLoadingMore = uiState.isLoadingMore,
                            onUnblockClick = { user ->
                                onAction(BlockedUsersAction.ShowConfirmationDialog(user))
                            },
                            onLoadMore = { onAction(BlockedUsersAction.LoadMoreUsers) },
                        )
                        val dialogState = uiState.dialogState
                        if (dialogState is UnblockDialogState.Visible) {
                            UnblockConfirmationDialog(
                                username = dialogState.user.profile.displayName ?: "",
                                photoUrl = dialogState.user.profile.photoUrl,
                                isLoading = dialogState is UnblockDialogState.Loading,
                                onConfirm = { onAction(BlockedUsersAction.ConfirmUnblock) },
                                onCancel = { onAction(BlockedUsersAction.DismissConfirmationDialog) },
                            )
                        }
                    }
                }

                is BlockedUsersUiState.Error -> {
                    QodeErrorCard(
                        error = uiState.error,
                        onRetry = { onAction(BlockedUsersAction.RetryLoadingUsers) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockedUsersList(
    blockedUsers: List<User>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onUnblockClick: (User) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState, hasMore, isLoadingMore) {
        snapshotFlow {
            if (hasMore && !isLoadingMore) {
                val layoutInfo = listState.layoutInfo
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()

                if (lastVisibleItem == null || totalItems == 0) {
                    false
                } else {
                    lastVisibleItem.index >= totalItems - 1
                }
            } else {
                false
            }
        }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                onLoadMore()
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(SpacingTokens.md),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        items(
            items = blockedUsers,
            key = { it.id.value },
        ) { user ->
            BlockedUserItem(
                user = user,
                onUnblockClick = { onUnblockClick(user) },
            )
        }

        if (hasMore && isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SpacingTokens.md),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun BlockedUserItem(
    user: User,
    onUnblockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeinOutlinedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = {},
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                modifier = Modifier.weight(1f),
            ) {
                ProfileAvatar(
                    user = user,
                    size = SizeTokens.Avatar.sizeMedium,
                )

                Spacer(modifier = Modifier.width(SpacingTokens.sm))

                Text(
                    text = user.displayName ?: stringResource(R.string.anonymous_user),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            QodeButton(
                text = stringResource(R.string.unblock_button),
                onClick = onUnblockClick,
                size = ButtonSize.Small,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

// MARK: Previews

@PreviewLightDark
@Composable
private fun BlockedUsersScreenLoadingPreview() {
    QodeTheme {
        BlockedUsersScreen(
            onBackClick = {},
            onAction = {},
            uiState = BlockedUsersUiState.Loading,
        )
    }
}

@PreviewLightDark
@Composable
private fun BlockedUsersScreenEmptyPreview() {
    QodeTheme {
        BlockedUsersScreen(
            onBackClick = {},
            onAction = {},
            uiState = BlockedUsersUiState.Success(
                blockedUsers = emptyList(),
                hasMore = false,
                nextCursor = null,
                isLoadingMore = false,
                dialogState = UnblockDialogState.Hidden,
            ),
        )
    }
}

@PreviewLightDark
@Composable
private fun BlockedUsersScreenSuccessPreview() {
    QodeTheme {
        BlockedUsersScreen(
            onBackClick = {},
            onAction = {},
            uiState = BlockedUsersUiState.Success(
                blockedUsers = listOf(
                    UserPreviewData.newUser,
                    UserPreviewData.powerUser,
                    UserPreviewData.activeContributor,
                ),
                hasMore = true,
                nextCursor = null,
                isLoadingMore = false,
                dialogState = UnblockDialogState.Idle(user = UserPreviewData.newUser),
            ),
        )
    }
}
