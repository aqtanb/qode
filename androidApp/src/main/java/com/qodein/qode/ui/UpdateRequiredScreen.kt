package com.qodein.qode.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeLogo
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.qode.R

@Composable
fun UpdateRequiredScreen(
    onUpdateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SpacingTokens.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            QodeLogo(
                modifier = Modifier.padding(bottom = SpacingTokens.md),
            )

            Text(
                text = stringResource(R.string.update_required_title),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.md))

            Text(
                text = stringResource(R.string.update_required_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.xl))

            QodeButton(
                onClick = onUpdateClick,
                text = stringResource(R.string.update_required_button),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun UpdateRequiredScreenPreview() {
    QodeTheme {
        UpdateRequiredScreen(
            onUpdateClick = {},
        )
    }
}
