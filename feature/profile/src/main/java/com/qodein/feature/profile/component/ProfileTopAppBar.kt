package com.qodein.feature.profile.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.component.QodeinBackIconButton
import com.qodein.core.designsystem.component.QodeinDropdownMenuGrouped
import com.qodein.core.designsystem.component.QodeinMenuGroup
import com.qodein.core.designsystem.component.QodeinMenuItem
import com.qodein.core.designsystem.component.QodeinMoreIconButton
import com.qodein.core.designsystem.component.QodeinTopAppBar
import com.qodein.core.designsystem.icon.ActionIcons
import com.qodein.core.designsystem.icon.NavigationIcons
import com.qodein.core.designsystem.icon.UIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.profile.ProfileAction
import com.qodein.feature.profile.R
import com.qodein.core.ui.R as CoreUiR

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ProfileTopAppBar(
    onBackClick: () -> Unit,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val groups = listOf(
        QodeinMenuGroup(
            listOf(
                QodeinMenuItem(
                    text = stringResource(R.string.profile_action_blocked),
                    onClick = { onAction(ProfileAction.BlockedClicked) },
                    leadingIcon = UIIcons.Block,
                    trailingIcon = NavigationIcons.ChevronRight,
                ),
            ),
        ),

        QodeinMenuGroup(
            listOf(
                QodeinMenuItem(
                    text = stringResource(R.string.profile_action_signout),
                    onClick = { onAction(ProfileAction.SignOutClicked) },
                    leadingIcon = ActionIcons.SignOut,
                    trailingIcon = NavigationIcons.ChevronRight,
                ),
            ),
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    )

    QodeinTopAppBar(
        title = stringResource(CoreUiR.string.ui_profile),
        modifier = modifier,
        navigationIcon = { QodeinBackIconButton(onClick = onBackClick) },
        actions = {
            Box(
                modifier = Modifier.padding(top = SpacingTokens.sm),
            ) {
                QodeinMoreIconButton(onClick = { isMenuExpanded = true })
                QodeinDropdownMenuGrouped(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                    groups = groups,
                )
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun ProfileTopAppBarPreview() {
    QodeTheme {
        Surface {
            ProfileTopAppBar(
                onBackClick = {},
                onAction = {},
            )
        }
    }
}
