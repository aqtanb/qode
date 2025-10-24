package com.qodein.feature.post.submission.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.ButtonSize
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.component.QodeTopAppBarVariant
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.post.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PostCreationTopBar(
    canSubmit: Boolean,
    onNavigateBack: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeTopAppBar(
        title = "",
        navigationIcon = QodeActionIcons.Back,
        onNavigationClick = onNavigateBack,
        variant = QodeTopAppBarVariant.CenterAligned,
        navigationIconTint = MaterialTheme.colorScheme.onSurface,
        backgroundColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
        customActions = {
            QodeButton(
                onClick = onSubmit,
                text = stringResource(R.string.post),
                enabled = canSubmit,
                size = ButtonSize.Small,
                modifier = Modifier.padding(end = SpacingTokens.xs),
            )
        },
    )
}

@ThemePreviews
@Composable
private fun PostCreationTopBarPreview() {
    QodeTheme {
        PostCreationTopBar(
            canSubmit = true,
            onNavigateBack = {},
            onSubmit = {},
        )
    }
}
