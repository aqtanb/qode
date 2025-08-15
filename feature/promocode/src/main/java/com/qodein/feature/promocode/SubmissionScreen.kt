package com.qodein.feature.promocode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeTextField
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens

@Composable
fun SubmissionScreen(
    onNavigateBack: () -> Unit,
    onPromoCodeSubmitted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SubmissionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                SubmissionEvent.NavigateBack -> onNavigateBack()
                SubmissionEvent.PromoCodeSubmitted -> onPromoCodeSubmitted()
            }
        }
    }

    when (val currentState = uiState) {
        SubmissionUiState.Loading -> {
            LoadingState()
        }
        is SubmissionUiState.Success -> {
            SubmissionContent(
                uiState = currentState,
                onAction = viewModel::onAction,
                modifier = modifier,
            )
        }
        is SubmissionUiState.Error -> {
            ErrorState(
                exception = currentState.exception,
                onRetry = { viewModel.onAction(SubmissionAction.RetryClicked) },
            )
        }
    }
}

@Composable
private fun SubmissionContent(
    uiState: SubmissionUiState.Success,
    onAction: (SubmissionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingTokens.lg),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        Text(
            text = "Submit Promo Code",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        QodeTextField(
            value = uiState.serviceName,
            onValueChange = { onAction(SubmissionAction.UpdateServiceName(it)) },
            label = "Service Name",
            placeholder = "e.g., Netflix, Kaspi, Glovo",
        )

        QodeTextField(
            value = uiState.promoCode,
            onValueChange = { onAction(SubmissionAction.UpdatePromoCode(it)) },
            label = "Promo Code",
            placeholder = "e.g., SAVE20, DISCOUNT50",
        )

        QodeTextField(
            value = uiState.description,
            onValueChange = { onAction(SubmissionAction.UpdateDescription(it)) },
            label = "Description (Optional)",
            placeholder = "Describe what this promo code offers",
        )

        QodeButton(
            onClick = { onAction(SubmissionAction.SubmitPromoCode) },
            text = if (uiState.isSubmitting) "Submitting..." else "Submit",
            variant = QodeButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.canSubmit,
            loading = uiState.isSubmitting,
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    exception: Throwable,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SpacingTokens.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = exception.message ?: "Unknown error occurred",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = SpacingTokens.md),
        )

        QodeButton(
            onClick = onRetry,
            text = "Try Again",
            variant = QodeButtonVariant.Primary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SubmissionScreenPreview() {
    QodeTheme {
        SubmissionContent(
            uiState = SubmissionUiState.Success(
                serviceName = "Netflix",
                promoCode = "SAVE50",
            ),
            onAction = {},
        )
    }
}
