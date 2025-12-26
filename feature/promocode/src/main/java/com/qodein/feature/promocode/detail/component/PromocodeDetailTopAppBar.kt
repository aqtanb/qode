package com.qodein.feature.promocode.detail.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.qodein.core.designsystem.component.QodeDropdownMenuItem
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.UIIcons
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.UserId
import com.qodein.core.ui.R as CoreUiR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PromocodeDetailTopAppBar(
    title: String,
    promocodeId: PromocodeId,
    currentUserId: UserId?,
    authorId: UserId?,
    onNavigateBack: () -> Unit,
    onBlockUserClick: (UserId) -> Unit,
    onReportPromocodeClick: (PromocodeId) -> Unit
) {
    var isMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val isOwnPromocode = currentUserId != null && currentUserId == authorId

    QodeTopAppBar(
        title = title,
        navigationIcon = QodeActionIcons.Back,
        onNavigationClick = onNavigateBack,
        customActions = if (!isOwnPromocode) {
            {
                Box {
                    IconButton(onClick = { isMenuExpanded = true }) {
                        Icon(
                            imageVector = UIIcons.MoreVert,
                            contentDescription = stringResource(CoreUiR.string.cd_more_options),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false },
                        modifier = Modifier.widthIn(min = SizeTokens.Menu.minWidth),
                    ) {
                        QodeDropdownMenuItem(
                            text = stringResource(CoreUiR.string.action_block),
                            leadingIcon = UIIcons.Block,
                            enabled = authorId != null,
                            minHeight = SizeTokens.Button.heightXL,
                            onClick = {
                                val userId = authorId ?: return@QodeDropdownMenuItem
                                isMenuExpanded = false
                                onBlockUserClick(userId)
                            },
                        )

                        HorizontalDivider()

                        QodeDropdownMenuItem(
                            text = stringResource(CoreUiR.string.action_report),
                            leadingIcon = UIIcons.Report,
                            minHeight = SizeTokens.Button.heightXL,
                            onClick = {
                                isMenuExpanded = false
                                onReportPromocodeClick(promocodeId)
                            },
                        )
                    }
                }
            }
        } else {
            null
        },
    )
}
