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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.ShimmerBox
import com.qodein.core.designsystem.component.ShimmerLine
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.AuthPromptAction
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.error.toUiText
import com.qodein.core.ui.preview.PromocodePreviewData
import com.qodein.core.ui.text.asString
import com.qodein.core.ui.util.formatNumber
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.detail.component.PromocodeActions
import com.qodein.feature.promocode.detail.component.PromocodeDetailTopAppBar
import com.qodein.feature.promocode.detail.component.PromocodeDetails
import com.qodein.feature.promocode.detail.component.PromocodeInfo
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.Discount
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
import com.qodein.shared.model.VoteState
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.qodein.core.ui.R as CoreUiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromocodeDetailRoute(
    promoCodeId: PromocodeId,
    onNavigateBack: () -> Unit,
    onNavigateToAuth: (AuthPromptAction) -> Unit,
    onNavigateToReport: (String, String, String?) -> Unit,
    onNavigateToBlockUser: (UserId, String?, String?) -> Unit,
    viewModel: PromocodeDetailViewModel = koinViewModel { parametersOf(promoCodeId.value) }
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
                is PromocodeDetailEvent.SharePromocode -> sharePromocode(localContext, event.promocode)
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
    val context = LocalContext.current
    val title = when (val state = uiState.promocodeState) {
        is PromocodeUiState.Success -> state.data.code.value
        else -> ""
    }
    val authorId = (uiState.promocodeState as? PromocodeUiState.Success)?.data?.authorId

    Scaffold(
        modifier = modifier,
        topBar = {
            PromocodeDetailTopAppBar(
                title = title,
                promocodeId = uiState.promocodeId,
                currentUserId = uiState.userId,
                authorId = authorId,
                onNavigateBack = onNavigateBack,
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
        when (val promoState = uiState.promocodeState) {
            PromocodeUiState.Loading -> PromocodeLoadingState()
            is PromocodeUiState.Error -> PromocodeErrorState(error = promoState.error, onRetry = {
                onAction(PromocodeDetailAction.RetryClicked)
            })
            is PromocodeUiState.Success -> PromocodeSuccessState(
                promocode = promoState.data,
                userInteraction = uiState.userInteraction,
                currentVoting = uiState.currentVoting,
                optimisticUpvotes = uiState.optimisticUpvotes,
                optimisticDownvotes = uiState.optimisticDownvotes,
                onAction = onAction,
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = SpacingTokens.sm),
            )
        }
    }
}

@Composable
private fun PromocodeSuccessState(
    promocode: Promocode,
    userInteraction: UserInteraction?,
    currentVoting: VoteState?,
    optimisticUpvotes: Int?,
    optimisticDownvotes: Int?,
    onAction: (PromocodeDetailAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayUpvotes = optimisticUpvotes ?: promocode.upvotes
    val displayDownvotes = optimisticDownvotes ?: promocode.downvotes
    val displayVoteScore = displayUpvotes - displayDownvotes

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        PromocodeInfo(
            promocode = promocode,
            voteScoreOverride = displayVoteScore,
        )
        PromocodeDetails(promocode = promocode)

        InteractionsSuccessState(
            promocode = promocode,
            userInteraction = userInteraction,
            currentVoting = currentVoting,
            optimisticUpvotes = optimisticUpvotes,
            optimisticDownvotes = optimisticDownvotes,
            onAction = onAction,
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
private fun InteractionsSuccessState(
    promocode: Promocode,
    userInteraction: UserInteraction?,
    currentVoting: VoteState?,
    optimisticUpvotes: Int?,
    optimisticDownvotes: Int?,
    onAction: (PromocodeDetailAction) -> Unit
) {
    val voteState = userInteraction?.voteState ?: VoteState.NONE
    val upvotes = optimisticUpvotes ?: promocode.upvotes
    val downvotes = optimisticDownvotes ?: promocode.downvotes

    PromocodeActions(
        upvoteCount = upvotes,
        downvoteCount = downvotes,
        vote = voteState,
        currentVoting = currentVoting,
        onVote = { vote -> onAction(PromocodeDetailAction.VoteClicked(vote)) },
        onShareClicked = { onAction(PromocodeDetailAction.ShareClicked) },
    )
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

// Helper functions
private fun sharePromocode(
    context: Context,
    promoCode: Promocode
) {
    val shareText = buildShareText(context, promoCode)

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(
            Intent.EXTRA_SUBJECT,
            context.getString(R.string.share_subject_format, promoCode.serviceName),
        )
    }

    val chooserTitle = context.getString(R.string.action_share_promocode)
    try {
        context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
    } catch (_: Exception) {
        // Swallow to avoid crashes if no activity can handle the intent.
    }
}

private fun buildShareText(
    context: Context,
    promoCode: Promocode
): String {
    val discountText = when (val discount = promoCode.discount) {
        is Discount.Percentage -> context.getString(
            CoreUiR.string.discount_percent,
            formatNumber(discount.value),
        )

        is Discount.FixedAmount -> context.getString(
            CoreUiR.string.discount_fixed,
            formatNumber(discount.value),
        )
    }

    return buildString {
        append(context.getString(R.string.share_header_format, promoCode.serviceName, discountText))
        append("\n")
        append(context.getString(R.string.share_code_label, promoCode.code.value))
        append("\n\n")

        promoCode.description?.takeIf { it.isNotBlank() }?.let { description ->
            append(description.trim())
            append("\n\n")
        }

        append(context.getString(R.string.share_link_format, promoCode.id.value))
    }
}

private fun copyToClipboard(
    context: Context,
    code: String
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Promo Code", code)
    clipboard.setPrimaryClip(clip)
}

@ThemePreviews
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

@ThemePreviews
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

@ThemePreviews
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

@ThemePreviews
@Composable
private fun InteractionLoadingPreview() {
    QodeTheme {
        val samplePromoCode = PromocodePreviewData.percentagePromocode
        PromocodeDetailScreen(
            uiState = PromocodeDetailUiState(
                promocodeId = samplePromoCode.id,
                promocodeState = PromocodeUiState.Success(samplePromoCode),
                currentVoting = VoteState.UPVOTE,
            ),
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@ThemePreviews
@Composable
private fun InteractionErrorPreview() {
    QodeTheme {
        val samplePromoCode = PromocodePreviewData.percentagePromocode
        PromocodeDetailScreen(
            uiState = PromocodeDetailUiState(
                promocodeId = samplePromoCode.id,
                promocodeState = PromocodeUiState.Success(samplePromoCode),
                transientError = SystemError.Unknown,
            ),
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@ThemePreviews
@Composable
private fun InteractionSuccessPreview() {
    QodeTheme {
        val samplePromoCode = PromocodePreviewData.percentagePromocode
        val interaction = UserInteraction.create(
            itemId = samplePromoCode.id.value,
            itemType = ContentType.PROMO_CODE,
            userId = UserId("preview-user"),
            voteState = VoteState.UPVOTE,
            isBookmarked = false,
        )

        PromocodeDetailScreen(
            uiState = PromocodeDetailUiState(
                promocodeId = samplePromoCode.id,
                promocodeState = PromocodeUiState.Success(samplePromoCode),
                userInteraction = interaction,
            ),
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
