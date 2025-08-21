package com.qodein.feature.inbox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.theme.SpacingTokens

@Composable
fun InboxScreen(
    modifier: Modifier = Modifier,
    viewModel: InboxViewModel = hiltViewModel()
) {
    TrackScreenViewEvent(screenName = "Inbox")

    val state by viewModel.state.collectAsState()

    InboxScreenContent(
        state = state,
        onAction = viewModel::handleAction,
        modifier = modifier,
    )
}

@Composable
private fun InboxScreenContent(
    state: InboxUiState,
    onAction: (InboxAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = SpacingTokens.md),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            modifier = Modifier.padding(bottom = SpacingTokens.md),
        ) {
            items(InboxFilter.entries) { filter ->
                FilterChip(
                    onClick = { onAction(InboxAction.FilterMessages(filter)) },
                    label = {
                        Text(
                            text = when (filter) {
                                InboxFilter.ALL -> "All"
                                InboxFilter.UNREAD -> "Unread"
                                InboxFilter.IMPORTANT -> "Important"
                                InboxFilter.PROMO_CODES -> "Promo Codes"
                            },
                        )
                    },
                    selected = state.selectedFilter == filter,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }

        // Messages List
        val filteredMessages = state.messages.filter { message ->
            val matchesFilter = when (state.selectedFilter) {
                InboxFilter.ALL -> true
                InboxFilter.UNREAD -> !message.isRead
                InboxFilter.IMPORTANT -> message.isImportant
                InboxFilter.PROMO_CODES -> message.type == MessageType.PROMO_CODE
            }

            val matchesSearch = if (state.searchQuery.isBlank()) {
                true
            } else {
                message.title.contains(state.searchQuery, ignoreCase = true) ||
                    message.description.contains(state.searchQuery, ignoreCase = true)
            }

            matchesFilter && matchesSearch
        }

        if (filteredMessages.isEmpty()) {
            EmptyInboxState(
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(SpacingTokens.md),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                modifier = Modifier.weight(1f),
            ) {
                items(
                    items = filteredMessages,
                    key = { it.id },
                ) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut(),
                    ) {
                        MessageCard(
                            message = message,
                            onMarkAsRead = { onAction(InboxAction.MarkAsRead(message.id)) },
                            onToggleImportant = { onAction(InboxAction.ToggleImportant(message.id)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageCard(
    message: InboxMessage,
    onMarkAsRead: () -> Unit,
    onToggleImportant: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(SpacingTokens.md),
        colors = CardDefaults.cardColors(
            containerColor = if (message.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            },
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (message.isRead) 2.dp else 4.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.md),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            // Message Type Indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        color = when (message.type) {
                            MessageType.PROMO_CODE -> MaterialTheme.colorScheme.primary
                            MessageType.NOTIFICATION -> MaterialTheme.colorScheme.secondary
                            MessageType.UPDATE -> MaterialTheme.colorScheme.tertiary
                            MessageType.OFFER -> MaterialTheme.colorScheme.error
                        }.copy(alpha = 0.2f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when (message.type) {
                        MessageType.PROMO_CODE -> "🎉"
                        MessageType.NOTIFICATION -> "🔔"
                        MessageType.UPDATE -> "📱"
                        MessageType.OFFER -> "💰"
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            // Message Content
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = message.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (message.isRead) FontWeight.Normal else FontWeight.Bold,
                        ),
                        color = if (message.isRead) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    // Unread Indicator
                    if (!message.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(SpacingTokens.xs))

                Text(
                    text = message.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(SpacingTokens.sm))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )

                    // Important Star
                    IconButton(
                        onClick = onToggleImportant,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = if (message.isImportant) {
                                Icons.Filled.Star
                            } else {
                                Icons.Outlined.StarBorder
                            },
                            contentDescription = if (message.isImportant) "Remove from important" else "Mark as important",
                            tint = if (message.isImportant) {
                                Color(0xFFFFD700)
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyInboxState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "📬",
            style = MaterialTheme.typography.displayLarge,
        )

        Spacer(modifier = Modifier.height(SpacingTokens.md))

        Text(
            text = "No messages found",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )

        Spacer(modifier = Modifier.height(SpacingTokens.xs))

        Text(
            text = "Check back later for new notifications and updates",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
    }
}
