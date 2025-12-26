package com.qodein.feature.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.component.QodeinBasicTextField
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.UIIcons
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.error.toUiText
import com.qodein.core.ui.text.asString
import com.qodein.feature.report.component.ReasonSelectionSection
import com.qodein.feature.report.component.ReportItemPreview
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.ReportReason
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    reportedItemId: String,
    reportedItemType: ContentType,
    itemTitle: String,
    itemAuthor: String?,
    onNavigateBack: () -> Unit,
    onReportSubmitted: () -> Unit,
    viewModel: ReportViewModel = koinViewModel {
        parametersOf(reportedItemId, reportedItemType)
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle success state - navigate to home
    LaunchedEffect(uiState) {
        if (uiState is ReportUiState.Success) {
            onReportSubmitted()
        }
    }

    Scaffold(
        topBar = {
            QodeTopAppBar(
                title = stringResource(R.string.report_title),
                navigationIcon = QodeActionIcons.Close,
                onNavigationClick = onNavigateBack,
            )
        },
        floatingActionButton = {
            if (uiState is ReportUiState.Input) {
                val inputState = uiState as ReportUiState.Input
                FloatingActionButton(
                    onClick = { viewModel.onAction(ReportAction.Submit) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    if (inputState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    } else {
                        Icon(
                            imageVector = UIIcons.Report,
                            contentDescription = stringResource(R.string.submit_report),
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        when (val state = uiState) {
            is ReportUiState.Input -> {
                ReportInputContent(
                    reportedItemType = reportedItemType,
                    itemTitle = itemTitle,
                    itemAuthor = itemAuthor,
                    selectedReason = state.selectedReason,
                    additionalDetails = state.additionalDetails,
                    validationErrorResId = state.validationErrorResId,
                    isSubmitting = state.isSubmitting,
                    onAction = viewModel::onAction,
                    modifier = Modifier.padding(paddingValues),
                )
            }
            is ReportUiState.Success -> {
                // This state triggers navigation back via LaunchedEffect
                SuccessContent(modifier = Modifier.padding(paddingValues))
            }
            is ReportUiState.Error -> {
                ErrorContent(
                    error = state.error,
                    onDismiss = { viewModel.onAction(ReportAction.DismissError) },
                    onNavigateBack = onNavigateBack,
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
    }
}

@Composable
private fun ReportInputContent(
    reportedItemType: ContentType,
    itemTitle: String,
    itemAuthor: String?,
    selectedReason: ReportReason?,
    additionalDetails: String,
    validationErrorResId: Int?,
    isSubmitting: Boolean,
    onAction: (ReportAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(SpacingTokens.md),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        ReportItemPreview(
            itemType = reportedItemType,
            itemTitle = itemTitle,
            itemAuthor = itemAuthor,
        )

        HorizontalDivider()

        ReasonSelectionSection(
            selectedReason = selectedReason,
            onReasonSelected = { onAction(ReportAction.SelectReason(it)) },
        )

        AdditionalDetailsSection(
            value = additionalDetails,
            onValueChange = { onAction(ReportAction.UpdateAdditionalDetails(it)) },
            isRequired = selectedReason == ReportReason.OTHER,
            enabled = !isSubmitting,
        )

        if (validationErrorResId != null) {
            Text(
                text = stringResource(validationErrorResId),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(SpacingTokens.xxxl))
    }
}

@Composable
private fun AdditionalDetailsSection(
    value: String,
    onValueChange: (String) -> Unit,
    isRequired: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        Row {
            Text(
                text = stringResource(R.string.additional_details_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isRequired) {
                Text(
                    text = " *",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        QodeinBasicTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = stringResource(
                if (isRequired) {
                    R.string.additional_details_placeholder_required
                } else {
                    R.string.additional_details_placeholder_optional
                },
            ),
            enabled = enabled,
            singleLine = false,
            minLines = 4,
            maxLines = 8,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
            ),
        )

        Text(
            text = stringResource(R.string.character_count, value.length, 500),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SuccessContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            Icon(
                imageVector = QodeActionIcons.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = stringResource(R.string.report_submitted_success),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: com.qodein.shared.common.error.OperationError,
    onDismiss: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            modifier = Modifier.padding(SpacingTokens.md),
        ) {
            Icon(
                imageVector = UIIcons.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = error.toUiText().asString(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                QodeButton(
                    onClick = onNavigateBack,
                    text = stringResource(R.string.close),
                )
                QodeButton(
                    onClick = onDismiss,
                    text = stringResource(R.string.retry),
                )
            }
        }
    }
}
