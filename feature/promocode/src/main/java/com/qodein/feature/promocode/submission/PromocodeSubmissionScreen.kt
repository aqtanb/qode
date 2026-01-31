package com.qodein.feature.promocode.submission

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.component.QodeinBackIconButton
import com.qodein.core.designsystem.component.QodeinTopAppBar
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.ServicePreviewData
import com.qodein.core.ui.text.asString
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.component.PromocodeSubmissionBottomToolbar
import com.qodein.feature.promocode.submission.component.PromocodeSubmissionCard
import com.qodein.feature.promocode.submission.component.ServiceConfirmationDialog
import com.qodein.feature.promocode.submission.component.WizardController
import com.qodein.feature.promocode.submission.wizard.PromocodeWizardStep
import com.qodein.feature.promocode.submission.wizard.indicatorRes
import com.qodein.feature.promocode.submission.wizard.stepIcon
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.ServiceId
import org.koin.androidx.compose.koinViewModel

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromocodeSubmissionRoute(
    onNavigateBack: () -> Unit,
    onPromocodeSubmitted: () -> Unit,
    onShowServiceSelection: (ServiceId?) -> Unit,
    viewModel: PromocodeSubmissionViewModel = koinViewModel()
) {
    TrackScreenViewEvent(screenName = "SubmissionScreen")

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = Promocode.MAX_IMAGES),
    ) { uris ->
        viewModel.onAction(PromocodeSubmissionAction.UpdateImageUris(uris.map { it.toString() }))
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(Unit) {
        viewModel.events.flowWithLifecycle(lifecycle).collect { event ->
            when (event) {
                PromocodeSubmissionEvent.NavigateBack -> onNavigateBack()
                PromocodeSubmissionEvent.PromocodeSubmitted -> onPromocodeSubmitted()
                is PromocodeSubmissionEvent.ShowError -> snackbarHostState.showSnackbar(
                    message = event.message.asString(context),
                    withDismissAction = true,
                )
                is PromocodeSubmissionEvent.ShowServiceSelection -> onShowServiceSelection(event.currentSelectedService)
                PromocodeSubmissionEvent.PickImagesRequested -> pickMediaLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
                PromocodeSubmissionEvent.ImageLimitReached -> snackbarHostState.showSnackbar(
                    message = context.getString(R.string.promocode_image_limit_reached),
                    withDismissAction = true,
                )
                is PromocodeSubmissionEvent.ImagesPartiallyAdded -> snackbarHostState.showSnackbar(
                    message = context.getString(R.string.promocode_images_partially_added, event.count),
                    withDismissAction = true,
                )
            }
        }
    }

    PromocodeSubmissionScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack = onNavigateBack,
        onAction = viewModel::onAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PromocodeSubmissionScreen(
    uiState: PromocodeSubmissionUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onAction: (PromocodeSubmissionAction) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            QodeinTopAppBar(
                title = stringResource(uiState.currentStep.indicatorRes),
                navigationIcon = { QodeinBackIconButton({ onNavigateBack() }) },
            )
        },
    ) { paddingValues ->
        SubmissionContent(
            uiState = uiState,
            onAction = onAction,
            modifier = Modifier.padding(paddingValues),
        )
    }
}

@Composable
private fun SubmissionContent(
    uiState: PromocodeSubmissionUiState,
    onAction: (PromocodeSubmissionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = modifier.fillMaxSize(),
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
                currentStep = uiState.currentStep,
                onStepClick = { step -> onAction(PromocodeSubmissionAction.NavigateToStep(step)) },
                modifier = Modifier.padding(SpacingTokens.xs),
            )

            PromocodeSubmissionCard(
                currentStep = uiState.currentStep,
                wizardData = uiState.wizardData,
                onAction = onAction,
                modifier = Modifier.padding(SpacingTokens.xs),
            )

            if (uiState.currentStep == PromocodeWizardStep.DESCRIPTION) {
                PromocodeSubmissionBottomToolbar(
                    onClick = { onAction(PromocodeSubmissionAction.PickImages) },
                    disable = uiState.wizardData.imageUris.size >= Promocode.MAX_IMAGES,
                )
            }

            Spacer(modifier = Modifier.padding(bottom = SpacingTokens.gigantic))
        }

        WizardController(
            canGoNext = uiState.canGoNext,
            canGoBack = uiState.canGoPrevious,
            isLoading = uiState.isCompressing,
            onNext = { onAction(PromocodeSubmissionAction.NextProgressiveStep) },
            onPrevious = { onAction(PromocodeSubmissionAction.PreviousProgressiveStep) },
            canSubmit = uiState.canSubmit,
            onSubmit = { onAction(PromocodeSubmissionAction.SubmitPromoCode) },
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProgressIndicator(
    currentStep: PromocodeWizardStep,
    onStepClick: ((PromocodeWizardStep) -> Unit),
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.md),
            horizontalArrangement = Arrangement.spacedBy(
                ButtonGroupDefaults.ConnectedSpaceBetween,
                Alignment.CenterHorizontally,
            ),
        ) {
            PromocodeWizardStep.entries.forEachIndexed { index, step ->
                val isCompleted = step.stepNumber < currentStep.stepNumber
                val isCurrent = step.stepNumber == currentStep.stepNumber
                val isEnabled = isCompleted || isCurrent

                val animatedWeight by animateFloatAsState(
                    targetValue = if (isCurrent) 2f else 1f,
                    animationSpec = tween(durationMillis = 700),
                    label = "stepWeight",
                )

                ToggleButton(
                    checked = isCurrent,
                    onCheckedChange = { if (isEnabled) onStepClick(step) },
                    modifier = Modifier.weight(animatedWeight),
                    enabled = isEnabled,
                    shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        PromocodeWizardStep.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    colors = ToggleButtonDefaults.tonalToggleButtonColors(
                        containerColor = when {
                            isCompleted -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = when {
                            isCompleted -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    ),
                ) {
                    Icon(
                        imageVector = step.stepIcon(isCompleted),
                        contentDescription = stringResource(step.indicatorRes),
                        modifier = Modifier.size(ToggleButtonDefaults.IconSize),
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SubmissionContentPreview() {
    QodeTheme {
        PromocodeSubmissionScreen(
            uiState = PromocodeSubmissionUiState(
                currentStep = PromocodeWizardStep.DISCOUNT_VALUE,
                wizardData = SubmissionWizardData(
                    selectedService = ServicePreviewData.netflix,
                    promocodeType = PromocodeType.PERCENTAGE,
                ),
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateBack = {},
        )
    }
}
