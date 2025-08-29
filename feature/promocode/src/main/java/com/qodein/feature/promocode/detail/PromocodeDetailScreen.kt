package com.qodein.feature.promocode.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.QodeActionErrorCard
import com.qodein.feature.promocode.detail.component.ActionButtonsSection
import com.qodein.feature.promocode.detail.component.DetailsSection
import com.qodein.feature.promocode.detail.component.GradientBannerSection
import com.qodein.feature.promocode.detail.component.ServiceInfoSection
import com.qodein.feature.promocode.detail.component.StatisticsSection
import com.qodein.shared.common.result.ErrorAction
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

@Composable
fun PromocodeDetailScreen(
    promoCodeId: PromoCodeId,
    onNavigateBack: () -> Unit,
    onNavigateToComments: (PromoCodeId) -> Unit = {},
    onNavigateToService: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: PromocodeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Load promocode on first composition
    LaunchedEffect(promoCodeId) {
        viewModel.onAction(PromocodeDetailAction.LoadPromocode(promoCodeId))
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
                    snackbarHostState.showSnackbar(event.message)
                }
                is PromocodeDetailEvent.ShowVoteFeedback -> {
                    val message = if (event.isUpvote) "Thanks for your upvote!" else "Thanks for your feedback!"
                    snackbarHostState.showSnackbar(message)
                }
                is PromocodeDetailEvent.ShowFollowServiceTodo -> {
                    snackbarHostState.showSnackbar("TODO: Follow ${event.serviceName} feature coming soon!")
                }
                is PromocodeDetailEvent.ShowFollowCategoryTodo -> {
                    snackbarHostState.showSnackbar("TODO: Follow ${event.categoryName} category coming soon!")
                }
            }
        }
    }

    PromocodeDetailContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PromocodeDetailContent(
    uiState: PromocodeDetailUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (PromocodeDetailAction) -> Unit,
    modifier: Modifier = Modifier
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
                    QodeActionErrorCard(
                        message = "Failed to load promocode details. Please try again.",
                        errorAction = uiState.errorType?.let {
                            ErrorAction.RETRY
                        } ?: ErrorAction.DISMISS_ONLY,
                        onActionClicked = { onAction(PromocodeDetailAction.RetryClicked) },
                        onDismiss = { onAction(PromocodeDetailAction.ErrorDismissed) },
                    )
                }
            }

            uiState.hasData -> {
                // Content state
                val promoCode = uiState.promoCode!!

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
                        isFollowingCategory = uiState.isFollowingCategory,
                        onServiceClicked = { onAction(PromocodeDetailAction.ServiceClicked) },
                        onFollowServiceClicked = { onAction(PromocodeDetailAction.FollowServiceClicked) },
                        onFollowCategoryClicked = { onAction(PromocodeDetailAction.FollowCategoryClicked) },
                    )

                    // Statistics Section - now horizontal and compact
                    StatisticsSection(
                        promoCode = promoCode,
                        showVoteAnimation = uiState.showVoteAnimation,
                    )

                    // Details Section
                    DetailsSection(promoCode = promoCode)

                    // Action Buttons Section
                    ActionButtonsSection(
                        promoCode = promoCode,
                        isVoting = uiState.isVoting,
                        showVoteAnimation = uiState.showVoteAnimation,
                        lastVoteType = uiState.lastVoteType,
                        isSharing = uiState.isSharing,
                        onUpvoteClicked = { onAction(PromocodeDetailAction.UpvoteClicked) },
                        onDownvoteClicked = { onAction(PromocodeDetailAction.DownvoteClicked) },
                        onShareClicked = { onAction(PromocodeDetailAction.ShareClicked) },
                        onCommentsClicked = { onAction(PromocodeDetailAction.CommentsClicked) },
                    )

                    // Bottom spacing - much smaller
                    Spacer(modifier = Modifier.height(SpacingTokens.md))
                }
            }
        }

        // Snackbar Host positioned at bottom
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        )
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
        when (promoCode) {
            is PromoCode.PercentagePromoCode -> append("${promoCode.discountPercentage.toInt()}% OFF")
            is PromoCode.FixedAmountPromoCode -> append("${promoCode.discountAmount.toInt()}₸ OFF")
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

@Preview
@Composable
private fun PromocodeDetailScreenPreview() {
    QodeTheme {
        val samplePromoCode = PromoCode.PercentagePromoCode(
            id = PromoCodeId("SAMPLE_ID"),
            code = "FALL60",
            serviceName = "Food Delivery Pro",
            category = "Food",
            title = "51% Off Food Orders",
            description = "Казахстанда ең жақсы бағалар",
            discountPercentage = 51.0,
            minimumOrderAmount = 76060.0,
            startDate = Clock.System.now(),
            endDate = Clock.System.now().plus(30.days),
            upvotes = 331,
            downvotes = 28,
            views = 1250,
            shares = 26,
            isVerified = true,
            targetCountries = listOf("KZ"),
            isUpvotedByCurrentUser = false,
            isDownvotedByCurrentUser = false,
            isBookmarkedByCurrentUser = false,
        )

        PromocodeDetailContent(
            uiState = PromocodeDetailUiState(
                promoCode = samplePromoCode,
                isLoading = false,
                isBookmarked = true,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
        )
    }
}
