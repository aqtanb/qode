package com.qodein.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeDecorationStyle
import com.qodein.core.designsystem.component.QodeGradient
import com.qodein.core.designsystem.icon.QodeCategoryIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.shared.model.Language
import com.qodein.shared.model.Theme

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    TrackScreenViewEvent(screenName = "Settings")

    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        QodeGradient(
            decorations = QodeDecorationStyle.FloatingCircles,
        )

        // Top app bar is now handled centrally by QodeApp
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = SpacingTokens.md)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
            ) {
                // Theme Section
                SettingsSection(
                    title = stringResource(R.string.settings_theme_title),
                    icon = QodeNavigationIcons.Settings,
                ) {
                    ThemeSelector(
                        selectedTheme = state.theme,
                        onThemeSelected = { theme ->
                            viewModel.handleAction(SettingsAction.ThemeChanged(theme))
                        },
                    )
                }

                // Language Section
                SettingsSection(
                    title = stringResource(R.string.settings_language_title),
                    icon = QodeCategoryIcons.Language,
                ) {
                    LanguageSelector(
                        selectedLanguage = state.language,
                        onLanguageSelected = { language ->
                            viewModel.handleAction(SettingsAction.LanguageChanged(language))
                        },
                    )
                }

                Spacer(modifier = Modifier.height(SpacingTokens.lg))
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    QodeCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.xs),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(SpacingTokens.md))

            content()
        }
    }
}

@Composable
private fun ThemeSelector(
    selectedTheme: Theme,
    onThemeSelected: (Theme) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        Theme.entries.forEach { theme ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeSelected(theme) }
                    .padding(vertical = SpacingTokens.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                RadioButton(
                    selected = selectedTheme == theme,
                    onClick = { onThemeSelected(theme) },
                )
                Text(
                    text = stringResource(theme.displayNameResId),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun LanguageSelector(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        Language.entries.forEach { language ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLanguageSelected(language) }
                    .padding(vertical = SpacingTokens.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                RadioButton(
                    selected = selectedLanguage == language,
                    onClick = { onLanguageSelected(language) },
                )
                Text(
                    text = language.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

// Extension property to get display name resource ID for themes
private val Theme.displayNameResId: Int
    get() = when (this) {
        Theme.SYSTEM -> R.string.theme_system
        Theme.LIGHT -> R.string.theme_light
        Theme.DARK -> R.string.theme_dark
    }
