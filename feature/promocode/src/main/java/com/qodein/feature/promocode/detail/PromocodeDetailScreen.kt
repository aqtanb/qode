package com.qodein.feature.promocode.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.designsystem.component.ShimmerBox
import com.qodein.core.designsystem.component.ShimmerLine
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.AuthPromptAction
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.component.post.InteractionsRow
import com.qodein.core.ui.error.toUiText
import com.qodein.core.ui.preview.PromocodePreviewData
import com.qodein.core.ui.text.asString
import com.qodein.feature.promocode.detail.component.PromocodeDetailTopAppBar
import com.qodein.feature.promocode.detail.component.PromocodeDetails
import com.qodein.feature.promocode.detail.component.PromocodeInfo
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.ShareContent
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState
import org.koin.androidx.compose.koinViewModel
import com.qodein.core.ui.R as CoreUiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromocodeDetailRoute(
    promoCodeId: PromocodeId,
    onNavigateBack: () -> Unit,
    onNavigateToAuth: (AuthPromptAction) -> Unit,
    onNavigateToReport: (String, String, String?) -> Unit,
    onNavigateToBlockUser: (UserId, String?, String?) -> Unit,
    viewModel: PromocodeDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val localContext = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PromocodeDetailEvent.NavigateBack -> onNavigateBack()
                is PromocodeDetailEvent.NavigateToAuth -> onNavigateToAuth(event.action)
                is PromocodeDetailEvent.NavigateToReport -> onNavigateToReport(
                    event.reportedItemId,
                    event.itemTitle,
                    event.itemAuthor,
                )
                is PromocodeDetailEvent.NavigateToBlockUser -> onNavigateToBlockUser(
                    event.userId,
                    event.username,
                    event.photoUrl,
                )
                is PromocodeDetailEvent.SharePromocode -> shareContent(localContext, event.shareContent)
                is PromocodeDetailEvent.CopyCodeToClipboard -> copyToClipboard(localContext, event.code)
                is PromocodeDetailEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.error.toUiText().asString(localContext),
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

    PromocodeDetailScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = Modifier.fillMaxSize(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromocodeDetailScreen(
    uiState: PromocodeDetailUiState,
    onAction: (PromocodeDetailAction) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val title = when (val state = uiState.promocodeState) {
        is PromocodeUiState.Success -> state.data.code.value
        else -> ""
    }
    val authorId = (uiState.promocodeState as? PromocodeUiState.Success)?.data?.authorId

    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = modifier,
        topBar = {
            PromocodeDetailTopAppBar(
                title = title,
                promocodeId = uiState.promocodeId,
                currentUserId = uiState.currentUserId,
                authorId = authorId,
                onNavigateBack = onNavigateBack,
                onCopyClick = { onAction(PromocodeDetailAction.CopyCodeClicked) },
                onBlockUserClick = { userId ->
                    onAction(PromocodeDetailAction.BlockUserClicked(userId))
                },
                onReportPromocodeClick = { promocodeId ->
                    onAction(PromocodeDetailAction.ReportPromocodeClicked(promocodeId))
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = uiState.isRefreshing,
            onRefresh = { onAction(PromocodeDetailAction.RefreshData) },
            modifier = modifier
                .padding(paddingValues)
                .padding(SpacingTokens.xs),
        ) {
            when (val promocodeUiState = uiState.promocodeState) {
                PromocodeUiState.Loading -> PromocodeLoadingState()
                is PromocodeUiState.Error -> PromocodeErrorState(error = promocodeUiState.error, onRetry = {
                    onAction(PromocodeDetailAction.RetryClicked)
                })
                is PromocodeUiState.Success -> PromocodeSuccessState(
                    promocode = promocodeUiState.data,
                    currentVoteState = uiState.userVoteState,
                    voteScoreDelta = uiState.voteScoreDelta,
                    onAction = onAction,
                )
            }
        }
    }
}

@Composable
private fun PromocodeSuccessState(
    promocode: Promocode,
    currentVoteState: VoteState,
    voteScoreDelta: Int,
    onAction: (PromocodeDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayVoteScore = promocode.voteScore + voteScoreDelta

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        PromocodeInfo(
            promocode = promocode,
            displayVoteScore = displayVoteScore,
        )
        PromocodeDetails(promocode = promocode)

        InteractionsRow(
            voteState = currentVoteState,
            onUpvote = { onAction(PromocodeDetailAction.ToggleVoteClicked(VoteState.UPVOTE)) },
            onDownvote = { onAction(PromocodeDetailAction.ToggleVoteClicked(VoteState.DOWNVOTE)) },
            onShare = { onAction(PromocodeDetailAction.ShareClicked) },
        )
    }
}

@Composable
private fun PromocodeErrorState(
    error: OperationError,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        QodeErrorCard(error = error, onRetry = onRetry)
    }
}

@Composable
private fun PromocodeLoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        // 1. Header (Avatar + Name | Score + Time)
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Left: Avatar and Name
            Row(
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShimmerBox(
                    width = 40.dp,
                    height = 40.dp,
                    shape = CircleShape,
                )
                ShimmerLine(width = 120.dp, height = 14.dp)
            }

            // Right: Meta info aligned to End
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
            ) {
                ShimmerLine(width = 40.dp, height = 12.dp)
                ShimmerLine(width = 70.dp, height = 12.dp)
            }
        }

        // 2. Title (Bold/Large)
        ShimmerLine(width = 280.dp, height = 24.dp)

        // 3. Description Body (3 Paragraphs)
        Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.md)) {
            // Paragraph 1 (~4 lines)
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs)) {
                ShimmerLine(width = 320.dp, height = 14.dp)
                ShimmerLine(width = 300.dp, height = 14.dp)
                ShimmerLine(width = 310.dp, height = 14.dp)
                ShimmerLine(width = 240.dp, height = 14.dp)
            }
        }

        // 4. Details List (Table style)
        Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
            // Table Header
            ShimmerLine(width = 150.dp, height = 18.dp)

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp,
            )

            // Table Rows (7 items)
            repeat(7) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = SpacingTokens.sm),
                    ) {
                        // Left: Icon + Label
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ShimmerBox(
                                width = 20.dp,
                                height = 20.dp,
                                shape = CircleShape,
                            )
                            ShimmerLine(width = 80.dp, height = 14.dp)
                        }

                        // Right: Value
                        ShimmerLine(width = 60.dp, height = 14.dp)
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        thickness = 1.dp,
                    )
                }
            }
        }
    }
}

