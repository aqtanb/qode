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
import com.qodein.shared.model.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ThemeBottomSheet(
    selectedTheme: Theme,
    onThemeSelected: (Theme) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(
            topStart = ShapeTokens.Corner.extraLarge,
            topEnd = ShapeTokens.Corner.extraLarge,
        ),
    ) {
        ThemeBottomSheetContent(
            selectedTheme = selectedTheme,
            onThemeSelected = onThemeSelected,
        )
    }
}

@Composable
private fun ThemeBottomSheetContent(
    selectedTheme: Theme,
    onThemeSelected: (Theme) -> Unit,
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
            label = stringResource(R.string.theme_system),
            isSelected = selectedTheme == Theme.SYSTEM,
            onClick = { onThemeSelected(Theme.SYSTEM) },
        )

        BottomSheetRadioItem(
            label = stringResource(R.string.theme_light),
            isSelected = selectedTheme == Theme.LIGHT,
            onClick = { onThemeSelected(Theme.LIGHT) },
        )

        BottomSheetRadioItem(
            label = stringResource(R.string.theme_dark),
            isSelected = selectedTheme == Theme.DARK,
            onClick = { onThemeSelected(Theme.DARK) },
        )
    }
}

@ThemePreviews
@Composable
private fun ThemeBottomSheetContentPreview() {
    QodeTheme {
        ThemeBottomSheetContent(
            selectedTheme = Theme.SYSTEM,
            onThemeSelected = {},
        )
    }
}
