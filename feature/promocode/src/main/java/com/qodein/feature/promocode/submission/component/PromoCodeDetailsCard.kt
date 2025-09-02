package com.qodein.feature.promocode.submission.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.component.QodeTextField
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.PromoCodeType
import com.qodein.feature.promocode.submission.SubmissionWizardAction
import com.qodein.feature.promocode.submission.SubmissionWizardData

@Composable
fun PromoCodeDetailsCard(
    wizardData: SubmissionWizardData,
    onAction: (SubmissionWizardAction) -> Unit,
    focusManager: FocusManager,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        // Clean layout without excessive cards - subtle background
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.3f),
                    RoundedCornerShape(ShapeTokens.Corner.large),
                )
                .padding(SpacingTokens.lg),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            // Promo Code Input with circular design
            QodeTextField(
                value = wizardData.promoCode,
                onValueChange = { onAction(SubmissionWizardAction.UpdatePromoCode(it)) },
                label = "Promo Code",
                placeholder = "SAVE20",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) },
                ),
                required = true,
            )

            // Perfectly aligned input row - same height fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
            ) {
                // Dynamic discount field based on type - same proportions
                when (wizardData.promoCodeType) {
                    PromoCodeType.PERCENTAGE -> {
                        QodeTextField(
                            value = wizardData.discountPercentage,
                            onValueChange = { onAction(SubmissionWizardAction.UpdateDiscountPercentage(it)) },
                            label = "Percentage",
                            placeholder = "20",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Next) },
                            ),
                            required = true,
                        )
                    }
                    PromoCodeType.FIXED_AMOUNT -> {
                        QodeTextField(
                            value = wizardData.discountAmount,
                            onValueChange = { onAction(SubmissionWizardAction.UpdateDiscountAmount(it)) },
                            label = "Amount",
                            placeholder = "100",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Next) },
                            ),
                            required = true,
                        )
                    }
                    else -> Unit
                }

                // Minimum order field - same height as discount field
                QodeTextField(
                    value = wizardData.minimumOrderAmount,
                    onValueChange = { onAction(SubmissionWizardAction.UpdateMinimumOrderAmount(it)) },
                    label = "Min Order",
                    placeholder = "500",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    required = true,
                )
            }
        }
    }
}

@Preview(name = "Promo Code Details - Percentage", showBackground = true)
@Composable
private fun PromoCodeDetailsCardPercentagePreview() {
    QodeTheme {
        PromoCodeDetailsCard(
            wizardData = SubmissionWizardData(
                promoCode = "SAVE20",
                promoCodeType = PromoCodeType.PERCENTAGE,
                discountPercentage = "20",
                minimumOrderAmount = "500",
            ),
            onAction = {},
            focusManager = LocalFocusManager.current,
            modifier = Modifier.padding(SpacingTokens.lg),
        )
    }
}

@Preview(name = "Promo Code Details - Fixed Amount", showBackground = true)
@Composable
private fun PromoCodeDetailsCardFixedAmountPreview() {
    QodeTheme {
        PromoCodeDetailsCard(
            wizardData = SubmissionWizardData(
                promoCode = "FOOD100",
                promoCodeType = PromoCodeType.FIXED_AMOUNT,
                discountAmount = "100",
                minimumOrderAmount = "300",
            ),
            onAction = {},
            focusManager = LocalFocusManager.current,
            modifier = Modifier.padding(SpacingTokens.lg),
        )
    }
}
