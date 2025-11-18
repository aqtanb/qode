package com.qodein.feature.promocode.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.component.QodeTopAppBarVariant
import com.qodein.core.designsystem.component.TopAppBarAction
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.AuthenticationBottomSheet
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.preview.PromoCodePreviewData
import com.qodein.feature.promocode.detail.component.ActionButtonsSection
import com.qodein.feature.promocode.detail.component.DetailsSection
import com.qodein.feature.promocode.detail.component.FooterSection
import com.qodein.feature.promocode.detail.component.GradientBannerSection
import com.qodein.feature.promocode.detail.component.ServiceInfoSection
import com.qodein.shared.common.error.PromocodeError
import com.qodein.shared.model.Discount
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeWithUserState
import com.qodein.shared.model.PromocodeId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromocodeDetailScreen(
    promoCodeId: PromocodeId,
    onNavigateBack: () -> Unit,
    onNavigateToComments: (PromocodeId) -> Unit = {},
    onNavigateToService: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    viewModel: PromocodeDetailViewModel = hiltViewModel(
        creationCallback = { factory: PromocodeDetailViewModel.Factory ->
            factory.create(promoCodeId.value)
        },
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Authentication-protected bookmark action
    val requireBookmark = {
        // This will be handled by wrapping the button in AuthenticationGate
        viewModel.onAction(PromocodeDetailAction.BookmarkToggleClicked)
    }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PromocodeDetailEvent.NavigateBack -> onNavigateBack()
                is PromocodeDetailEvent.NavigateToComments -> onNavigateToComments(event.promoCodeId)
                is PromocodeDetailEvent.NavigateToService -> onNavigateToService(event.serviceName)
                is PromocodeDetailEvent.SharePromocode -> sharePromocode(context, event.promoCode)
                is PromocodeDetailEvent.CopyCodeToClipboard -> copyToClipboard(context, event.code)
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
                is PromocodeDetailEvent.ShowFollowServiceTodo -> {
                    snackbarHostState.showSnackbar(
                        message = "TODO: Follow ${event.serviceName} feature coming soon!",
                        duration = SnackbarDuration.Short,
                    )
                }
                is PromocodeDetailEvent.ShowFollowCategoryTodo -> {
                    snackbarHostState.showSnackbar(
                        message = "TODO: Follow ${event.categoryName} category coming soon!",
                        duration = SnackbarDuration.Short,
                    )
                }
                is PromocodeDetailEvent.ShowAuthenticationRequired -> {
                    // This event is no longer used - authentication is handled via UI state
                }
            }
        }
    }

    // Screen-level scaffold with own top bar to avoid app-level coupling
    // TODO: Make it have positive UI
    Scaffold(
        topBar = {
            QodeTopAppBar(
                title = "Promocode Details",
                navigationIcon = QodeActionIcons.Back,
                onNavigationClick = onNavigateBack,
                variant = QodeTopAppBarVariant.CenterAligned,
                statusBarPadding = true, // Add status bar padding manually
                actions = listOf(
                    TopAppBarAction(
                        icon = if (uiState.promoCodeWithUserState?.isBookmarkedByCurrentUser == true) {
                            QodeActionIcons.BookmarkFilled
                        } else {
                            QodeActionIcons.Bookmark
                        },
                        contentDescription = if (uiState.promoCodeWithUserState?.isBookmarkedByCurrentUser == true) {
                            "Remove bookmark"
                        } else {
                            "Bookmark promocode"
                        },
                        onClick = requireBookmark,
                    ),
                ),
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
        contentWindowInsets = WindowInsets(0), // Remove automatic window insets
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        PromocodeDetailContent(
            uiState = uiState,
            onAction = viewModel::onAction,
            modifier = Modifier.padding(paddingValues),
            isDarkTheme = isDarkTheme,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PromocodeDetailContent(
    uiState: PromocodeDetailUiState,
    onAction: (PromocodeDetailAction) -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        when {
            uiState.isLoading && !uiState.hasData -> {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Loading promocode details...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            uiState.hasError && !uiState.hasData -> {
                // Error state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(SpacingTokens.md),
                    contentAlignment = Alignment.Center,
                ) {
                    QodeErrorCard(
                        error = PromocodeError.RetrievalFailure.NotFound,
                        onRetry = { onAction(PromocodeDetailAction.RetryClicked) },
                        onDismiss = { onAction(PromocodeDetailAction.ErrorDismissed) },
                    )
                }
            }

            uiState.hasData -> {
                // Content state
                val promoCodeWithUserState = uiState.promoCodeWithUserState!!
                val promoCode = promoCodeWithUserState.promoCode

                // Vote actions - auth checking now handled in ViewModel
                val requireUpvote = {
                    onAction(PromocodeDetailAction.UpvoteClicked)
                }

                val requireDownvote = {
                    onAction(PromocodeDetailAction.DownvoteClicked)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    // Gradient Banner Section
                    GradientBannerSection(
                        promoCode = promoCode,
                        isCopying = uiState.isCopying,
                        onCopyClicked = { onAction(PromocodeDetailAction.CopyCodeClicked) },
                    )

                    // Service Information Section
                    ServiceInfoSection(
                        promoCode = promoCode,
                        isFollowingService = uiState.isFollowingService,
                        onServiceClicked = { onAction(PromocodeDetailAction.ServiceClicked) },
                        onFollowServiceClicked = { onAction(PromocodeDetailAction.FollowServiceClicked) },
                        isDarkTheme = isDarkTheme,
                    )

                    // Details Section
                    DetailsSection(promoCode = promoCode)

                    // Action Buttons Section
                    ActionButtonsSection(
                        promoCode = promoCode,
                        isUpvotedByCurrentUser = promoCodeWithUserState.isUpvotedByCurrentUser,
                        isDownvotedByCurrentUser = promoCodeWithUserState.isDownvotedByCurrentUser,
                        showVoteAnimation = uiState.showVoteAnimation,
                        lastVoteType = uiState.lastVoteType,
                        isSharing = uiState.isSharing,
                        onUpvoteClicked = requireUpvote,
                        onDownvoteClicked = requireDownvote,
                        onShareClicked = { onAction(PromocodeDetailAction.ShareClicked) },
                        onCommentsClicked = { onAction(PromocodeDetailAction.CommentsClicked) },
                    )

                    FooterSection(
                        username = promoCode.authorUsername,
                        avatarUrl = promoCode.authorAvatarUrl,
                        createdAt = promoCode.createdAt,
                        modifier = Modifier.padding(horizontal = SpacingTokens.md),
                    )

                    // Bottom spacing - much smaller
                    Spacer(modifier = Modifier.height(SpacingTokens.md))
                }
            }
        }

        // Authentication Bottom Sheet
        uiState.authBottomSheet?.let { authSheetState ->
            AuthenticationBottomSheet(
                authPromptAction = authSheetState.action,
                isLoading = authSheetState.isLoading,
                onSignInClick = { onAction(PromocodeDetailAction.SignInWithGoogleClicked) },
                onDismiss = { onAction(PromocodeDetailAction.DismissAuthSheet) },
                isDarkTheme = isDarkTheme,
            )
        }
    }
}

// Helper functions
private fun sharePromocode(
    context: Context,
    promoCode: PromoCode
) {
    val shareText = buildString {
        append("🎉 Check out this amazing deal!\n\n")
        append("${promoCode.serviceName}\n")
        when (val discount = promoCode.discount) {
            is Discount.Percentage -> append("${discount.value.toInt()}% OFF")
            is Discount.FixedAmount -> append("${discount.value.toInt()}₸ OFF")
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

@Preview(
    name = "Light Mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Composable
private fun PromocodeDetailScreenPreview() {
    QodeTheme(darkTheme = false) {
        val samplePromoCode = PromoCodePreviewData.percentagePromoCode

        PromocodeDetailContent(
            uiState = PromocodeDetailUiState(
                promoCodeId = samplePromoCode.id,
                promoCodeWithUserState = PromoCodeWithUserState(
                    promoCode = samplePromoCode,
                    userInteraction = null,
                ),
                isLoading = false,
            ),
            onAction = {},
            isDarkTheme = false,
        )
    }
}
