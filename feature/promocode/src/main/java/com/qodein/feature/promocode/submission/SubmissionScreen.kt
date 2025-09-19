package com.qodein.feature.promocode.submission

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.AuthPromptAction
import com.qodein.core.ui.component.AuthenticationBottomSheet
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.component.ServiceSelectorBottomSheet
import com.qodein.core.ui.error.toLocalizedMessage
import com.qodein.core.ui.preview.ServicePreviewData
import com.qodein.feature.promocode.submission.component.FloatingStepCard
import com.qodein.feature.promocode.submission.component.ModernFloatingController
import com.qodein.feature.promocode.submission.component.ProgressIndicator
import com.qodein.feature.promocode.submission.component.SmartProgressSummary
import com.qodein.shared.common.result.toErrorType

// MARK: - Main Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubmissionWizardViewModel = hiltViewModel()
) {
    TrackScreenViewEvent(screenName = "ProgressiveSubmission")

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(events) {
        when (events) {
            SubmissionWizardEvent.NavigateBack -> onNavigateBack()
            SubmissionWizardEvent.PromoCodeSubmitted -> onNavigateBack()
            null -> { /* No event */ }
        }
    }

    when (val currentState = uiState) {
        is SubmissionWizardUiState.Loading -> {
            LoadingState()
        }
        is SubmissionWizardUiState.Success -> {
            // Show authentication bottom sheet when needed
            val showAuthSheet = currentState.authentication !is AuthenticationState.Authenticated
            if (showAuthSheet) {
                val isSigningIn = currentState.authentication is AuthenticationState.Loading
                val authError = (currentState.authentication as? AuthenticationState.Error)?.throwable

                AuthenticationBottomSheet(
                    action = AuthPromptAction.SubmitPromoCode,
                    onSignInClick = { viewModel.onAction(SubmissionWizardAction.SignInWithGoogle) },
                    onDismiss = { viewModel.onAction(SubmissionWizardAction.DismissAuthSheet) },
                    isLoading = isSigningIn,
                    errorType = authError?.toErrorType(),
                    onErrorDismissed = { viewModel.onAction(SubmissionWizardAction.ClearAuthError) },
                    isDarkTheme = false, // TODO: Get from theme state
                )
                return
            }

            // Only show submission content if user is authenticated
            val isAuthenticated = currentState.authentication is AuthenticationState.Authenticated
            if (!isAuthenticated) {
                return
            }

            val serviceSelectorSheetState = rememberModalBottomSheetState()

            // Effect to control bottom sheet visibility based on state
            LaunchedEffect(currentState.showServiceSelector) {
                if (currentState.showServiceSelector) {
                    serviceSelectorSheetState.show()
                } else {
                    serviceSelectorSheetState.hide()
                }
            }

            SubmissionContent(
                uiState = currentState,
                onAction = viewModel::onAction,
            )

            // Always render the bottom sheet, visibility controlled by sheetState
            ServiceSelectorBottomSheet(
                isVisible = currentState.showServiceSelector,
                services = when (val state = currentState.serviceSelectionUiState) {
                    is ServiceSelectionUiState.Searching -> state.results
                    else -> emptyList()
                },
                popularServices = when (val state = currentState.serviceSelectionUiState) {
                    is ServiceSelectionUiState.Searching -> if (state.query.isEmpty()) state.results else emptyList()
                    else -> emptyList()
                },
                onServiceSelected = { service ->
                    viewModel.onAction(SubmissionWizardAction.SelectService(service))
                    viewModel.onAction(SubmissionWizardAction.HideServiceSelector)
                },
                onDismiss = {
                    viewModel.onAction(SubmissionWizardAction.HideServiceSelector)
                },
                onSearch = { query ->
                    viewModel.onAction(SubmissionWizardAction.SearchServices(query))
                },
                isLoading = when (val state = currentState.serviceSelectionUiState) {
                    is ServiceSelectionUiState.Searching -> state.results.isEmpty() && state.query.isNotEmpty()
                    else -> false
                },
                sheetState = serviceSelectorSheetState,
                searchQuery = when (val state = currentState.serviceSelectionUiState) {
                    is ServiceSelectionUiState.Searching -> state.query
                    else -> ""
                },
                onSearchQueryChange = { query ->
                    viewModel.onAction(SubmissionWizardAction.SearchServices(query))
                },
            )
        }
        is SubmissionWizardUiState.Error -> {
            ErrorState(
                message = currentState.errorType.toLocalizedMessage(),
                onRetry = { viewModel.onAction(SubmissionWizardAction.RetryClicked) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmissionContent(
    uiState: SubmissionWizardUiState.Success,
    onAction: (SubmissionWizardAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Create dynamic background alpha based on scroll
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (scrollState.value > 100) 0.95f else 0.8f,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "backgroundAlpha",
    )

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        // Dynamic gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                ),
        )

        // Main content
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            bottomBar = {
                ModernFloatingController(
                    currentStep = uiState.wizardFlow.currentStep,
                    wizardData = uiState.wizardFlow.wizardData,
                    isLoading = uiState.submission is SubmissionState.Submitting,
                    onNext = {
                        if (uiState.wizardFlow.currentStep.isLast) {
                            onAction(SubmissionWizardAction.SubmitPromoCode)
                        } else {
                            onAction(SubmissionWizardAction.NextProgressiveStep)
                        }
                    },
                    onPrevious = {
                        onAction(SubmissionWizardAction.PreviousProgressiveStep)
                    },
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(paddingValues)
                    .padding(horizontal = SpacingTokens.md),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
            ) {
                // Progress Indicator
                ProgressIndicator(
                    currentStep = uiState.wizardFlow.currentStep,
                    onStepClick = { step ->
                        onAction(SubmissionWizardAction.NavigateToStep(step))
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Smart Progress Summary
                SmartProgressSummary(
                    currentStep = uiState.wizardFlow.currentStep,
                    wizardData = uiState.wizardFlow.wizardData,
                    onEditStep = { step ->
                        onAction(SubmissionWizardAction.NavigateToStep(step))
                    },
                    onUpdateField = onAction,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Main Step Content Card
                FloatingStepCard(
                    currentStep = uiState.wizardFlow.currentStep,
                    wizardData = uiState.wizardFlow.wizardData,
                    serviceSelectionUiState = uiState.serviceSelectionUiState,
                    onAction = onAction,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Add spacing for bottom navigation
                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier.height(SpacingTokens.xl),
                )
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingTokens.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        QodeErrorCard(
            message = message,
            onRetry = onRetry,
        )
    }
}

// MARK: - Previews

@Preview(name = "Progressive Submission Content - Service Step", showBackground = true)
@Composable
private fun ProgressiveSubmissionContentServicePreview() {
    QodeTheme {
        SubmissionContent(
            uiState = SubmissionWizardUiState.Success.initial().copy(
                wizardFlow = WizardFlowState(
                    wizardData = SubmissionWizardData(),
                    currentStep = ProgressiveStep.SERVICE,
                ),
                authentication = AuthenticationState.Unauthenticated,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Progressive Submission Content - Promo Code Step", showBackground = true)
@Composable
private fun ProgressiveSubmissionContentPromoCodePreview() {
    QodeTheme {
        SubmissionContent(
            uiState = SubmissionWizardUiState.Success.initial().copy(
                wizardFlow = WizardFlowState(
                    wizardData = SubmissionWizardData(
                        selectedService = ServicePreviewData.netflix,
                        promoCodeType = PromoCodeType.PERCENTAGE,
                    ),
                    currentStep = ProgressiveStep.PROMO_CODE,
                ),
                authentication = AuthenticationState.Unauthenticated,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Submission Screen - Loading", showSystemUi = true)
@Composable
private fun SubmissionScreenLoadingPreview() {
    QodeTheme {
        LoadingState()
    }
}

@Preview(name = "Submission Screen - Error", showSystemUi = true)
@Composable
private fun SubmissionScreenErrorPreview() {
    QodeTheme {
        ErrorState(
            message = "Failed to load submission data",
            onRetry = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SubmissionContentDarkThemePreview() {
    QodeTheme {
        SubmissionContent(
            uiState = SubmissionWizardUiState.Success.initial().copy(
                wizardFlow = WizardFlowState(
                    wizardData = SubmissionWizardData(
                        selectedService = ServicePreviewData.netflix,
                        promoCodeType = PromoCodeType.PERCENTAGE,
                    ),
                    currentStep = ProgressiveStep.DISCOUNT_VALUE,
                ),
                authentication = AuthenticationState.Unauthenticated,
            ),
            onAction = {},
        )
    }
}
