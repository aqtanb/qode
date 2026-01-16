package com.qodein.feature.post.submission.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.component.ButtonSize
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeinBackIconButton
import com.qodein.core.designsystem.component.QodeinTopAppBar
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
    QodeinTopAppBar(
        title = "",
        navigationIcon = { QodeinBackIconButton({ onNavigateBack() }) },
        modifier = modifier,
        actions = {
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

@PreviewLightDark
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
