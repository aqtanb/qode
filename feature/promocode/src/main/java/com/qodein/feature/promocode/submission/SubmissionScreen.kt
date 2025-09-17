package com.qodein.feature.promocode.submission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.qodein.feature.promocode.submission.component.BottomController
import com.qodein.feature.promocode.submission.component.CurrentStepContent
import com.qodein.feature.promocode.submission.component.StepWithHint
import com.qodein.feature.promocode.submission.component.StepsStack
import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.toErrorType
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.auth.AuthStateManager
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SubmissionEntryPoint {
    fun authStateManager(): AuthStateManager
    fun signInWithGoogleUseCase(): SignInWithGoogleUseCase
}

// MARK: - Main Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubmissionWizardViewModel = hiltViewModel()
) {
    TrackScreenViewEvent(screenName = "ProgressiveSubmission")

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Get auth dependencies via EntryPoint
    val entryPoint = remember {
        EntryPointAccessors.fromApplication(context, SubmissionEntryPoint::class.java)
    }
    val authStateManager = remember { entryPoint.authStateManager() }
    val signInWithGoogleUseCase = remember { entryPoint.signInWithGoogleUseCase() }

    // Track auth state
    val authState by authStateManager.getAuthState().collectAsStateWithLifecycle(initialValue = AuthState.Loading)

    // Track authentication bottom sheet state
    var showAuthSheet by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    // Check if user is authenticated
    val isAuthenticated = authState is AuthState.Authenticated
    val isAuthLoading = authState is AuthState.Loading

    // Show auth sheet if user is not authenticated and auth is not loading
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Unauthenticated -> {
                showAuthSheet = true
            }
            is AuthState.Authenticated -> {
                showAuthSheet = false
            }
            is AuthState.Loading -> {
                // Keep loading, don't show auth sheet yet
            }
        }
    }

    // Handle sign-in
    val handleSignIn = {
        scope.launch {
            isSigningIn = true
            authError = null

            signInWithGoogleUseCase()
                .onEach { result ->
                    when (result) {
                        is Result.Loading -> {
                            isSigningIn = true
                        }
                        is Result.Success -> {
                            isSigningIn = false
                            showAuthSheet = false
                        }
                        is Result.Error -> {
                            isSigningIn = false
                            authError = result.exception.message ?: "Sign-in failed"
                        }
                    }
                }
                .launchIn(scope)
        }
        Unit
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(events) {
        when (events) {
            SubmissionWizardEvent.NavigateBack -> onNavigateBack()
            SubmissionWizardEvent.PromoCodeSubmitted -> onNavigateBack()
            null -> { /* No event */ }
        }
    }

    // Show loading state while auth is being checked
    if (isAuthLoading) {
        LoadingState()
        return
    }

    // Show authentication bottom sheet when needed
    if (showAuthSheet) {
        AuthenticationBottomSheet(
            action = AuthPromptAction.SubmitPromoCode,
            onSignInClick = handleSignIn,
            onDismiss = {
                // Don't allow dismissing - user must authenticate to access submission
                // showAuthSheet = false
                onNavigateBack()
            },
            isLoading = isSigningIn,
            errorType = authError?.let { RuntimeException(it).toErrorType() },
            onErrorDismissed = {
                authError = null
            },
            isDarkTheme = false, // TODO: Get from theme state
        )
        return
    }

    // Only show the submission content if user is authenticated
    if (!isAuthenticated) {
        return
    }

    when (val currentState = uiState) {
        is SubmissionWizardUiState.Loading -> {
            LoadingState()
        }
        is SubmissionWizardUiState.Success -> {
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
                authState = authState,
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
    authState: AuthState,
    onAction: (SubmissionWizardAction) -> Unit,
    modifier: Modifier = Modifier
) {
    // Since authentication is now handled at the screen level,
    // we can directly proceed with submission without additional auth checks

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomController(
                currentStep = uiState.currentProgressiveStep,
                canProceed = uiState.canGoNextProgressive,
                onNext = {
                    if (uiState.currentProgressiveStep.isLast) {
                        // Submit with authenticated user data
                        val authenticatedState = authState as? AuthState.Authenticated
                        if (authenticatedState != null) {
                            val userData = SubmissionWizardViewModel.UserData(
                                id = authenticatedState.user.id,
                                username = authenticatedState.user.profile.displayName,
                                avatarUrl = authenticatedState.user.profile.photoUrl,
                            )
                            onAction(SubmissionWizardAction.SubmitPromoCodeWithUser(userData))
                        }
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
                .padding(paddingValues),
        ) {
            StepsStack(
                currentStep = uiState.currentProgressiveStep,
                wizardData = uiState.wizardData,
            )

            StepWithHint(
                currentStep = uiState.currentProgressiveStep,
                modifier = modifier
                    .weight(1f)
                    .padding(horizontal = SpacingTokens.lg),
            ) {
                CurrentStepContent(
                    currentStep = uiState.currentProgressiveStep,
                    wizardData = uiState.wizardData,
                    serviceSelectionUiState = uiState.serviceSelectionUiState,
                    onAction = onAction,
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
            uiState = SubmissionWizardUiState.Success(
                wizardData = SubmissionWizardData(),
                currentProgressiveStep = ProgressiveStep.SERVICE,
            ),
            authState = AuthState.Unauthenticated,
            onAction = {},
        )
    }
}

@Preview(name = "Progressive Submission Content - Promo Code Step", showBackground = true)
@Composable
private fun ProgressiveSubmissionContentPromoCodePreview() {
    QodeTheme {
        SubmissionContent(
            uiState = SubmissionWizardUiState.Success(
                wizardData = SubmissionWizardData(
                    selectedService = ServicePreviewData.netflix,
                    promoCodeType = PromoCodeType.PERCENTAGE,
                ),
                currentProgressiveStep = ProgressiveStep.PROMO_CODE,
            ),
            authState = AuthState.Unauthenticated,
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
            uiState = SubmissionWizardUiState.Success(
                wizardData = SubmissionWizardData(
                    selectedService = ServicePreviewData.netflix,
                    promoCodeType = PromoCodeType.PERCENTAGE,
                ),
                currentProgressiveStep = ProgressiveStep.DISCOUNT_VALUE,
            ),
            authState = AuthState.Unauthenticated,
            onAction = {},
        )
    }
}
