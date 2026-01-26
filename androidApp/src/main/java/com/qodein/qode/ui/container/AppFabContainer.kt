package com.qodein.qode.ui.container

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import com.qodein.core.designsystem.component.AutoHideDirection
import com.qodein.core.designsystem.component.AutoHidingContent
import com.qodein.core.designsystem.component.AutoHidingState
import com.qodein.core.designsystem.icon.PostIcons
import com.qodein.core.designsystem.icon.PromocodeIcons
import com.qodein.qode.R
import com.qodein.qode.navigation.NavigationActions
import com.qodein.qode.ui.QodeAppState
import com.qodein.qode.ui.state.AppUiEvents

/**
 * Container responsible for floating action button with auto-hiding behavior.
 *
 * Handles:
 * - Context-aware FAB actions per screen (Create promocode on home, Add to feed, etc.)
 * - Auto-hiding behavior synchronized with bottom navigation
 * - Smart visibility logic (only shown on top-level destinations)
 * - Smooth animations and transitions
 *
 * Benefits:
 * - Context-aware actions that make sense for each screen
 * - Consistent auto-hiding behavior across the app
 * - Clean separation from main app logic
 * - Enterprise-grade performance optimizations
 */
@Composable
fun AppFabContainer(
    appState: QodeAppState,
    onEvent: (AppUiEvents) -> Unit
) {
    val currentScrollableState by appState.currentScrollableState
    val fabAutoHidingState by appState.fabAutoHidingState
    LaunchedEffect(currentScrollableState) {
        val extractor = appState.getScrollExtractor()
        val scrollState = currentScrollableState

        if (extractor != null && scrollState != null) {
            snapshotFlow {
                extractor(scrollState)
            }.collect { scrollInfo ->
                appState.updateScrollInfo(scrollInfo)
            }
        }
    }

    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    val currentTopLevelDestination = appState.currentTopLevelDestination
    if (currentTopLevelDestination != null) {
        AppFabMenu(
            fabMenuExpanded = fabMenuExpanded,
            onFabMenuExpandedChange = { fabMenuExpanded = it },
            fabAutoHidingState = fabAutoHidingState,
            onEvent = onEvent,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AppFabMenu(
    fabMenuExpanded: Boolean,
    onFabMenuExpandedChange: (Boolean) -> Unit,
    fabAutoHidingState: AutoHidingState?,
    onEvent: (AppUiEvents) -> Unit
) {
    val fabContent: @Composable () -> Unit = {
        FloatingActionButtonMenu(
            expanded = fabMenuExpanded,
            modifier = Modifier,
            button = {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        if (fabMenuExpanded) {
                            TooltipAnchorPosition.Start
                        } else {
                            TooltipAnchorPosition.Above
                        },
                    ),
                    tooltip = { PlainTooltip { Text(stringResource(R.string.app_fab_tooltip)) } },
                    state = rememberTooltipState(),
                ) {
                    ToggleFloatingActionButton(
                        checked = fabMenuExpanded,
                        onCheckedChange = { onFabMenuExpandedChange(!fabMenuExpanded) },
                    ) {
                        val imageVector by remember {
                            derivedStateOf {
                                if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                            }
                        }
                        Icon(
                            painter = rememberVectorPainter(imageVector),
                            contentDescription = null,
                            modifier = Modifier.animateIcon({ checkedProgress }),
                        )
                    }
                }
            },
        ) {
            FloatingActionButtonMenuItem(
                onClick = {
                    onFabMenuExpandedChange(false)
                    onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToPromocodeSubmission))
                },
                icon = { Icon(PromocodeIcons.Promocode, contentDescription = null) },
                text = { Text(text = stringResource(R.string.app_submit_promocode)) },
            )
            FloatingActionButtonMenuItem(
                onClick = {
                    onFabMenuExpandedChange(false)
                    onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToPostSubmission))
                },
                icon = { Icon(PostIcons.Post, contentDescription = null) },
                text = { Text(text = stringResource(R.string.app_create_post)) },
            )
        }
    }

    if (fabAutoHidingState != null) {
        AutoHidingContent(
            state = fabAutoHidingState,
            direction = AutoHideDirection.UP,
        ) {
            fabContent()
        }
    } else {
        fabContent()
    }
}
