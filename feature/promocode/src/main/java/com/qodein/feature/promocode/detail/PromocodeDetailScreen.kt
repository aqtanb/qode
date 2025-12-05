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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.component.ShimmerBox
import com.qodein.core.designsystem.component.ShimmerLine
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.AuthenticationBottomSheet
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.preview.PromocodePreviewData
import com.qodein.core.ui.util.formatNumber
import com.qodein.feature.promocode.detail.component.PromocodeActions
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromocodeDetailRoute(
    promoCodeId: PromocodeId,
    onNavigateBack: () -> Unit,
    viewModel: PromocodeDetailViewModel = hiltViewModel(
        creationCallback = { factory: PromocodeDetailViewModel.Factory ->
            factory.create(promoCodeId.value)
        },
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val promocodeState by viewModel.promocodeUiState.collectAsStateWithLifecycle()
    val interactionState by viewModel.interactionState.collectAsStateWithLifecycle()
    val localContext = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PromocodeDetailEvent.NavigateBack -> onNavigateBack()
                is PromocodeDetailEvent.SharePromocode -> sharePromocode(localContext, event.promoCode)
                is PromocodeDetailEvent.CopyCodeToClipboard -> copyToClipboard(localContext, event.code)
                is PromocodeDetailEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short,
                    )
                }
                is PromocodeDetailEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = "Something went wrong. Please try again.",
                        duration = SnackbarDuration.Short,
                    )
                }
                is PromocodeDetailEvent.ShowVoteFeedback -> {
                    val message = if (event.isUpvote) "Thanks for your upvote!" else "Thanks for your feedback!"
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

    PromocodeDetailScreen(
        uiState = uiState,
        promocodeState = promocodeState,
        interactionState = interactionState,
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
    promocodeState: PromocodeUiState,
    interactionState: InteractionUiState,
    onAction: (PromocodeDetailAction) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val title = when (promocodeState) {
        is PromocodeUiState.Success -> promocodeState.data.code.value
        else -> ""
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            QodeTopAppBar(
                title = title,
                navigationIcon = QodeActionIcons.Back,
                onNavigationClick = onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = SpacingTokens.sm),
        ) {
            when (promocodeState) {
                PromocodeUiState.Loading -> PromocodeLoadingState()
                is PromocodeUiState.Error -> PromocodeErrorState(error = promocodeState.error, onRetry = {
                    onAction(PromocodeDetailAction.RetryClicked)
                })
                is PromocodeUiState.Success -> PromocodeSuccessState(
                    promocode = promocodeState.data,
                    interactionState = interactionState,
                    onAction = onAction,
                )
            }

            uiState.authBottomSheet?.let { authSheetState ->
                AuthenticationBottomSheet(
                    authPromptAction = authSheetState.action,
                    isLoading = authSheetState.isLoading,
                    onSignInClick = { onAction(PromocodeDetailAction.SignInWithGoogleClicked(context)) },
                    onDismiss = { onAction(PromocodeDetailAction.DismissAuthSheet) },
                )
            }
        }
    }
}

@Composable
private fun PromocodeSuccessState(
    promocode: Promocode,
    interactionState: InteractionUiState,
    onAction: (PromocodeDetailAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        PromocodeInfo(promocode = promocode)
        PromocodeDetails(promocode = promocode)

        when (interactionState) {
            InteractionUiState.Loading -> InteractionLoadingState()
            is InteractionUiState.Error -> InteractionErrorState(error = interactionState.error, onRetry = {
                onAction(PromocodeDetailAction.RetryClicked)
            })
            else -> InteractionsSuccessState(
                interactionState = interactionState,
                onAction = onAction,
                promocode = promocode,
            )
        }
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
    interactionState: InteractionUiState,
    onAction: (PromocodeDetailAction) -> Unit
) {
    val voteState = when (interactionState) {
        is InteractionUiState.Success -> interactionState.interaction.voteState
        else -> VoteState.NONE
    }
    val isUpvoted = voteState == VoteState.UPVOTE
    val isDownvoted = voteState == VoteState.DOWNVOTE

    PromocodeActions(
        promocode = promocode,
        upvoteCount = promocode.upvotes,
        downvoteCount = promocode.downvotes,
        isUpvotedByCurrentUser = isUpvoted,
        isDownvotedByCurrentUser = isDownvoted,
        onUpvoteClicked = { onAction(PromocodeDetailAction.VoteClicked(VoteState.UPVOTE)) },
        onDownvoteClicked = { onAction(PromocodeDetailAction.VoteClicked(VoteState.DOWNVOTE)) },
        onShareClicked = { onAction(PromocodeDetailAction.ShareClicked) },
    )
}

@Composable
private fun InteractionLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun InteractionErrorState(
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

// Helper functions
private fun sharePromocode(
    context: Context,
    promoCode: Promocode
) {
    val shareText = buildString {
        append("🎉 Check out this amazing deal!\n\n")
        append("${promoCode.serviceName}\n")
        when (val discount = promoCode.discount) {
            is Discount.Percentage -> append("${formatNumber(discount.value)}% OFF")
            is Discount.FixedAmount -> append("${formatNumber(discount.value)}₸ OFF")
        }
        append("\n\nCode: ${promoCode.code}")
        promoCode.description?.let { append("\n\n$it") }
        append("\n\nShared via Qode App")
    }

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "Amazing Deal: ${promoCode.serviceName}")
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share Promocode"))
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
            uiState = PromocodeDetailUiState(promocodeId = samplePromoCode.id),
            promocodeState = PromocodeUiState.Loading,
            interactionState = InteractionUiState.None,
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
            uiState = PromocodeDetailUiState(promocodeId = samplePromoCode.id),
            promocodeState = PromocodeUiState.Error(SystemError.Unknown),
            interactionState = InteractionUiState.None,
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
            uiState = PromocodeDetailUiState(promocodeId = samplePromoCode.id),
            promocodeState = PromocodeUiState.Success(samplePromoCode),
            interactionState = InteractionUiState.None,
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
            uiState = PromocodeDetailUiState(promocodeId = samplePromoCode.id),
            promocodeState = PromocodeUiState.Success(samplePromoCode),
            interactionState = InteractionUiState.Loading,
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
            uiState = PromocodeDetailUiState(promocodeId = samplePromoCode.id),
            promocodeState = PromocodeUiState.Success(samplePromoCode),
            interactionState = InteractionUiState.Error(SystemError.Unknown),
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
            uiState = PromocodeDetailUiState(promocodeId = samplePromoCode.id),
            promocodeState = PromocodeUiState.Success(samplePromoCode),
            interactionState = InteractionUiState.Success(interaction),
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
