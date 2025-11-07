package com.qodein.feature.settings.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.settings.R
import com.qodein.shared.model.Language

// For Android 13 > phones
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
        BottomSheetRadioItem(
            label = stringResource(R.string.language_english),
            isSelected = selectedLanguage == Language.ENGLISH,
            onClick = { onLanguageSelected(Language.ENGLISH) },
        )

        BottomSheetRadioItem(
            label = stringResource(R.string.language_russian),
            isSelected = selectedLanguage == Language.RUSSIAN,
            onClick = { onLanguageSelected(Language.RUSSIAN) },
        )

        BottomSheetRadioItem(
            label = stringResource(R.string.language_kazakh),
            isSelected = selectedLanguage == Language.KAZAKH,
            onClick = { onLanguageSelected(Language.KAZAKH) },
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
