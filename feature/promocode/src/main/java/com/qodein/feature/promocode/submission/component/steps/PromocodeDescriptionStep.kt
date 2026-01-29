package com.qodein.feature.promocode.submission.component.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.component.QodeinBasicTextField
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.ContentImage
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.submission.SubmissionWizardData
import com.qodein.feature.promocode.submission.component.PromocodeSubmissionCard
import com.qodein.feature.promocode.submission.wizard.PromocodeWizardStep

@Composable
internal fun PromocodeDescriptionStep(
    description: String,
    imageUris: List<String>,
    onDescriptionChange: (String) -> Unit,
    onRemoveImage: (Int) -> Unit,
    onNextStep: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        QodeinBasicTextField(
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = stringResource(R.string.promocode_description_placeholder),
            singleLine = false,
            minLines = 3,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Sentences,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onNextStep() },
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.md),
        )

        if (imageUris.isNotEmpty()) {
            HorizontalPager(
                state = rememberPagerState { imageUris.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SpacingTokens.xs),
            ) { page ->
                ContentImage(
                    uri = imageUris[page],
                    currentPage = page + 1,
                    totalPages = imageUris.size,
                    onRemove = { onRemoveImage(page) },
                    ratio = 1f,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun PromocodeRulesStep() {
    QodeTheme {
        PromocodeSubmissionCard(
            currentStep = PromocodeWizardStep.DESCRIPTION,
            wizardData = SubmissionWizardData(),
            onAction = {},
        )
    }
}
