package com.qodein.feature.promocode.submission

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.component.ButtonSize
import com.qodein.core.designsystem.component.QodeinBackIconButton
import com.qodein.core.designsystem.component.QodeinIconButton
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
import com.qodein.feature.promocode.submission.wizard.titleRes
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
                title = stringResource(uiState.currentStep.titleRes),
                navigationIcon = { QodeinBackIconButton({ onNavigateBack() }) },
            )
        },
        bottomBar = {
            if (uiState.currentStep == PromocodeWizardStep.DESCRIPTION) {
                PromocodeSubmissionBottomToolbar(
                    disable = uiState.wizardData.imageUris.size >= Promocode.MAX_IMAGES,
                    onClick = { onAction(PromocodeSubmissionAction.PickImages) },
                    modifier = Modifier.navigationBarsPadding(),
                )
            }
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

@Composable
fun ProgressIndicator(
    currentStep: PromocodeWizardStep,
    onStepClick: ((PromocodeWizardStep) -> Unit),
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(SpacingTokens.md),
            verticalAlignment = Alignment.Top,
            contentPadding = PaddingValues(horizontal = SpacingTokens.sm),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            itemsIndexed(PromocodeWizardStep.entries) { index, step ->
                val isCompleted = step.stepNumber < currentStep.stepNumber
                val isCurrent = step.stepNumber == currentStep.stepNumber

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    StepIcon(
                        step = step,
                        isCompleted = isCompleted,
                        isCurrent = isCurrent,
                        onClick = if (isCompleted || isCurrent) {
                            { onStepClick(step) }
                        } else {
                            null
                        },
                    )

                    Text(
                        text = stringResource(step.indicatorRes),
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isCurrent -> MaterialTheme.colorScheme.secondary
                            isCompleted -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center,
                        fontWeight = if (isCurrent) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        },
                        modifier = Modifier.padding(top = SpacingTokens.xs),
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIcon(
    step: PromocodeWizardStep,
    isCompleted: Boolean,
    isCurrent: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val isOptional = !step.isRequired

    val containerColor = when {
        isCompleted -> MaterialTheme.colorScheme.primary
        isCurrent -> if (isOptional) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        isCompleted -> MaterialTheme.colorScheme.onPrimary
        isCurrent -> if (isOptional) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        }
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    QodeinIconButton(
        onClick = { onClick?.invoke() },
        icon = step.stepIcon(isCompleted),
        contentDescription = stringResource(step.titleRes),
        size = ButtonSize.Small,
        enabled = isCompleted || isCurrent,
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier,
    )
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
