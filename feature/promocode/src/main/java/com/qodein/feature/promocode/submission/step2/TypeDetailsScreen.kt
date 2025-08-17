package com.qodein.feature.promocode.submission.step2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
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
    when (wizardData.promoCodeType) {
        PromoCodeType.PERCENTAGE -> PercentageDetailsSection(wizardData, onAction, modifier)
        PromoCodeType.FIXED_AMOUNT -> FixedAmountDetailsSection(wizardData, onAction, modifier)
        null -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(SpacingTokens.xl),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Please select a discount type first",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PercentageDetailsSection(
    wizardData: SubmissionWizardData,
    onAction: (SubmissionWizardAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xl),
    ) {
        QodeTextField(
            value = wizardData.promoCode,
            onValueChange = { onAction(SubmissionWizardAction.UpdatePromoCode(it)) },
            label = "Promo Code",
            placeholder = "SAVE20",
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) },
            ),
            required = true,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            QodeTextField(
                value = wizardData.discountPercentage,
                onValueChange = { onAction(SubmissionWizardAction.UpdateDiscountPercentage(it)) },
                label = "Percentage",
                placeholder = "20",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) },
                ),
                modifier = Modifier.weight(1f),
                required = true,
            )

            QodeTextField(
                value = wizardData.minimumOrderAmount,
                onValueChange = { onAction(SubmissionWizardAction.UpdateMinimumOrderAmount(it)) },
                label = "Min. Order",
                placeholder = "1000",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.weight(1f),
                required = true,
            )
        }

        FirstUserOnlyCheckbox(
            isChecked = wizardData.isFirstUserOnly,
            onCheckedChange = { onAction(SubmissionWizardAction.UpdateFirstUserOnly(it)) },
        )
    }
}

@Composable
private fun FixedAmountDetailsSection(
    wizardData: SubmissionWizardData,
    onAction: (SubmissionWizardAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xl),
    ) {
        QodeTextField(
            value = wizardData.promoCode,
            onValueChange = { onAction(SubmissionWizardAction.UpdatePromoCode(it)) },
            label = "Promo Code",
            placeholder = "GET500",
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) },
            ),
            required = true,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            QodeTextField(
                value = wizardData.discountAmount,
                onValueChange = { onAction(SubmissionWizardAction.UpdateDiscountAmount(it)) },
                label = "Amount",
                placeholder = "500",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) },
                ),
                modifier = Modifier.weight(1f),
                required = true,
            )

            QodeTextField(
                value = wizardData.minimumOrderAmount,
                onValueChange = { onAction(SubmissionWizardAction.UpdateMinimumOrderAmount(it)) },
                label = "Min. Order",
                placeholder = "2000",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.weight(1f),
                required = true,
            )
        }

        FirstUserOnlyCheckbox(
            isChecked = wizardData.isFirstUserOnly,
            onCheckedChange = { onAction(SubmissionWizardAction.UpdateFirstUserOnly(it)) },
        )
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

        Text(
            text = "New customers only",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = SpacingTokens.sm),
        )
    }
}

// MARK: - Enterprise-Level Previews

@Preview(name = "Type Details - Empty State", showBackground = true)
@Composable
private fun TypeDetailsScreenEmptyPreview() {
    QodeTheme {
        TypeDetailsScreen(
            wizardData = SubmissionWizardData(),
            onAction = {},
        )
    }
}

@Preview(name = "Type Details - Percentage Form", showBackground = true)
@Composable
private fun TypeDetailsScreenPercentagePreview() {
    QodeTheme {
        TypeDetailsScreen(
            wizardData = SubmissionWizardData(
                promoCodeType = PromoCodeType.PERCENTAGE,
                promoCode = "SAVE20",
                discountPercentage = "20",
                minimumOrderAmount = "1000",
                isFirstUserOnly = true,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Type Details - Fixed Amount Form", showBackground = true)
@Composable
private fun TypeDetailsScreenFixedAmountPreview() {
    QodeTheme {
        TypeDetailsScreen(
            wizardData = SubmissionWizardData(
                promoCodeType = PromoCodeType.FIXED_AMOUNT,
                promoCode = "GET500",
                discountAmount = "500",
                minimumOrderAmount = "2000",
                isFirstUserOnly = false,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Type Details - Validation Errors", showBackground = true)
@Composable
private fun TypeDetailsScreenValidationPreview() {
    QodeTheme {
        TypeDetailsScreen(
            wizardData = SubmissionWizardData(
                promoCodeType = PromoCodeType.PERCENTAGE,
                promoCode = "",
                discountPercentage = "",
                minimumOrderAmount = "",
            ),
            onAction = {},
        )
    }
}

@Preview(name = "Type Details - Dark Theme", showBackground = true)
@Composable
private fun TypeDetailsScreenDarkPreview() {
    QodeTheme(darkTheme = true) {
        TypeDetailsScreen(
            wizardData = SubmissionWizardData(
                promoCodeType = PromoCodeType.PERCENTAGE,
                promoCode = "DARKMODE20",
                discountPercentage = "25",
                minimumOrderAmount = "1500",
                isFirstUserOnly = true,
            ),
            onAction = {},
        )
    }
}

@Preview(name = "First User Checkbox - Variants", showBackground = true)
@Composable
private fun FirstUserOnlyCheckboxPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            FirstUserOnlyCheckbox(
                isChecked = false,
                onCheckedChange = {},
            )
            FirstUserOnlyCheckbox(
                isChecked = true,
                onCheckedChange = {},
            )
        }
    }
}
