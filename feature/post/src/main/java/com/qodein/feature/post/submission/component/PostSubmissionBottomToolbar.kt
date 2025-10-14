package com.qodein.feature.post.submission.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.OpacityTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.post.R

@Composable
internal fun PostSubmissionBottomToolbar(
    isImageLimitReached: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = SpacingTokens.md, vertical = SpacingTokens.xs),
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(
            imageVector = QodeNavigationIcons.Gallery,
            contentDescription = stringResource(R.string.cd_add_image),
            modifier = Modifier
                .size(SizeTokens.Icon.sizeLarge)
                .clickable { onClick() },
            tint = if (isImageLimitReached) {
                MaterialTheme.colorScheme.onBackground.copy(alpha = OpacityTokens.DISABLED)
            } else {
                MaterialTheme.colorScheme.onBackground
            },
        )
    }
}

@PreviewLightDark
@Composable
private fun PostSubmissionBottomToolbarEnabledPreview() {
    QodeTheme {
        PostSubmissionBottomToolbar(
            isImageLimitReached = false,
            onClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun PostSubmissionBottomToolbarDisabledPreview() {
    QodeTheme {
        PostSubmissionBottomToolbar(
            isImageLimitReached = true,
            onClick = {},
        )
    }
}
