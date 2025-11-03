package com.qodein.feature.post.detail.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PostDetailTopAppBar(onNavigationClick: () -> Unit) {
    QodeTopAppBar(
        title = "Post Detail",
        navigationIcon = QodeActionIcons.Back,
        onNavigationClick = onNavigationClick,
    )
}

@ThemePreviews
@Composable
private fun PostDetailTopAppBarPreview() {
    QodeTheme {
        Surface {
            PostDetailTopAppBar(
                onNavigationClick = {},
            )
        }
    }
}
