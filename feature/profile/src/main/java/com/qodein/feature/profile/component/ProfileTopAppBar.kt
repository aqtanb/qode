package com.qodein.feature.profile.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.AutoHideDirection
import com.qodein.core.designsystem.component.AutoHidingContent
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.component.QodeTopAppBarVariant
import com.qodein.core.designsystem.component.rememberAutoHidingState
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme

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
        QodeTopAppBar(
            title = "",
            navigationIcon = QodeActionIcons.Back,
            onNavigationClick = onBackClick,
            variant = QodeTopAppBarVariant.Transparent,
            statusBarPadding = true,
            modifier = modifier,
        )
    }
}

@ThemePreviews
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
