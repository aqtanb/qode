package com.qodein.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.component.QodeinElevatedCard
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCategoryIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeinIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

@Composable
fun SettingsRoute(
    onBackClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    TrackScreenViewEvent(screenName = "Settings")

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        uiState = uiState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    onAction: (SettingsAction) -> Unit,
    onBackClick: () -> Unit,
    uiState: SettingsUiState,
) {
    Scaffold(
        topBar = {
            QodeTopAppBar(
                title = stringResource(R.string.settings_title),
                navigationIcon = QodeActionIcons.Back,
                onNavigationClick = onBackClick,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsItem(
                title = stringResource(R.string.settings_language_title),
                leadingIcon = QodeCategoryIcons.Language,
                trailingIcon = QodeActionIcons.Next,
                onClick = { },
            )

            SettingsItem(
                title = stringResource(R.string.settings_theme_title),
                leadingIcon = QodeinIcons.DarkMode,
                trailingIcon = QodeActionIcons.Next,
                onClick = {},
            )

            SettingsItem(
                title = stringResource(R.string.settings_notifications_title),
                leadingIcon = QodeNavigationIcons.Notifications,
                trailingIcon = QodeActionIcons.Next,
                onClick = {},
            )

            HorizontalDivider()

            SettingsItem(
                title = stringResource(R.string.settings_source_code_title),
                leadingIcon = QodeCategoryIcons.Tech,
                trailingIcon = QodeActionIcons.Next,
                onClick = {},
            )

            SettingsItem(
                title = stringResource(R.string.settings_open_source_licences_title),
                leadingIcon = QodeCategoryIcons.Certification,
                trailingIcon = QodeActionIcons.Next,
                onClick = {},
            )

            HorizontalDivider()

            SettingsItem(
                title = stringResource(R.string.settings_feedback_title),
                leadingIcon = QodeNavigationIcons.Feedback,
                trailingIcon = QodeActionIcons.Next,
                onClick = {},
            )

            SettingsItem(
                title = stringResource(R.string.settings_about_title),
                leadingIcon = QodeNavigationIcons.Info,
                trailingIcon = QodeActionIcons.Next,
                onClick = {},
            )
        }
    }
}

@Composable
private fun SettingsItem(
    onClick: () -> Unit,
    title: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null
) {
    QodeinElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ShapeTokens.Corner.small),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )

            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                )
            }
        }
    }
}

// MARK: Previews

@ThemePreviews
@Composable
private fun SettingsContentPreview() {
    QodeTheme {
        SettingsScreen(
            onAction = {},
            onBackClick = {},
            uiState = SettingsUiState()
        )
    }
}
