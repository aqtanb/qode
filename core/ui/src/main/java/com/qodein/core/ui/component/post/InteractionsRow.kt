package com.qodein.core.ui.component.post

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.icon.ActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.R
import com.qodein.shared.model.VoteState

@Composable
fun InteractionsRow(
    voteState: VoteState,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VoteButtons(voteState, onUpvote, onDownvote, modifier)

        Spacer(modifier = Modifier.weight(1f))

        FilledTonalIconButton(
            onClick = onShare,
        ) {
            Icon(
                imageVector = ActionIcons.Share,
                contentDescription = stringResource(R.string.ui_action_share),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun VoteButtons(
    voteState: VoteState,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToggleButton(
            checked = voteState == VoteState.UPVOTE,
            onCheckedChange = { onUpvote() },
            shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
            colors = ToggleButtonDefaults.tonalToggleButtonColors(),
        ) {
            Icon(
                imageVector = ActionIcons.Up,
                contentDescription = stringResource(R.string.ui_action_upvote),
            )
            AnimatedVisibility(visible = voteState == VoteState.UPVOTE) {
                Row {
                    Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                    Text(
                        text = stringResource(R.string.ui_label_upvoted),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        ToggleButton(
            checked = voteState == VoteState.DOWNVOTE,
            onCheckedChange = { onDownvote() },
            shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
            colors = ToggleButtonDefaults.tonalToggleButtonColors(),
        ) {
            AnimatedVisibility(visible = voteState == VoteState.DOWNVOTE) {
                Row {
                    Text(
                        text = stringResource(R.string.ui_label_downvoted),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                }
            }
            Icon(
                imageVector = ActionIcons.Down,
                contentDescription = stringResource(R.string.ui_action_downvote),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun InteractionsRowPreview() {
    QodeTheme {
        Surface {
            InteractionsRow(
                voteState = VoteState.UPVOTE,
                onUpvote = {},
                onDownvote = {},
                onShare = {},
            )
        }
    }
}
