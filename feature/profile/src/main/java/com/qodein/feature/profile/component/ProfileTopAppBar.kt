package com.qodein.feature.profile.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.component.AutoHideDirection
import com.qodein.core.designsystem.component.AutoHidingContent
import com.qodein.core.designsystem.component.QodeinBackIconButton
import com.qodein.core.designsystem.component.QodeinTopAppBar
import com.qodein.core.designsystem.component.rememberAutoHidingState
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileTopAppBar(
    scrollState: ScrollState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val autoHidingState = rememberAutoHidingState(scrollState = scrollState)
    AutoHidingContent(
        state = autoHidingState,
        direction = AutoHideDirection.DOWN,
    ) {
        QodeinTopAppBar(
            title = stringResource(R.string.ui_profile),
            modifier = modifier,
            navigationIcon = { QodeinBackIconButton(onClick = onBackClick) },
        )
    }
}

@PreviewLightDark
@Composable
private fun ProfileTopAppBarPreview() {
    QodeTheme {
        Surface {
            ProfileTopAppBar(
                scrollState = rememberScrollState(),
                onBackClick = {},
            )
        }
    }
}
