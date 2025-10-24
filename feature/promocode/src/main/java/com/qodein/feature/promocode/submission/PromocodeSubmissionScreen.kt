package com.qodein.feature.promocode.submission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.AuthPromptAction
import com.qodein.core.ui.component.AuthenticationBottomSheet
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.component.ServiceSelectorBottomSheet
import com.qodein.core.ui.error.asUiText
import com.qodein.core.ui.preview.ServicePreviewData
import com.qodein.core.ui.state.ServiceSelectionUiAction
import com.qodein.core.ui.state.ServiceSelectionUiState
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.component.ProgressIndicator
import com.qodein.feature.promocode.submission.component.SubmissionStepCard
import com.qodein.feature.promocode.submission.component.WizardController
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.service.selection.SelectionState

// MARK: - Constants

private object ScreenConstants {
    val LARGE_SCREEN_THRESHOLD = 800.dp
    val MEDIUM_SCREEN_THRESHOLD = 600.dp
    const val BACKGROUND_ALPHA = 0.3f
}

// MARK: - Main Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromocodeSubmissionScreen(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean,
    viewModel: PromocodeSubmissionViewModel = hiltViewModel()
) {
    TrackScreenViewEvent(screenName = "SubmissionScreen")

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle(initialValue = null)
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle error string outside LaunchedEffect since asUiText is @Composable
    val errorMessage = (events as? PromocodeSubmissionEvent.ShowError)?.error?.asUiText()

    LaunchedEffect(events, errorMessage) {
        when (events) {
            PromocodeSubmissionEvent.NavigateBack -> onNavigateBack()
            PromocodeSubmissionEvent.PromoCodeSubmitted -> onNavigateBack()
            is PromocodeSubmissionEvent.ShowError -> {
                errorMessage?.let {
                    snackbarHostState.showSnackbar(
                        message = it,
                        withDismissAction = true,
                    )
                }
            }
            null -> { /* No event */ }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val currentState = uiState) {
                is PromocodeSubmissionUiState.Loading -> {
                    LoadingState()
                }
                is PromocodeSubmissionUiState.Success -> {
                    // MARK: Authentication check
                    val showAuthSheet = currentState.authentication !is PromocodeSubmissionAuthenticationState.Authenticated
                    if (showAuthSheet) {
                        val isSigningIn = currentState.authentication is PromocodeSubmissionAuthenticationState.Loading

                        AuthenticationBottomSheet(
                            authPromptAction = AuthPromptAction.SubmitPromoCode,
                            onSignInClick = { viewModel.onAction(PromocodeSubmissionAction.SignInWithGoogle) },
                            onDismiss = { viewModel.onAction(PromocodeSubmissionAction.DismissAuthSheet) },
                            isLoading = isSigningIn,
                            isDarkTheme = isDarkTheme,
                        )
                    } else {
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
                            snackbarHostState = snackbarHostState,
                        )

                        // Always render the bottom sheet, visibility controlled by sheetState
                        if (currentState.showServiceSelector) {
                            var isSearchFocused by remember { mutableStateOf(false) }

                            // Get unified service selection state from ViewModel
                            val serviceSelectionState by viewModel.serviceSelectionState.collectAsStateWithLifecycle()
                            // Get cached services from coordinator
                            val cachedServices by viewModel.cachedServices.collectAsStateWithLifecycle()

                            // Use different sheet state based on focus/search mode
                            val adjustedSheetState = rememberModalBottomSheetState(
                                skipPartiallyExpanded = isSearchFocused || serviceSelectionState.search.isSearching,
                            )

                            // Update selection state with current wizard selection
                            val updatedSelectionState = serviceSelectionState.copy(
                                selection = SelectionState.Single(selectedId = currentState.wizardFlow.wizardData.selectedService?.id),
                            )

                            val uiState = ServiceSelectionUiState(
                                domainState = updatedSelectionState,
                                allServices = cachedServices,
                                isVisible = true,
                                isSearchFocused = isSearchFocused,
                            )

                            ServiceSelectorBottomSheet(
                                state = uiState,
                                sheetState = adjustedSheetState,
                                onAction = { uiAction ->
                                    when (uiAction) {
                                        is ServiceSelectionUiAction.UpdateQuery -> {
                                            viewModel.onAction(PromocodeSubmissionAction.SearchServices(uiAction.query))
                                        }
                                        ServiceSelectionUiAction.ClearQuery -> {
                                            viewModel.onAction(PromocodeSubmissionAction.SearchServices(""))
                                        }
                                        is ServiceSelectionUiAction.SelectService -> {
                                            viewModel.onAction(PromocodeSubmissionAction.SelectService(uiAction.service))
                                            viewModel.onAction(PromocodeSubmissionAction.HideServiceSelector)
                                        }
                                        is ServiceSelectionUiAction.SetSearchFocus -> {
                                            isSearchFocused = uiAction.focused
                                        }
                                        ServiceSelectionUiAction.Dismiss -> {
                                            viewModel.onAction(PromocodeSubmissionAction.HideServiceSelector)
                                        }
                                        else -> {
                                            // Handle other UI actions if needed
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
                is PromocodeSubmissionUiState.Error -> {
                    ErrorState(
                        error = currentState.errorType,
                        onRetry = { viewModel.onAction(PromocodeSubmissionAction.RetryClicked) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmissionContent(
    uiState: PromocodeSubmissionUiState.Success,
    onAction: (PromocodeSubmissionAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current

    // Track keyboard visibility by detecting IME padding changes
    var isKeyboardVisible by remember { mutableStateOf(false) }

    // Responsive spacing based on screen height
    val screenHeightDp = configuration.screenHeightDp.dp
    val adaptiveSpacing = when {
        screenHeightDp > ScreenConstants.LARGE_SCREEN_THRESHOLD -> SpacingTokens.xl // Large screens: generous spacing
        screenHeightDp > ScreenConstants.MEDIUM_SCREEN_THRESHOLD -> SpacingTokens.lg // Medium screens: standard spacing
        else -> SpacingTokens.md // Small screens: compact spacing
    }

    val verticalSpacing = when {
        screenHeightDp > ScreenConstants.LARGE_SCREEN_THRESHOLD -> SpacingTokens.lg // More space between components on large screens
        else -> SpacingTokens.md // Compact spacing on smaller screens
    }

    // Clean, single-layer design with floating controller overlay
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = ScreenConstants.BACKGROUND_ALPHA),
            ),
    ) {
        // Main content - no bottom padding reserved for controller
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = adaptiveSpacing)
                .padding(vertical = verticalSpacing)
                .padding(bottom = 100.dp) // Account for floating controller height + extra breathing room
                .imePadding(), // Apply IME padding to the content area
            verticalArrangement = Arrangement.spacedBy(verticalSpacing, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProgressIndicator(
                currentStep = uiState.wizardFlow.currentStep,
                onStepClick = { step ->
                    onAction(PromocodeSubmissionAction.NavigateToStep(step))
                },
                modifier = Modifier.fillMaxWidth(),
            )

            SubmissionStepCard(
                currentStep = uiState.wizardFlow.currentStep,
                wizardData = uiState.wizardFlow.wizardData,
                onAction = onAction,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Floating controller at bottom - hidden when keyboard is active
        WizardController(
            canGoNext = uiState.canGoNext,
            canGoBack = uiState.canGoPrevious,
            isLoading = uiState.submission is PromocodeSubmissionState.Submitting,
            nextButtonText = stringResource(R.string.action_continue),
            onNext = {
                onAction(PromocodeSubmissionAction.NextProgressiveStep)
            },
            onPrevious = {
                onAction(PromocodeSubmissionAction.PreviousProgressiveStep)
            },
            canSubmit = uiState.canSubmit,
            onSubmit = {
                onAction(PromocodeSubmissionAction.SubmitPromoCode)
            },
            showSubmitAlongside = uiState.wizardFlow.currentStep.isLastRequired && !uiState.wizardFlow.currentStep.isLast,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
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
    error: OperationError,
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
            error = error,
            onRetry = onRetry,
            onDismiss = {},
        )
    }
}

// MARK: - Previews

@Preview(name = "Progressive Submission Content - Service Step", showBackground = true)
@Composable
private fun ProgressiveSubmissionContentServicePreview() {
    QodeTheme {
        SubmissionContent(
            uiState = PromocodeSubmissionUiState.Success.initial().copy(
                wizardFlow = WizardFlowState(
                    wizardData = SubmissionWizardData(),
                    currentStep = PromocodeSubmissionStep.SERVICE,
                ),
                authentication = PromocodeSubmissionAuthenticationState.Unauthenticated,
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview(name = "Progressive Submission Content - Promo Code Step", showBackground = true)
@Composable
private fun ProgressiveSubmissionContentPromoCodePreview() {
    QodeTheme {
        SubmissionContent(
            uiState = PromocodeSubmissionUiState.Success.initial().copy(
                wizardFlow = WizardFlowState(
                    wizardData = SubmissionWizardData(
                        selectedService = ServicePreviewData.netflix,
                        promoCodeType = PromoCodeType.PERCENTAGE,
                    ),
                    currentStep = PromocodeSubmissionStep.PROMO_CODE,
                ),
                authentication = PromocodeSubmissionAuthenticationState.Unauthenticated,
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
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

@ThemePreviews
@Composable
private fun SubmissionContentDarkThemePreview() {
    QodeTheme {
        SubmissionContent(
            uiState = PromocodeSubmissionUiState.Success.initial().copy(
                wizardFlow = WizardFlowState(
                    wizardData = SubmissionWizardData(
                        selectedService = ServicePreviewData.netflix,
                        promoCodeType = PromoCodeType.PERCENTAGE,
                    ),
                    currentStep = PromocodeSubmissionStep.DISCOUNT_VALUE,
                ),
                authentication = PromocodeSubmissionAuthenticationState.Unauthenticated,
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
