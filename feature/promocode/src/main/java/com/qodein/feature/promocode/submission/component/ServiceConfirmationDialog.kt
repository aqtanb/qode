package com.qodein.feature.promocode.submission.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.component.QodeAlertDialog
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.R
import com.qodein.core.ui.R as CoreUiR

@Composable
fun ServiceConfirmationDialog(
    serviceName: String,
    serviceUrl: String,
    logoUrl: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeAlertDialog(
        onDismissRequest = onDismiss,
        confirmButtonText = stringResource(R.string.action_continue),
        onConfirmClick = onConfirm,
        dismissButtonText = stringResource(CoreUiR.string.cancel),
        onDismissClick = onDismiss,
        modifier = modifier,
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                modifier = Modifier.fillMaxWidth(),
            ) {
                CircularImage(
                    imageUrl = logoUrl,
                    fallbackIcon = QodeIcons.Service,
                    size = SizeTokens.Avatar.sizeLarge,
                )

                Text(
                    text = serviceName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = stringResource(R.string.service_confirmation_question),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    )
}

@ThemePreviews
@Composable
private fun ServiceConfirmationDialogPreview() {
    QodeTheme {
        ServiceConfirmationDialog(
            serviceName = "Netflix",
            serviceUrl = "netflix.com",
            logoUrl = "https://img.logo.dev/netflix.com",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
