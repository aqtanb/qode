package com.qodein.feature.promocode.submission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.AuthPromptAction
import com.qodein.core.ui.component.QodeErrorCard
import com.qodein.core.ui.preview.ServicePreviewData
import com.qodein.core.ui.text.asString
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.component.ProgressIndicator
import com.qodein.feature.promocode.submission.component.PromocodeSubmissionCard
import com.qodein.feature.promocode.submission.component.ServiceConfirmationDialog
import com.qodein.feature.promocode.submission.component.WizardController
import com.qodein.feature.promocode.submission.wizard.PromocodeSubmissionStep
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.model.ServiceId
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromocodeSubmissionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAuth: (AuthPromptAction) -> Unit,
    onShowServiceSelection: (ServiceId?) -> Unit,
    viewModel: PromocodeSubmissionViewModel = koinViewModel()
) {
    TrackScreenViewEvent(screenName = "SubmissionScreen")

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                PromocodeSubmissionEvent.NavigateBack -> onNavigateBack()
                PromocodeSubmissionEvent.PromoCodeSubmitted -> onNavigateBack()
                is PromocodeSubmissionEvent.NavigateToAuth -> {
                    onNavigateToAuth(event.action)
                }
                is PromocodeSubmissionEvent.ShowError -> snackbarHostState.showSnackbar(
                    message = event.message.asString(context),
                    withDismissAction = true,
                )
                is PromocodeSubmissionEvent.ShowServiceSelection -> onShowServiceSelection(event.currentSelectedService)
            }
        }
    }

    PromocodeSubmissionScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PromocodeSubmissionScreenContent(
    uiState: PromocodeSubmissionUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onAction: (PromocodeSubmissionAction) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            QodeTopAppBar(
                title = stringResource(R.string.submit_promocode),
                navigationIcon = QodeActionIcons.Back,
                onNavigationClick = onNavigateBack,
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (uiState) {
                is PromocodeSubmissionUiState.Loading -> {
                    LoadingState()
                }
                is PromocodeSubmissionUiState.Success -> {
                    SuccessState(
                        uiState = uiState,
                        onAction = onAction,
                    )
                }
                is PromocodeSubmissionUiState.Error -> {
                    ErrorState(
                        error = uiState.error,
                        onRetry = { onAction(PromocodeSubmissionAction.RetryClicked) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuccessState(
    uiState: PromocodeSubmissionUiState.Success,
    onAction: (PromocodeSubmissionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(bottom = SpacingTokens.gigantic),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProgressIndicator(
                currentStep = uiState.wizardFlow.currentStep,
                onStepClick = { step ->
                    onAction(PromocodeSubmissionAction.NavigateToStep(step))
                },
                modifier = Modifier.fillMaxWidth(),
            )

            PromocodeSubmissionCard(
                currentStep = uiState.wizardFlow.currentStep,
                wizardData = uiState.wizardFlow.wizardData,
                validation = uiState.validation,
                onAction = onAction,
                modifier = Modifier.fillMaxWidth(),
            )

            val submissionState = uiState.submission
            if (submissionState is PromocodeSubmissionState.Error) {
                Spacer(modifier = Modifier.height(SpacingTokens.md))
                QodeErrorCard(
                    error = submissionState.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SpacingTokens.md),
                )
            }
        }

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
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        uiState.serviceConfirmationDialog?.let { dialogState ->
            ServiceConfirmationDialog(
                serviceName = dialogState.serviceName,
                serviceUrl = dialogState.serviceUrl,
                logoUrl = dialogState.logoUrl,
                onConfirm = { onAction(PromocodeSubmissionAction.ConfirmServiceLogo) },
                onDismiss = { onAction(PromocodeSubmissionAction.DismissServiceConfirmation) },
            )
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
        )
    }
}

@ThemePreviews
@Composable
private fun LoadingStatePreview() {
    QodeTheme {
        LoadingState()
    }
}

@ThemePreviews
@Composable
private fun ErrorStatePreview() {
    QodeTheme {
        ErrorState(
            error = SystemError.Unknown,
            onRetry = { },
        )
    }
}

@ThemePreviews
@Composable
private fun SuccessStatePreview() {
    QodeTheme {
        SuccessState(
            uiState = PromocodeSubmissionUiState.Success.initial().copy(
                wizardFlow = WizardFlowState(
                    wizardData = SubmissionWizardData(
                        selectedService = ServicePreviewData.netflix,
                        promocodeType = PromocodeType.PERCENTAGE,
                    ),
                    currentStep = PromocodeSubmissionStep.DISCOUNT_VALUE,
                ),
            ),
            onAction = {},
        )
    }
}
