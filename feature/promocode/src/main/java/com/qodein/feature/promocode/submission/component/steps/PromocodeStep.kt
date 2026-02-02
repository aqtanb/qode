package com.qodein.feature.promocode.submission.component.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.component.QodeinTextField
import com.qodein.core.designsystem.icon.PromocodeIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.ServicePreviewData
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.PromocodeSubmissionScreen
import com.qodein.feature.promocode.submission.PromocodeSubmissionUiState
import com.qodein.feature.promocode.submission.PromocodeType
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.wizard.PromocodeWizardStep
import com.qodein.shared.model.Promocode

@Composable
internal fun PromocodeStep(
    promocode: String,
    promocodeType: PromocodeType?,
    onPromocodeChange: (String) -> Unit,
    onPromocodeTypeChange: (PromocodeType) -> Unit,
    focusRequester: FocusRequester,
    onNextStep: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    Column {
        Text(
            text = stringResource(R.string.promocode_code_choose_label),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = SpacingTokens.md, start = SpacingTokens.xs),
        )

        QodeinTextField(
            value = promocode,
            onValueChange = { onPromocodeChange(it) },
            placeholder = stringResource(R.string.promo_code_step_placeholder),
            leadingIcon = PromocodeIcons.Promocode,
            helperText = stringResource(R.string.promo_code_step_helper_text),
            focusRequester = focusRequester,
            showPasteIcon = true,
            maxLength = Promocode.CODE_MAX_LENGTH,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Characters,
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onNextStep()
                },
            ),
            modifier = Modifier.padding(bottom = SpacingTokens.xl),
        )

        Text(
            text = stringResource(R.string.promocode_type_choose_label),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = SpacingTokens.md, start = SpacingTokens.xs),
        )

        PromocodeDiscountType(
            promocodeType = promocodeType,
            onPromocodeTypeChange = onPromocodeTypeChange,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PromocodeDiscountType(
    promocodeType: PromocodeType?,
    onPromocodeTypeChange: (PromocodeType) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = PromocodeType.entries.toList()
    Row(
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween, Alignment.CenterHorizontally),
        modifier = modifier.fillMaxWidth(),
    ) {
        options.forEachIndexed { index, type ->
            val animatedWeight by animateFloatAsState(
                targetValue = if (type == promocodeType) 2f else 1f,
                animationSpec = tween(durationMillis = 700),
                label = "buttonWeight",
            )

            ToggleButton(
                checked = type == promocodeType,
                onCheckedChange = { onPromocodeTypeChange(type) },
                modifier = Modifier
                    .weight(animatedWeight)
                    .semantics { role = Role.RadioButton },
                shapes =
                when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                colors = ToggleButtonDefaults.tonalToggleButtonColors(),
            ) {
                Icon(
                    imageVector = type.icon,
                    contentDescription = null,
                    modifier = Modifier.size(ToggleButtonDefaults.IconSize),
                )
                AnimatedVisibility(
                    visible = type == promocodeType,
                    enter = fadeIn(
                        animationSpec = tween(durationMillis = 700),
                    ) + expandHorizontally(
                        animationSpec = tween(durationMillis = 700),
                    ),
                    exit = fadeOut(
                        animationSpec = tween(durationMillis = 700),
                    ) + shrinkHorizontally(
                        animationSpec = tween(durationMillis = 700),
                    ),
                ) {
                    Row {
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text(
                            text = stringResource(type.titleRes),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

val PromocodeType.titleRes: Int
    get() = when (this) {
        PromocodeType.PERCENTAGE -> R.string.promocode_type_percentage
        PromocodeType.FIXED_AMOUNT -> R.string.promocode_type_amount
        PromocodeType.FREE_ITEM -> R.string.promocode_type_gift
    }

val PromocodeType.icon: ImageVector
    get() = when (this) {
        PromocodeType.PERCENTAGE -> PromocodeIcons.Percentage
        PromocodeType.FIXED_AMOUNT -> PromocodeIcons.FixedAmount
        PromocodeType.FREE_ITEM -> PromocodeIcons.FreeItem
    }

@PreviewLightDark
@Composable
private fun PromocodeStepPreview() {
    QodeTheme {
        PromocodeSubmissionScreen(
            uiState = PromocodeSubmissionUiState(
                currentStep = PromocodeWizardStep.PROMOCODE,
                wizardData = SubmissionWizardData(
                    selectedService = ServicePreviewData.netflix,
                    promocodeType = PromocodeType.PERCENTAGE,
                ),
            ),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateBack = {},
        )
    }
}
