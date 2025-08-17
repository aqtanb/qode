package com.qodein.feature.promocode.submission.step4

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeTextField
import com.qodein.core.designsystem.component.QodeTextFieldVariant
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.PromoCodeType
import com.qodein.feature.promocode.submission.SubmissionWizardAction
import com.qodein.feature.promocode.submission.SubmissionWizardData

@Composable
fun OptionalDetailsScreen(
    wizardData: SubmissionWizardData,
    onAction: (SubmissionWizardAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxl),
    ) {
        // Title Input - now pre-filled by ViewModel
        QodeTextField(
            value = wizardData.title,
            onValueChange = { onAction(SubmissionWizardAction.UpdateTitle(it)) },
            label = "Title",
            placeholder = "Enter promo code title...",
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) },
            ),
        )

        // Description Input
        QodeTextField(
            value = wizardData.description,
            onValueChange = { onAction(SubmissionWizardAction.UpdateDescription(it)) },
            label = "Description (Optional)",
            placeholder = "Terms and conditions...",
            variant = QodeTextFieldVariant.Multiline,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        )

        // Screenshot Section
        ScreenshotSection(
            screenshotUrl = wizardData.screenshotUrl,
            onScreenshotUpdate = { onAction(SubmissionWizardAction.UpdateScreenshotUrl(it)) },
        )
    }
}

@Composable
private fun ScreenshotSection(
    screenshotUrl: String?,
    onScreenshotUpdate: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        Text(
            text = "Screenshot (Optional)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (screenshotUrl != null) {
            ScreenshotPreview(
                screenshotUrl = screenshotUrl,
                onRemove = { onScreenshotUpdate(null) },
            )
        } else {
            ScreenshotUploadOptions(
                onUpload = { url -> onScreenshotUpdate(url) },
            )
        }
    }
}

@Composable
private fun ScreenshotPreview(
    screenshotUrl: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = "Screenshot uploaded",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )

            Text(
                text = screenshotUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            QodeButton(
                onClick = onRemove,
                text = "Remove",
                variant = QodeButtonVariant.Outlined,
            )
        }
    }
}

@Composable
private fun ScreenshotUploadOptions(
    onUpload: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    QodeButton(
        onClick = {
            // TODO: Show bottom sheet with Camera/Gallery options
            // For now, default to gallery
            onUpload("gallery://screenshot_${System.currentTimeMillis()}")
        },
        text = "Add Screenshot",
        variant = QodeButtonVariant.Outlined,
        modifier = modifier.fillMaxWidth(),
    )
}

// MARK: - Enterprise-Level Previews

@Preview(name = "Optional Details - Empty State", showBackground = true)
@Composable
private fun OptionalDetailsScreenEmptyPreview() {
    QodeTheme {
        OptionalDetailsScreen(
            wizardData = SubmissionWizardData(
                serviceName = "Netflix",
                promoCodeType = PromoCodeType.PERCENTAGE,
                discountPercentage = "20",
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Optional Details - Filled Form", showBackground = true)
@Composable
private fun OptionalDetailsScreenPreview() {
    QodeTheme {
        OptionalDetailsScreen(
            wizardData = SubmissionWizardData(
                serviceName = "Netflix",
                title = "20% off Premium subscription",
                description = "Get 20% off your first 3 months of Netflix Premium. Valid for new users only.",
                screenshotUrl = null,
                promoCodeType = PromoCodeType.PERCENTAGE,
                discountPercentage = "20",
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Optional Details - With Screenshot", showBackground = true)
@Composable
private fun OptionalDetailsScreenWithScreenshotPreview() {
    QodeTheme {
        OptionalDetailsScreen(
            wizardData = SubmissionWizardData(
                serviceName = "Amazon",
                title = "Free shipping weekend",
                description = "Get free shipping on all orders this weekend only.",
                screenshotUrl = "https://example.com/screenshot.png",
                promoCodeType = PromoCodeType.FIXED_AMOUNT,
                discountAmount = "500",
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Optional Details - Auto-Preview", showBackground = true)
@Composable
private fun OptionalDetailsScreenAutoPreviewDemo() {
    QodeTheme {
        OptionalDetailsScreen(
            wizardData = SubmissionWizardData(
                serviceName = "Spotify",
                title = "",
                description = "3 months free premium",
                promoCodeType = PromoCodeType.PERCENTAGE,
                discountPercentage = "50",
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Optional Details - Dark Theme", showBackground = true)
@Composable
private fun OptionalDetailsScreenDarkPreview() {
    QodeTheme(darkTheme = true) {
        OptionalDetailsScreen(
            wizardData = SubmissionWizardData(
                serviceName = "YouTube Premium",
                title = "Student Discount",
                description = "Special offer for students with valid ID",
                screenshotUrl = null,
                promoCodeType = PromoCodeType.PERCENTAGE,
                discountPercentage = "30",
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Screenshot Upload Options", showBackground = true)
@Composable
private fun ScreenshotUploadOptionsPreview() {
    QodeTheme {
        ScreenshotUploadOptions(
            onUpload = {},
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}

@Preview(name = "Screenshot Preview", showBackground = true)
@Composable
private fun ScreenshotPreviewDemo() {
    QodeTheme {
        ScreenshotPreview(
            screenshotUrl = "https://example.com/screenshot.png",
            onRemove = {},
            modifier = Modifier.padding(SpacingTokens.md),
        )
    }
}
