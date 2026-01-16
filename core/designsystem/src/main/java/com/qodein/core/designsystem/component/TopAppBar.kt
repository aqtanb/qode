package com.qodein.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.R
import com.qodein.core.designsystem.icon.ActionIcons
import com.qodein.core.designsystem.icon.NavigationIcons
import com.qodein.core.designsystem.theme.QodeTheme

/**
 * Top app bar component for Qode design system
 *
 * @param modifier Modifier to be applied to the app bar
 * @param title The title string to display
 * @param navigationIcon Navigation icon composable
 * @param actions Action buttons to display
 * @param scrollBehavior Scroll behavior configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QodeinTopAppBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val titleContent: @Composable () -> Unit = {
        title?.let {
            Text(
                text = it,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }

    CenterAlignedTopAppBar(
        title = titleContent,
        modifier = modifier,
        navigationIcon = navigationIcon ?: {},
        actions = actions ?: {},
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    )
}

@Composable
fun QodeinBackIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeinIconButton(
        onClick = onClick,
        icon = NavigationIcons.Back,
        contentDescription = stringResource(R.string.ds_navigation_back),
        size = ButtonSize.Large,
        modifier = modifier,
    )
}

@Composable
fun QodeinCloseIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeinIconButton(
        onClick = onClick,
        icon = NavigationIcons.Close,
        contentDescription = stringResource(R.string.ds_navigation_close),
        size = ButtonSize.Large,
        modifier = modifier,
    )
}

@Composable
fun QodeinMoreIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeinIconButton(
        onClick = onClick,
        icon = ActionIcons.MoreVert,
        contentDescription = stringResource(R.string.ds_action_more),
        size = ButtonSize.Large,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun QodeinTopAppBarPreview() {
    QodeTheme {
        Surface {
            Column {
                QodeinTopAppBar(
                    title = "Title",
                    navigationIcon = { QodeinBackIconButton({}) },
                    actions = { QodeinMoreIconButton({}) },
                )
                Box(
                    modifier = Modifier
                        .height(64.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "Content")
                }
            }
        }
    }
}
