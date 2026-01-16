package com.qodein.feature.post.detail.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.component.QodeinBackIconButton
import com.qodein.core.designsystem.component.QodeinTopAppBar
import com.qodein.core.designsystem.theme.QodeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PostDetailTopAppBar(onNavigationClick: () -> Unit) {
    QodeinTopAppBar(
        title = "Post Detail",
        navigationIcon = { QodeinBackIconButton({ onNavigationClick() }) },
    )
}

@PreviewLightDark
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
