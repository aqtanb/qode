package com.qodein.feature.promocode.submission.step4

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.component.QodeTextField
import com.qodein.core.designsystem.component.QodeTextFieldVariant
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.SubmissionWizardAction
import com.qodein.feature.promocode.submission.SubmissionWizardData

@Composable
fun OptionalDetailsScreen(
    wizardData: SubmissionWizardData,
    onAction: (SubmissionWizardAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(SpacingTokens.lg),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        Text(
            text = "Optional Details",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Add a title, description, and screenshot to make your promo code more appealing",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        QodeCard(variant = QodeCardVariant.Outlined) {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
            ) {
                Text(
                    text = "Additional Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                // Title Input
                QodeTextField(
                    value = wizardData.title,
                    onValueChange = { onAction(SubmissionWizardAction.UpdateTitle(it)) },
                    label = "Title",
                    placeholder = "e.g., 20% off all electronics",
                    required = true,
                    helperText = "If left empty, we'll generate a title based on your promo code details",
                )

                // Description Input
                QodeTextField(
                    value = wizardData.description,
                    onValueChange = { onAction(SubmissionWizardAction.UpdateDescription(it)) },
                    label = "Description",
                    placeholder = "Describe the offer, terms, and conditions...",
                    variant = QodeTextFieldVariant.Multiline,
                    helperText = "Help others understand what this promo code offers",
                )

                // Screenshot Section
                ScreenshotSection(
                    screenshotUrl = wizardData.screenshotUrl,
                    onScreenshotUpdate = { onAction(SubmissionWizardAction.UpdateScreenshotUrl(it)) },
                )
            }
        }

        // Auto-generation info
        if (wizardData.title.isBlank()) {
            QodeCard(variant = QodeCardVariant.Filled) {
                Column {
                    Text(
                        text = "💡 Auto-generated Title",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = generateAutoTitle(wizardData),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
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
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        Text(
            text = "Screenshot (Optional)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )

        if (screenshotUrl != null) {
            // Show uploaded screenshot placeholder
            ScreenshotPreview(
                screenshotUrl = screenshotUrl,
                onRemove = { onScreenshotUpdate(null) },
            )
        } else {
            // Show upload options
            ScreenshotUploadOptions(
                onUpload = { url -> onScreenshotUpdate(url) },
            )
        }

        Text(
            text = "Screenshots help users understand the offer better and increase trust",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        OutlinedCard(
            modifier = Modifier.weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingTokens.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = "Take Photo",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )

                QodeButton(
                    onClick = {
                        // TODO: Implement camera functionality
                        onUpload("camera://screenshot_${System.currentTimeMillis()}")
                    },
                    text = "Camera",
                    variant = QodeButtonVariant.Outlined,
                )
            }
        }

        OutlinedCard(
            modifier = Modifier.weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingTokens.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = "Choose Image",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )

                QodeButton(
                    onClick = {
                        // TODO: Implement gallery picker functionality
                        onUpload("gallery://screenshot_${System.currentTimeMillis()}")
                    },
                    text = "Gallery",
                    variant = QodeButtonVariant.Outlined,
                )
            }
        }
    }
}

private fun generateAutoTitle(wizardData: SubmissionWizardData): String =
    when (wizardData.promoCodeType) {
        com.qodein.feature.promocode.submission.PromoCodeType.PERCENTAGE -> {
            val percentage = wizardData.discountPercentage.toIntOrNull() ?: 0
            "$percentage% off at ${wizardData.serviceName}"
        }
        com.qodein.feature.promocode.submission.PromoCodeType.FIXED_AMOUNT -> {
            val amount = wizardData.discountAmount.toIntOrNull() ?: 0
            "₸$amount off at ${wizardData.serviceName}"
        }
        null -> "Promo code for ${wizardData.serviceName}"
    }

@Preview(showBackground = true)
@Composable
private fun OptionalDetailsScreenPreview() {
    QodeTheme {
        OptionalDetailsScreen(
            wizardData = SubmissionWizardData(
                serviceName = "Netflix",
                title = "20% off Premium subscription",
                description = "Get 20% off your first 3 months of Netflix Premium. Valid for new users only.",
                screenshotUrl = null,
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OptionalDetailsScreenWithScreenshotPreview() {
    QodeTheme {
        OptionalDetailsScreen(
            wizardData = SubmissionWizardData(
                serviceName = "Netflix",
                title = "",
                description = "",
                screenshotUrl = "https://example.com/screenshot.png",
            ),
            onAction = {},
        )
    }
}
