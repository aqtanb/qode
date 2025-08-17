package com.qodein.feature.promocode.submission

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.designsystem.component.QodeErrorCard
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.model.Service
import com.qodein.core.model.ServiceId
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.step1.ServiceAndTypeScreen
import com.qodein.feature.promocode.submission.step2.TypeDetailsScreen
import com.qodein.feature.promocode.submission.step3.DateSettingsScreen
import com.qodein.feature.promocode.submission.step4.OptionalDetailsScreen
import java.time.LocalDate

// MARK: - Main Screen Components

@Composable
fun SubmissionWizardScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubmissionWizardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle(initialValue = null)

    // Handle events
    LaunchedEffect(events) {
        when (events) {
            SubmissionWizardEvent.NavigateBack -> onNavigateBack()
            SubmissionWizardEvent.PromoCodeSubmitted -> {
                // Could show a success message here
            }
            null -> { /* No event */ }
        }
    }

    // Main content
    when (val currentState = uiState) {
        is SubmissionWizardUiState.Loading -> {
            WizardLoadingContent()
        }
        is SubmissionWizardUiState.Success -> {
            WizardMainContent(
                uiState = currentState,
                onAction = viewModel::onAction,
                onNavigateBack = onNavigateBack,
            )
        }
        is SubmissionWizardUiState.Error -> {
            WizardErrorContent(
                error = currentState.exception,
                onRetry = { viewModel.onAction(SubmissionWizardAction.RetryClicked) },
            )
        }
    }
}

// MARK: - Content States

@Composable
private fun WizardLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            // Premium loading indicator with glass effect
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Text(
                text = stringResource(R.string.wizard_loading),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun WizardErrorContent(
    error: Throwable,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingTokens.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        QodeErrorCard(
            message = error.message ?: "Error",
            onRetry = onRetry,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WizardMainContent(
    uiState: SubmissionWizardUiState.Success,
    onAction: (SubmissionWizardAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState.currentStep) {
                            SubmissionWizardStep.SERVICE_AND_TYPE -> stringResource(R.string.wizard_step_service_type)
                            SubmissionWizardStep.TYPE_DETAILS -> stringResource(R.string.wizard_step_details)
                            SubmissionWizardStep.DATE_SETTINGS -> stringResource(R.string.wizard_step_dates)
                            SubmissionWizardStep.OPTIONAL_DETAILS -> stringResource(R.string.wizard_step_optional)
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = QodeActionIcons.Back,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.statusBarsPadding(),
            )
        },
    ) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(contentPadding)
                    .padding(
                        start = SpacingTokens.lg,
                        end = SpacingTokens.lg,
                        top = SpacingTokens.lg,
                        bottom = SpacingTokens.lg,
                    ),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
            ) {
                // Animated step content with smooth transitions
                AnimatedContent(
                    targetState = uiState.currentStep,
                    transitionSpec = {
                        slideInHorizontally(
                            initialOffsetX = { if (targetState.stepNumber > initialState.stepNumber) it else -it },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow,
                            ),
                        ) + fadeIn() togetherWith slideOutHorizontally(
                            targetOffsetX = { if (targetState.stepNumber > initialState.stepNumber) -it else it },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow,
                            ),
                        ) + fadeOut()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = "wizard_step_transition",
                ) { step ->
                    when (step) {
                        SubmissionWizardStep.SERVICE_AND_TYPE -> {
                            ServiceAndTypeScreen(
                                wizardData = uiState.wizardData,
                                onAction = onAction,
                                availableServices = uiState.availableServices,
                                isLoadingServices = uiState.isLoadingServices,
                                onSearchServices = { query ->
                                    onAction(SubmissionWizardAction.SearchServices(query))
                                },
                            )
                        }
                        SubmissionWizardStep.TYPE_DETAILS -> {
                            TypeDetailsScreen(
                                wizardData = uiState.wizardData,
                                onAction = onAction,
                            )
                        }
                        SubmissionWizardStep.DATE_SETTINGS -> {
                            DateSettingsScreen(
                                wizardData = uiState.wizardData,
                                onAction = onAction,
                            )
                        }
                        SubmissionWizardStep.OPTIONAL_DETAILS -> {
                            OptionalDetailsScreen(
                                wizardData = uiState.wizardData,
                                onAction = onAction,
                            )
                        }
                    }
                }
            }

            // Bottom Controller (floating at bottom)
            Box(
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                BottomWizardController(
                    uiState = uiState,
                    onAction = onAction,
                    onFocusField = { fieldName ->
                        // TODO: Implement field focus logic
                    },
                )
            }
        }
    }
}

