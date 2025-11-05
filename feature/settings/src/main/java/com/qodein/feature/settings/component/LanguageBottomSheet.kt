package com.qodein.feature.settings.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.settings.R
import com.qodein.shared.model.Language

//
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LanguageBottomSheet(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(
            topStart = ShapeTokens.Corner.extraLarge,
            topEnd = ShapeTokens.Corner.extraLarge,
        ),
    ) {
        LanguageBottomSheetContent(
            selectedLanguage = selectedLanguage,
            onLanguageSelected = onLanguageSelected,
        )
    }
}

@Composable
private fun LanguageBottomSheetContent(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.lg)
            .padding(bottom = SpacingTokens.xl)
            .selectableGroup(),
    ) {
        Text(
            text = stringResource(R.string.settings_language_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = SpacingTokens.sm),
        )

        LanguageRadioItem(
            languageName = stringResource(R.string.language_english),
            isSelected = selectedLanguage == Language.ENGLISH,
            onClick = { onLanguageSelected(Language.ENGLISH) },
        )

        LanguageRadioItem(
            languageName = stringResource(R.string.language_russian),
            isSelected = selectedLanguage == Language.RUSSIAN,
            onClick = { onLanguageSelected(Language.RUSSIAN) },
        )

        LanguageRadioItem(
            languageName = stringResource(R.string.language_kazakh),
            isSelected = selectedLanguage == Language.KAZAKH,
            onClick = { onLanguageSelected(Language.KAZAKH) },
        )
    }
}

@Composable
private fun LanguageRadioItem(
    languageName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(vertical = SpacingTokens.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
        )

        Text(
            text = languageName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = SpacingTokens.sm),
        )
    }
}

@ThemePreviews
@Composable
private fun LanguageBottomSheetContentPreview() {
    QodeTheme {
        LanguageBottomSheetContent(
            selectedLanguage = Language.ENGLISH,
            onLanguageSelected = {},
        )
    }
}
