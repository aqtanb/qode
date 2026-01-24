package com.qodein.core.ui.component.post

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.icon.ActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.shared.model.VoteState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VoteButtonGroup(
    voteCount: Int,
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
                contentDescription = null,
            )
            AnimatedVisibility(visible = voteState == VoteState.UPVOTE) {
                Row {
                    Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                    Text(
                        text = "Upvoted",
                        style = MaterialTheme.typography.titleMedium,
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
                        text = "Downvoted",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                }
            }
            Icon(
                imageVector = ActionIcons.Down,
                contentDescription = null,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun VoteButtonGroupPreview() {
    QodeTheme {
        Surface {
            VoteButtonGroup(
                voteCount = 125,
                voteState = VoteState.UPVOTE,
                onUpvote = {},
                onDownvote = {},
            )
        }
    }
}
