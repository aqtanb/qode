package com.qodein.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.component.QodeinFab
import com.qodein.core.designsystem.icon.NavigationIcons
import com.qodein.core.designsystem.icon.PostIcons
import com.qodein.core.designsystem.icon.PromocodeIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.EmptyState
import com.qodein.core.ui.component.ProfileAvatar
import com.qodein.core.ui.component.PromocodeCard
import com.qodein.core.ui.component.PromocodeCardSkeleton
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.component.post.PostCard
import com.qodein.core.ui.component.post.PostCardSkeleton
import com.qodein.core.ui.preview.PostPreviewData
import com.qodein.core.ui.preview.PromocodePreviewData
import com.qodein.core.ui.preview.UserPreviewData
import com.qodein.feature.profile.component.ProfileSkeleton
import com.qodein.feature.profile.component.ProfileTopAppBar
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.User
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private const val PAGINATION_LOAD_THRESHOLD = 1

@Composable
fun ProfileRoute(
    onBackClick: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateToBlockedUsers: () -> Unit,
    onNavigateToPostDetail: (PostId) -> Unit,
    onNavigateToPromocodeDetail: (PromocodeId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel()
) {
    TrackScreenViewEvent(screenName = "Profile")
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileEvent.SignedOut -> onSignOut()
                ProfileEvent.NavigateToAuth -> onBackClick()
                ProfileEvent.NavigateToBlockedUsers -> onNavigateToBlockedUsers()
                is ProfileEvent.NavigateToPostDetail -> onNavigateToPostDetail(event.postId)
                is ProfileEvent.NavigateToPromocodeDetail -> onNavigateToPromocodeDetail(event.promocodeId)
            }
        }
    }

    ProfileScreen(
        onBackClick = onBackClick,
        onAction = viewModel::onAction,
        uiState = uiState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(
    onBackClick: () -> Unit,
    onAction: (ProfileAction) -> Unit,
    uiState: ProfileUiState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ProfileTopAppBar(
                onBackClick = onBackClick,
                onAction = onAction,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = SpacingTokens.sm),
            contentAlignment = Alignment.Center,
        ) {
            when (uiState) {
                is ProfileUiState.Success -> {
                    ProfileContent(
                        successState = uiState,
                        onAction = onAction,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is ProfileUiState.Loading -> {
                    ProfileSkeleton()
                }

                is ProfileUiState.Error -> {
                    QodeErrorCard(
                        error = uiState.errorType,
                        onRetry = { onAction(ProfileAction.RetryClicked) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    successState: ProfileUiState.Success,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val showScrollToTopButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 2
        }
    }

    LaunchedEffect(listState, successState.promocodesState, successState.postsState) {
        snapshotFlow {
            val paginatedState = when (successState.selectedTab) {
                ProfileTab.PROMOCODES -> successState.promocodesState
                ProfileTab.POSTS -> successState.postsState
            }

            if (paginatedState is PaginatedDataState.Success &&
                paginatedState.hasMore &&
                !paginatedState.isLoadingMore
            ) {
                val layoutInfo = listState.layoutInfo
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()

                if (lastVisibleItem == null || totalItems == 0) {
                    false
                } else {
                    lastVisibleItem.index >= totalItems - PAGINATION_LOAD_THRESHOLD
                }
            } else {
                false
            }
        }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                val action = when (successState.selectedTab) {
                    ProfileTab.PROMOCODES -> ProfileAction.LoadMorePromocodes
                    ProfileTab.POSTS -> ProfileAction.LoadMorePosts
                }
                onAction(action)
            }
    }

    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            contentPadding = PaddingValues(vertical = SpacingTokens.lg),
        ) {
            item {
                ProfileHeader(user = successState.user, modifier = Modifier.fillMaxWidth())
            }

            item {
                SecondaryTabRow(
                    selectedTabIndex = successState.selectedTab.ordinal,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ProfileTab.entries.forEach { tab ->
                        Tab(
                            selected = successState.selectedTab == tab,
                            onClick = { onAction(ProfileAction.TabSelected(tab)) },
                            text = {
                                Text(
                                    text = when (tab) {
                                        ProfileTab.PROMOCODES -> stringResource(R.string.profile_promocodes)
                                        ProfileTab.POSTS -> stringResource(R.string.profile_posts)
                                    },
                                )
                            },
                        )
                    }
                }
            }

            when (successState.selectedTab) {
                ProfileTab.PROMOCODES -> promocodesTabItems(
                    state = successState.promocodesState,
                    onAction = onAction,
                )
                ProfileTab.POSTS -> postsTabItems(
                    state = successState.postsState,
                    onAction = onAction,
                )
            }
        }

        if (showScrollToTopButton) {
            QodeinFab(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                icon = NavigationIcons.Upward,
                contentDescription = stringResource(R.string.profile_fab_scroll_to_top),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = SpacingTokens.md, bottom = SpacingTokens.gigantic),
            )
        }
    }
}

private fun LazyListScope.promocodesTabItems(
    state: PaginatedDataState<Promocode>,
    onAction: (ProfileAction) -> Unit
) {
    when (state) {
        is PaginatedDataState.Loading -> {
            items(5) {
                PromocodeCardSkeleton()
            }
        }

        is PaginatedDataState.Success -> {
            if (state.items.isEmpty()) {
                item {
                    EmptyState(
                        icon = PromocodeIcons.Promocode,
                        title = stringResource(R.string.profile_empty_promocodes_title),
                        description = stringResource(R.string.profile_empty_promocodes_description),
                    )
                }
            } else {
                items(
                    items = state.items,
                    key = { it.id.value },
                ) { promocode ->
                    PromocodeCard(
                        promocode = promocode,
                        onCardClick = { onAction(ProfileAction.PromocodeClicked(promocode.id)) },
                        onCopyCodeClick = { },
                    )
                }

                if (state.hasMore && state.isLoadingMore) {
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

        is PaginatedDataState.Error -> {
            item {
                QodeErrorCard(
                    error = state.error,
                    onRetry = { onAction(ProfileAction.RetryPromocodesClicked) },
                )
            }
        }
    }
}

private fun LazyListScope.postsTabItems(
    state: PaginatedDataState<Post>,
    onAction: (ProfileAction) -> Unit
) {
    when (state) {
        is PaginatedDataState.Loading -> {
            items(5) {
                PostCardSkeleton()
            }
        }

        is PaginatedDataState.Success -> {
            if (state.items.isEmpty()) {
                item {
                    EmptyState(
                        icon = PostIcons.Post,
                        title = stringResource(R.string.profile_empty_posts_title),
                        description = stringResource(R.string.profile_empty_posts_description),
                    )
                }
            } else {
                items(
                    items = state.items,
                    key = { it.id.value },
                ) { post ->
                    PostCard(
                        post = post,
                        onPostClick = { onAction(ProfileAction.PostClicked(post.id)) },
                        onImageClick = { },
                    )
                }

                if (state.hasMore && state.isLoadingMore) {
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

        is PaginatedDataState.Error -> {
            item {
                QodeErrorCard(
                    error = state.error,
                    onRetry = { onAction(ProfileAction.RetryPostsClicked) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProfileHeader(
    user: User,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        ProfileAvatar(
            user = user,
            shape = MaterialShapes.Cookie9Sided.toShape(),
            size = SizeTokens.Avatar.sizeXLarge,
            modifier = Modifier.testTag("profile_avatar"),
        )
        UserInfo(user = user)
    }
}

@Composable
private fun UserInfo(
    user: User,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        Text(
            text = user.displayName ?: "",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
            modifier = modifier,
        )
    }
}

@PreviewLightDark
@Composable
private fun ProfilePromocodesLoadingPreview() = ProfilePromocodesPreview(PaginatedDataState.Loading)

@PreviewLightDark
@Composable
private fun ProfilePromocodesEmptyPreview() =
    ProfilePromocodesPreview(
        PaginatedDataState.Success(
            items = emptyList(),
            hasMore = false,
            nextCursor = null,
        ),
    )

@PreviewLightDark
@Composable
private fun ProfilePromocodesErrorPreview() =
    ProfilePromocodesPreview(
        PaginatedDataState.Error(SystemError.Offline),
    )

@PreviewLightDark
@Composable
private fun ProfilePromocodesSuccessPreview() =
    ProfilePromocodesPreview(
        PaginatedDataState.Success(
            items = PromocodePreviewData.allSamples,
            hasMore = false,
            nextCursor = null,
        ),
    )

@Composable
private fun ProfilePromocodesPreview(promocodesState: PaginatedDataState<Promocode>) {
    QodeTheme {
        Surface {
            ProfileScreen(
                onBackClick = {},
                onAction = {},
                uiState = ProfileUiState.Success(
                    user = UserPreviewData.powerUser,
                    promocodesState = promocodesState,
                ),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ProfilePostsLoadingPreview() = ProfilePostsPreview(PaginatedDataState.Loading)

@PreviewLightDark
@Composable
private fun ProfilePostsEmptyPreview() =
    ProfilePostsPreview(
        PaginatedDataState.Success(
            items = emptyList(),
            hasMore = false,
            nextCursor = null,
        ),
    )

@PreviewLightDark
@Composable
private fun ProfilePostsErrorPreview() =
    ProfilePostsPreview(
        PaginatedDataState.Error(SystemError.Offline),
    )

@PreviewLightDark
@Composable
private fun ProfilePostsSuccessPreview() =
    ProfilePostsPreview(
        PaginatedDataState.Success(
            items = PostPreviewData.allPosts,
            hasMore = false,
            nextCursor = null,
        ),
    )

@Composable
private fun ProfilePostsPreview(postsState: PaginatedDataState<Post>) {
    QodeTheme {
        Surface {
            ProfileScreen(
                onBackClick = {},
                onAction = {},
                uiState = ProfileUiState.Success(
                    user = UserPreviewData.powerUser,
                    selectedTab = ProfileTab.POSTS,
                    postsState = postsState,
                ),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ProfileLoadingPreview() {
    QodeTheme {
        Surface {
            ProfileScreen(
                onBackClick = {},
                onAction = {},
                uiState = ProfileUiState.Loading,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ProfileErrorPreview() {
    QodeTheme {
        Surface {
            ProfileScreen(
                onBackClick = {},
                onAction = {},
                uiState = ProfileUiState.Error(SystemError.Unknown),
            )
        }
    }
}
