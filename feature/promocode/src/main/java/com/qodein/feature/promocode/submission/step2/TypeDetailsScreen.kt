package com.qodein.feature.promocode.submission.step2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.component.QodeTextField
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.PromoCodeType
import com.qodein.feature.promocode.submission.SubmissionWizardAction
import com.qodein.feature.promocode.submission.SubmissionWizardData

@Composable
fun TypeDetailsScreen(
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
            text = "Promo Code Details",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = when (wizardData.promoCodeType) {
                PromoCodeType.PERCENTAGE -> "Enter the promo code and percentage discount details"
                PromoCodeType.FIXED_AMOUNT -> "Enter the promo code and fixed amount discount details"
                null -> "Please select a promo code type first"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        when (wizardData.promoCodeType) {
            PromoCodeType.PERCENTAGE -> PercentageDetailsSection(wizardData, onAction)
            PromoCodeType.FIXED_AMOUNT -> FixedAmountDetailsSection(wizardData, onAction)
            null -> {
                QodeCard(variant = QodeCardVariant.Outlined) {
                    Text(
                        text = "Please go back and select a promo code type first",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun PercentageDetailsSection(
    wizardData: SubmissionWizardData,
    onAction: (SubmissionWizardAction) -> Unit
) {
    QodeCard(variant = QodeCardVariant.Outlined) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            Text(
                text = "Percentage Discount Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            QodeTextField(
                value = wizardData.promoCode,
                onValueChange = { onAction(SubmissionWizardAction.UpdatePromoCode(it)) },
                label = "Promo Code",
                placeholder = "e.g., SAVE20, DISCOUNT50",
                required = true,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                QodeTextField(
                    value = wizardData.discountPercentage,
                    onValueChange = { onAction(SubmissionWizardAction.UpdateDiscountPercentage(it)) },
                    label = "Discount Percentage",
                    placeholder = "e.g., 20",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    required = true,
                )
            }

            QodeTextField(
                value = wizardData.minimumOrderAmount,
                onValueChange = { onAction(SubmissionWizardAction.UpdateMinimumOrderAmount(it)) },
                label = "Minimum Order Amount",
                placeholder = "e.g., 1000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                required = true,
            )

            FirstUserOnlyCheckbox(
                isChecked = wizardData.isFirstUserOnly,
                onCheckedChange = { onAction(SubmissionWizardAction.UpdateFirstUserOnly(it)) },
            )
        }
    }
}

@Composable
private fun FixedAmountDetailsSection(
    wizardData: SubmissionWizardData,
    onAction: (SubmissionWizardAction) -> Unit
) {
    QodeCard(variant = QodeCardVariant.Outlined) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            Text(
                text = "Fixed Amount Discount Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            QodeTextField(
                value = wizardData.promoCode,
                onValueChange = { onAction(SubmissionWizardAction.UpdatePromoCode(it)) },
                label = "Promo Code",
                placeholder = "e.g., GET500, FLAT1000",
                required = true,
            )

            QodeTextField(
                value = wizardData.discountAmount,
                onValueChange = { onAction(SubmissionWizardAction.UpdateDiscountAmount(it)) },
                label = "Discount Amount",
                placeholder = "e.g., 500",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                required = true,
            )

            QodeTextField(
                value = wizardData.minimumOrderAmount,
                onValueChange = { onAction(SubmissionWizardAction.UpdateMinimumOrderAmount(it)) },
                label = "Minimum Order Amount",
                placeholder = "e.g., 2000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                required = true,
            )

            FirstUserOnlyCheckbox(
                isChecked = wizardData.isFirstUserOnly,
                onCheckedChange = { onAction(SubmissionWizardAction.UpdateFirstUserOnly(it)) },
            )
        }
    }
}

@Composable
private fun FirstUserOnlyCheckbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
        )

        Column(
            modifier = Modifier.padding(start = SpacingTokens.sm),
        ) {
            Text(
                text = "First time users only",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "This promo code can only be used by new customers",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TypeDetailsScreenPercentagePreview() {
    QodeTheme {
        TypeDetailsScreen(
            wizardData = SubmissionWizardData(
                promoCodeType = PromoCodeType.PERCENTAGE,
                promoCode = "SAVE20",
                discountPercentage = "20",
                minimumOrderAmount = "1000",
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TypeDetailsScreenFixedAmountPreview() {
    QodeTheme {
        TypeDetailsScreen(
            wizardData = SubmissionWizardData(
                promoCodeType = PromoCodeType.FIXED_AMOUNT,
                promoCode = "GET500",
                discountAmount = "500",
                minimumOrderAmount = "2000",
            ),
            onAction = {},
        )
    }
}