// MARK: - UI Components

@Composable
private fun WizardProgressIndicator(
    currentStep: SubmissionWizardStep,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Animated progress bar
        val progress by animateFloatAsState(
            targetValue = currentStep.stepNumber / SubmissionWizardStep.entries.size.toFloat(),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
            label = "progress_animation",
        )

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            strokeCap = StrokeCap.Round,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        )

        Spacer(modifier = Modifier.height(SpacingTokens.md))

        // Elegant step indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SubmissionWizardStep.entries.forEach { step ->
                AnimatedStepIndicator(
                    step = step,
                    isActive = step == currentStep,
                    isCompleted = step.stepNumber < currentStep.stepNumber,
                )
            }
        }

        Spacer(modifier = Modifier.height(SpacingTokens.sm))

        // Progress text
        Text(
            text = stringResource(R.string.wizard_step_progress, currentStep.stepNumber, SubmissionWizardStep.entries.size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AnimatedStepIndicator(
    step: SubmissionWizardStep,
    isActive: Boolean,
    isCompleted: Boolean
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isCompleted -> MaterialTheme.colorScheme.primary
            isActive -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300),
        label = "background_color",
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isCompleted -> MaterialTheme.colorScheme.onPrimary
            isActive -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(300),
        label = "text_color",
    )

    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "scale_animation",
    )

    Card(
        modifier = Modifier
            .size(40.dp)
            .scale(scale),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 4.dp else 2.dp,
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Text(
                    text = step.stepNumber.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// MARK: - Preview Helpers

private object PreviewData {
    val sampleServices = listOf(
        Service(
            id = ServiceId("1"),
            name = "Netflix",
            category = "Streaming",
            isPopular = true,
        ),
        Service(
            id = ServiceId("2"),
            name = "Spotify",
            category = "Music",
            isPopular = true,
        ),
        Service(
            id = ServiceId("3"),
            name = "Kaspi",
            category = "Finance",
            isPopular = false,
        ),
    )

    val emptyWizardData = SubmissionWizardData()

    val step1CompletedData = SubmissionWizardData(
        serviceName = "Netflix",
        promoCodeType = PromoCodeType.PERCENTAGE,
    )

    val step2CompletedData = SubmissionWizardData(
        serviceName = "Netflix",
        promoCodeType = PromoCodeType.PERCENTAGE,
        promoCode = "SAVE20",
        discountPercentage = "20",
        minimumOrderAmount = "15",
    )

    val step3CompletedData = SubmissionWizardData(
        serviceName = "Netflix",
        promoCodeType = PromoCodeType.PERCENTAGE,
        promoCode = "SAVE20",
        discountPercentage = "20",
        minimumOrderAmount = "15",
        startDate = LocalDate.now(),
        endDate = LocalDate.now().plusDays(30),
    )

    val step4CompletedData = SubmissionWizardData(
        serviceName = "Netflix",
        promoCodeType = PromoCodeType.PERCENTAGE,
        promoCode = "SAVE20",
        discountPercentage = "20",
        minimumOrderAmount = "15",
        startDate = LocalDate.now(),
        endDate = LocalDate.now().plusDays(30),
        title = "20% off Netflix Premium",
        description = "Limited time offer for new subscribers",
        screenshotUrl = "https://example.com/screenshot.jpg",
    )

    val fixedAmountData = SubmissionWizardData(
        serviceName = "Spotify",
        promoCodeType = PromoCodeType.FIXED_AMOUNT,
        promoCode = "SAVE5",
        discountAmount = "5",
        minimumOrderAmount = "10",
    )

    fun createSuccessState(
        step: SubmissionWizardStep,
        wizardData: SubmissionWizardData = emptyWizardData,
        services: List<Service> = sampleServices,
        isLoadingServices: Boolean = false
    ) = SubmissionWizardUiState.Success(
        currentStep = step,
        wizardData = wizardData,
        availableServices = services,
        isLoadingServices = isLoadingServices,
    )
}

// MARK: - Previews

@Preview(name = "Loading State", showBackground = true)
@Composable
private fun WizardLoadingPreview() {
    QodeTheme {
        WizardLoadingContent()
    }
}

@Preview(name = "Error State", showBackground = true)
@Composable
private fun WizardErrorPreview() {
    QodeTheme {
        WizardErrorContent(
            error = Exception("Network connection failed"),
            onRetry = {},
        )
    }
}

@Preview(name = "Step 1 - Empty", showBackground = true)
@Composable
private fun WizardStep1EmptyPreview() {
    QodeTheme {
        WizardMainContent(
            uiState = PreviewData.createSuccessState(
                step = SubmissionWizardStep.SERVICE_AND_TYPE,
                wizardData = PreviewData.emptyWizardData,
            ),
            onAction = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "Step 1 - Completed", showBackground = true)
@Composable
private fun WizardStep1CompletedPreview() {
    QodeTheme {
        WizardMainContent(
            uiState = PreviewData.createSuccessState(
                step = SubmissionWizardStep.SERVICE_AND_TYPE,
                wizardData = PreviewData.step1CompletedData,
            ),
            onAction = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "Step 2 - Percentage", showBackground = true)
@Composable
private fun WizardStep2PercentagePreview() {
    QodeTheme {
        WizardMainContent(
            uiState = PreviewData.createSuccessState(
                step = SubmissionWizardStep.TYPE_DETAILS,
                wizardData = PreviewData.step2CompletedData,
            ),
            onAction = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "Step 2 - Fixed Amount", showBackground = true)
@Composable
private fun WizardStep2FixedAmountPreview() {
    QodeTheme {
        WizardMainContent(
            uiState = PreviewData.createSuccessState(
                step = SubmissionWizardStep.TYPE_DETAILS,
                wizardData = PreviewData.fixedAmountData,
            ),
            onAction = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "Step 3 - Date Settings", showBackground = true)
@Composable
private fun WizardStep3Preview() {
    QodeTheme {
        WizardMainContent(
            uiState = PreviewData.createSuccessState(
                step = SubmissionWizardStep.DATE_SETTINGS,
                wizardData = PreviewData.step3CompletedData,
            ),
            onAction = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "Step 4 - Optional Details", showBackground = true)
@Composable
private fun WizardStep4Preview() {
    QodeTheme {
        WizardMainContent(
            uiState = PreviewData.createSuccessState(
                step = SubmissionWizardStep.OPTIONAL_DETAILS,
                wizardData = PreviewData.step4CompletedData,
            ),
            onAction = {},
            onNavigateBack = {},
        )
    }
}

@Preview(name = "Progress Indicator", showBackground = true)
@Composable
private fun WizardProgressIndicatorPreview() {
    QodeTheme {
        WizardProgressIndicator(
            currentStep = SubmissionWizardStep.TYPE_DETAILS,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Bottom Controller - Step 1", showBackground = true)
@Composable
private fun BottomControllerStep1Preview() {
    QodeTheme {
        BottomWizardController(
            uiState = PreviewData.createSuccessState(
                step = SubmissionWizardStep.SERVICE_AND_TYPE,
                wizardData = PreviewData.step1CompletedData,
            ),
            onAction = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Bottom Controller - Step 4", showBackground = true)
@Composable
private fun BottomControllerStep4Preview() {
    QodeTheme {
        BottomWizardController(
            uiState = PreviewData.createSuccessState(
                step = SubmissionWizardStep.OPTIONAL_DETAILS,
                wizardData = PreviewData.step4CompletedData,
            ),
            onAction = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
