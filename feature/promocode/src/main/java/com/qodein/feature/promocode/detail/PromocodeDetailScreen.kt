package com.qodein.feature.promocode.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.AuthenticationBottomSheet
import com.qodein.core.ui.preview.PromocodePreviewData
import com.qodein.core.ui.util.formatNumber
import com.qodein.feature.promocode.detail.component.PromocodeActions
import com.qodein.feature.promocode.detail.component.PromocodeDetails
import com.qodein.feature.promocode.detail.component.PromocodeInfo
import com.qodein.shared.model.Discount
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.PromocodeInteraction

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

    Scaffold(
        modifier = modifier,
        topBar = {
            QodeTopAppBar(
                title = uiState.promocodeInteraction?.promocode?.code?.value ?: "",
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
            when {
                uiState.isLoading -> {
                    LoadingState()
                }

                uiState.hasData -> {
                    val promocodeInteraction = uiState.promocodeInteraction!!
                    val promocode = promocodeInteraction.promocode

                    SuccessState(
                        promocode = promocode,
                        promocodeInteraction = promocodeInteraction,
                        onAction = onAction,
                    )
                }
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
private fun SuccessState(
    promocode: Promocode,
    promocodeInteraction: PromocodeInteraction,
    onAction: (PromocodeDetailAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        PromocodeInfo(
            promocode = promocode,
        )

        PromocodeDetails(promocode = promocode)

        PromocodeActions(
            promoCode = promocode,
            isUpvotedByCurrentUser = promocodeInteraction.isUpvotedByCurrentUser,
            isDownvotedByCurrentUser = promocodeInteraction.isDownvotedByCurrentUser,
            onUpvoteClicked = { onAction(PromocodeDetailAction.UpvoteClicked) },
            onDownvoteClicked = { onAction(PromocodeDetailAction.DownvoteClicked) },
            onShareClicked = { onAction(PromocodeDetailAction.ShareClicked) },
        )
    }
}

// TODO: Make it a skeleton
@Composable
private fun LoadingState() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator()
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
private fun PromocodeDetailScreenPreview() {
    QodeTheme {
        val samplePromoCode = PromocodePreviewData.percentagePromocode

        PromocodeDetailScreen(
            uiState = PromocodeDetailUiState(
                promocodeId = samplePromoCode.id,
                promocodeInteraction = PromocodeInteraction(
                    promocode = samplePromoCode,
                    userInteraction = null,
                ),
                isLoading = false,
            ),
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@ThemePreviews
@Composable
private fun LoadingStatePreview() {
    QodeTheme {
        val samplePromoCode = PromocodePreviewData.percentagePromocode
        PromocodeDetailScreen(
            uiState = PromocodeDetailUiState(
                promocodeId = samplePromoCode.id,
                promocodeInteraction = PromocodeInteraction(
                    promocode = samplePromoCode,
                    userInteraction = null,
                ),
                isLoading = true,
            ),
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
