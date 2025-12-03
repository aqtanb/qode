package com.qodein.feature.auth.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.ShimmerLine
import com.qodein.core.designsystem.icon.UIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.error.asUiText
import com.qodein.feature.auth.LegalDocumentUiState
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.model.DocumentType
import com.qodein.shared.model.LegalDocument
import com.qodein.core.ui.R as CoreUiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LegalDocumentBottomSheet(
    onDismiss: () -> Unit,
    onRetry: (DocumentType) -> Unit,
    state: LegalDocumentUiState
) {
    if (state !is LegalDocumentUiState.Closed) {
        ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.background) {
            when (state) {
                is LegalDocumentUiState.Loading -> {
                    LegalDocumentSkeleton()
                }

                is LegalDocumentUiState.Content -> {
                    LegalDocumentContent(document = state.document)
                }

                is LegalDocumentUiState.Error -> {
                    LegalDocumentError(
                        error = state.errorType,
                        onRetry = { onRetry(state.documentType) },
                    )
                }

                LegalDocumentUiState.Closed -> { /* Won't happen due to if check */ }
            }
        }
    }
}

// MARK: - Components

@Composable
private fun LegalDocumentSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingTokens.lg)
            .padding(bottom = SpacingTokens.xl),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        // Title
        ShimmerLine(width = 200.dp, height = 28.dp)

        Spacer(modifier = Modifier.height(SpacingTokens.xs))

        // Subtitle/Heading
        ShimmerLine(width = 180.dp, height = 20.dp)

        Spacer(modifier = Modifier.height(SpacingTokens.xs))

        // Paragraph lines
        ShimmerLine(width = 300.dp, height = 14.dp)
        ShimmerLine(width = 280.dp, height = 14.dp)
        ShimmerLine(width = 290.dp, height = 14.dp)

        Spacer(modifier = Modifier.height(SpacingTokens.sm))

        // Another heading
        ShimmerLine(width = 160.dp, height = 20.dp)

        Spacer(modifier = Modifier.height(SpacingTokens.xs))

        // More paragraph lines
        ShimmerLine(width = 310.dp, height = 14.dp)
        ShimmerLine(width = 270.dp, height = 14.dp)
        ShimmerLine(width = 300.dp, height = 14.dp)
        ShimmerLine(width = 220.dp, height = 14.dp)

        Spacer(modifier = Modifier.height(SpacingTokens.sm))

        // Another section
        ShimmerLine(width = 170.dp, height = 20.dp)

        Spacer(modifier = Modifier.height(SpacingTokens.xs))

        ShimmerLine(width = 290.dp, height = 14.dp)
        ShimmerLine(width = 260.dp, height = 14.dp)
    }
}

@Composable
private fun LegalDocumentContent(
    document: LegalDocument,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(SpacingTokens.lg)
            .padding(bottom = SpacingTokens.xl),
    ) {
        Markdown(
            content = document.content,
        )
    }
}

@Composable
private fun LegalDocumentError(
    error: OperationError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.xl)
            .padding(bottom = SpacingTokens.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        Icon(
            imageVector = UIIcons.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
        )

        Text(
            text = error.asUiText(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        QodeButton(
            text = stringResource(CoreUiR.string.action_retry),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// MARK: - Previews

@ThemePreviews
@Composable
private fun LegalDocumentSkeletonPreview() {
    QodeTheme {
        LegalDocumentSkeleton()
    }
}

@ThemePreviews
@Composable
private fun LegalDocumentContentPreview() {
    QodeTheme {
        LegalDocumentContent(
            document = LegalDocument(
                type = DocumentType.TermsOfService,
                content = """
                    # Terms of Service

                    ## 1. Acceptance of Terms

                    By accessing and using this service, you accept and agree to be bound by the terms and provision of this agreement.

                    ## 2. Use License

                    Permission is granted to temporarily download one copy of the materials for personal, non-commercial transitory viewing only.

                    ## 3. Disclaimer

                    The materials are provided on an 'as is' basis. We make no warranties, expressed or implied.
                """.trimIndent(),
            ),
        )
    }
}

@ThemePreviews
@Composable
private fun LegalDocumentErrorPreview() {
    QodeTheme {
        LegalDocumentError(
            error = SystemError.Offline,
            onRetry = {},
        )
    }
}
