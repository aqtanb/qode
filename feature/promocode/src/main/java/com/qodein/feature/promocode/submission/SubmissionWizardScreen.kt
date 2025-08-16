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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.designsystem.component.AutoHidingTopAppBar
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.component.QodeHeroGradient
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.step1.ServiceAndTypeScreen
import com.qodein.feature.promocode.submission.step2.TypeDetailsScreen
import com.qodein.feature.promocode.submission.step3.DateSettingsScreen
import com.qodein.feature.promocode.submission.step4.OptionalDetailsScreen

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

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        QodeHeroGradient()

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
}

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
        QodeCard(variant = QodeCardVariant.Outlined) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.error_something_went_wrong),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(SpacingTokens.sm))

                Text(
                    text = error.message ?: stringResource(R.string.error_unexpected),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(SpacingTokens.lg))

                QodeButton(
                    onClick = onRetry,
                    text = stringResource(R.string.action_try_again),
                )
            }
        }
    }
}

@Composable
private fun WizardMainContent(
    uiState: SubmissionWizardUiState.Success,
    onAction: (SubmissionWizardAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(SpacingTokens.lg),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            Spacer(modifier = Modifier.height(SpacingTokens.xxxl))

            // Simple progress bar
            val progress = uiState.currentStep.stepNumber / SubmissionWizardStep.entries.size.toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                strokeCap = StrokeCap.Round,
            )

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

            // Premium navigation buttons
            WizardNavigationButtons(
                uiState = uiState,
                onAction = onAction,
            )
        }

        // AutoHiding TopAppBar overlay like ProfileScreen
        AutoHidingTopAppBar(
            scrollState = scrollState,
            navigationIcon = QodeActionIcons.Back,
            onNavigationClick = onNavigateBack,
            navigationIconTint = MaterialTheme.colorScheme.onPrimaryContainer,
            title = when (uiState.currentStep) {
                SubmissionWizardStep.SERVICE_AND_TYPE -> stringResource(R.string.wizard_step_service_type)
                SubmissionWizardStep.TYPE_DETAILS -> stringResource(R.string.wizard_step_details)
                SubmissionWizardStep.DATE_SETTINGS -> stringResource(R.string.wizard_step_dates)
                SubmissionWizardStep.OPTIONAL_DETAILS -> stringResource(R.string.wizard_step_optional)
            },
        )
    }
}

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

@Composable
private fun WizardNavigationButtons(
    uiState: SubmissionWizardUiState.Success,
    onAction: (SubmissionWizardAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        // Back Button (fixed width, only when needed)
        if (uiState.canGoPrevious) {
            QodeButton(
                onClick = { onAction(SubmissionWizardAction.GoToPreviousStep) },
                text = stringResource(R.string.action_back),
                variant = QodeButtonVariant.Outlined,
                modifier = Modifier.width(120.dp),
                enabled = !uiState.isSubmitting,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Dynamic Next/Submit Button (fixed width)
        val isLastStep = uiState.currentStep == SubmissionWizardStep.OPTIONAL_DETAILS
        QodeButton(
            onClick = {
                if (isLastStep) {
                    onAction(SubmissionWizardAction.SubmitPromoCode)
                } else {
                    onAction(SubmissionWizardAction.GoToNextStep)
                }
            },
            text = if (isLastStep) stringResource(R.string.action_submit) else stringResource(R.string.action_continue),
            modifier = Modifier.width(160.dp),
            enabled = if (isLastStep) uiState.canSubmit else uiState.canGoNext,
            loading = uiState.isSubmitting,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SubmissionWizardScreenPreview() {
    QodeTheme {
        SubmissionWizardScreen(
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WizardProgressIndicatorPreview() {
    QodeTheme {
        WizardProgressIndicator(
            currentStep = SubmissionWizardStep.TYPE_DETAILS,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WizardNavigationButtonsPreview() {
    QodeTheme {
        WizardNavigationButtons(
            uiState = SubmissionWizardUiState.Success(
                currentStep = SubmissionWizardStep.TYPE_DETAILS,
                wizardData = SubmissionWizardData(
                    serviceName = "Netflix",
                    promoCodeType = PromoCodeType.PERCENTAGE,
                ),
            ),
            onAction = {},
        )
    }
}