private fun shareContent(
    context: Context,
    shareContent: ShareContent
) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareContent.text)
        putExtra(Intent.EXTRA_SUBJECT, shareContent.title)
    }

    val chooserTitle = context.getString(CoreUiR.string.ui_action_share_promocode)
    try {
        context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
    } catch (_: Exception) {
    }
}

private fun copyToClipboard(
    context: Context,
    code: String
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Promocode", code)
    clipboard.setPrimaryClip(clip)
}

@PreviewLightDark
@Composable
private fun PromocodeLoadingPreview() {
    QodeTheme {
        val samplePromoCode = PromocodePreviewData.percentagePromocode
        PromocodeDetailScreen(
            uiState = PromocodeDetailUiState(
                promocodeId = samplePromoCode.id,
                promocodeState = PromocodeUiState.Loading,
            ),
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@PreviewLightDark
@Composable
private fun PromocodeErrorPreview() {
    QodeTheme {
        val samplePromoCode = PromocodePreviewData.percentagePromocode
        PromocodeDetailScreen(
            uiState = PromocodeDetailUiState(
                promocodeId = samplePromoCode.id,
                promocodeState = PromocodeUiState.Error(SystemError.Unknown),
            ),
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@PreviewLightDark
@Composable
private fun PromocodeSuccessPreview() {
    QodeTheme {
        val samplePromoCode = PromocodePreviewData.percentagePromocode
        PromocodeDetailScreen(
            uiState = PromocodeDetailUiState(
                promocodeId = samplePromoCode.id,
                promocodeState = PromocodeUiState.Success(samplePromoCode),
            ),
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@PreviewLightDark
@Composable
private fun InteractionLoadingPreview() {
    QodeTheme {
        val samplePromoCode = PromocodePreviewData.percentagePromocode
        PromocodeDetailScreen(
            uiState = PromocodeDetailUiState(
                promocodeId = samplePromoCode.id,
                promocodeState = PromocodeUiState.Success(samplePromoCode),
                userVoteState = VoteState.UPVOTE,
            ),
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@PreviewLightDark
@Composable
private fun InteractionErrorPreview() {
    QodeTheme {
        val samplePromoCode = PromocodePreviewData.percentagePromocode
        PromocodeDetailScreen(
            uiState = PromocodeDetailUiState(
                promocodeId = samplePromoCode.id,
                promocodeState = PromocodeUiState.Success(samplePromoCode),
            ),
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@PreviewLightDark
@Composable
private fun InteractionSuccessPreview() {
    QodeTheme {
        val samplePromoCode = PromocodePreviewData.percentagePromocode
        PromocodeDetailScreen(
            uiState = PromocodeDetailUiState(
                promocodeId = samplePromoCode.id,
                promocodeState = PromocodeUiState.Success(samplePromoCode),
            ),
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
