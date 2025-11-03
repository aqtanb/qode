package com.qodein.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.component.ButtonSize
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeinElevatedCard
import com.qodein.core.designsystem.component.QodeinIconButton
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.R
import com.qodein.core.ui.error.asUiText
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError

/**
 * Error card component for consistent error handling across the app.
 */
@Composable
fun QodeErrorCard(
    error: OperationError,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.error_default_title),
    onDismiss: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null
) {
    QodeinElevatedCard(
        shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(SpacingTokens.xl)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = error.asUiText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                if (onRetry != null) {
                    QodeButton(
                        text = stringResource(R.string.action_retry),
                        onClick = onRetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = SpacingTokens.sm),
                    )
                }
            }

            if (onDismiss != null) {
                QodeinIconButton(
                    onClick = onDismiss,
                    icon = QodeActionIcons.Close,
                    contentDescription = stringResource(R.string.cd_close),
                    size = ButtonSize.Small,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(SpacingTokens.sm),
                )
            }
        }
    }
}

// MARK: - Previews

@Preview(showBackground = true)
@Composable
private fun QodeErrorCardPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            QodeErrorCard(
                error = SystemError.Offline,
                onRetry = {},
                onDismiss = {},
            )
        }
    }
}
