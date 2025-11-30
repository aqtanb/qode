package com.qodein.feature.promocode.submission.component.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.submission.PromoCodeType
import com.qodein.feature.promocode.submission.PromocodeSubmissionStep
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.component.PromocodeSubmissionCard
import com.qodein.feature.promocode.submission.component.SubmissionFieldType
import com.qodein.feature.promocode.submission.component.SubmissionTextField

@Composable
internal fun DiscountValueStep(
    promoCodeType: PromoCodeType?,
    discountPercentage: String,
    discountAmount: String,
    onDiscountPercentageChange: (String) -> Unit,
    onDiscountAmountChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onNextStep: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        // Discount value field (changes based on type)
        when (promoCodeType) {
            PromoCodeType.PERCENTAGE -> {
                SubmissionTextField(
                    value = discountPercentage,
                    onValueChange = onDiscountPercentageChange,
                    label = "Discount Percentage",
                    placeholder = "20",
                    fieldType = SubmissionFieldType.PERCENTAGE,
                    leadingIcon = QodeIcons.Sale,
                    helperText = "Enter percentage (1-99%)",
                    isRequired = true,
                    focusRequester = focusRequester,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Companion.Next,
                        keyboardType = KeyboardType.Companion.Number,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { onNextStep() },
                    ),
                    supportingContent = {
                        if (discountPercentage.isNotEmpty()) {
                            val percentage = discountPercentage.toIntOrNull() ?: 0
                            if (percentage > 0) {
                                Text(
                                    text = "Customer saves $percentage% on their order",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.Companion.fillMaxWidth(),
                                    textAlign = TextAlign.Companion.Center,
                                )
                            }
                        }
                    },
                )
            }

            PromoCodeType.FIXED_AMOUNT -> {
                SubmissionTextField(
                    value = discountAmount,
                    onValueChange = onDiscountAmountChange,
                    label = "Discount Amount",
                    placeholder = "500",
                    fieldType = SubmissionFieldType.CURRENCY,
                    leadingIcon = QodeIcons.Dollar,
                    helperText = "Enter amount in ₸ (tenge)",
                    isRequired = true,
                    focusRequester = focusRequester,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Companion.Next,
                        keyboardType = KeyboardType.Companion.Number,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { onNextStep() },
                    ),
                    supportingContent = {
                        if (discountAmount.isNotEmpty()) {
                            val amount = discountAmount.toIntOrNull() ?: 0
                            if (amount > 0) {
                                Text(
                                    text = "Customer saves ₸$amount on their order",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    },
                )
            }

            null -> {
                SubmissionTextField(
                    value = "",
                    onValueChange = { },
                    label = "Discount Value",
                    placeholder = "Select discount type first",
                    enabled = false,
                    helperText = "Choose a discount type in the previous step",
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun PromocodeStepPreview() {
    QodeTheme {
        PromocodeSubmissionCard(
            currentStep = PromocodeSubmissionStep.DISCOUNT_VALUE,
            wizardData = SubmissionWizardData(),
            onAction = {},
        )
    }
}
