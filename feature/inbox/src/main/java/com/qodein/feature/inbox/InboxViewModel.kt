package com.qodein.feature.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logFilterContent
import com.qodein.core.analytics.logSearch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(private val analyticsHelper: AnalyticsHelper) : ViewModel() {

    private val _state = MutableStateFlow(InboxUiState())
    val state: StateFlow<InboxUiState> = _state.asStateFlow()

    init {
        handleAction(InboxAction.LoadMessages)
    }

    fun handleAction(action: InboxAction) {
        viewModelScope.launch {
            when (action) {
                is InboxAction.LoadMessages -> loadMessages()
                is InboxAction.FilterMessages -> filterMessages(action.filter)
                is InboxAction.SearchMessages -> searchMessages(action.query)
                is InboxAction.MarkAsRead -> markAsRead(action.messageId)
                is InboxAction.ToggleImportant -> toggleImportant(action.messageId)
                is InboxAction.DeleteMessage -> deleteMessage(action.messageId)
                is InboxAction.RefreshMessages -> refreshMessages()
            }
        }
    }

    private fun loadMessages() {
        _state.value = _state.value.copy(isLoading = true)

        // Mock data for beautiful display
        val mockMessages = listOf(
            InboxMessage(
                id = "1",
                title = "🎉 New Promo Code Available",
                description = "Get 50% off at your favorite store with code SAVE50",
                type = MessageType.PROMO_CODE,
                timestamp = "2 min ago",
                isImportant = true,
            ),
            InboxMessage(
                id = "2",
                title = "💰 Cashback Reward",
                description = "You've earned $15 cashback from your recent purchase",
                type = MessageType.OFFER,
                timestamp = "1 hour ago",
            ),
            InboxMessage(
                id = "3",
                title = "📱 App Update Available",
                description = "New features and bug fixes are now available",
                type = MessageType.UPDATE,
                timestamp = "3 hours ago",
            ),
            InboxMessage(
                id = "4",
                title = "🛍️ Weekend Sale Alert",
                description = "Don't miss out on exclusive weekend deals",
                type = MessageType.NOTIFICATION,
                timestamp = "1 day ago",
                isRead = true,
            ),
            InboxMessage(
                id = "5",
                title = "🎯 Personalized Offers",
                description = "We found some deals you might love",
                type = MessageType.OFFER,
                timestamp = "2 days ago",
                isRead = true,
            ),
        )

        _state.value = _state.value.copy(
            messages = mockMessages,
            isLoading = false,
        )
    }

    private fun filterMessages(filter: InboxFilter) {
        analyticsHelper.logFilterContent(
            filterType = "inbox_filter",
            filterValue = filter.name.lowercase(),
        )
        _state.value = _state.value.copy(selectedFilter = filter)
    }

    private fun searchMessages(query: String) {
        if (query.isNotBlank()) {
            analyticsHelper.logSearch(query)
        }
        _state.value = _state.value.copy(searchQuery = query)
    }

    private fun markAsRead(messageId: String) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                type = "mark_message_read",
                extras = listOf(
                    AnalyticsEvent.Param("message_id", messageId),
                ),
            ),
        )

        val updatedMessages = _state.value.messages.map { message ->
            if (message.id == messageId) {
                message.copy(isRead = true)
            } else {
                message
            }
        }
        _state.value = _state.value.copy(messages = updatedMessages)
    }

    private fun toggleImportant(messageId: String) {
        val updatedMessages = _state.value.messages.map { message ->
            if (message.id == messageId) {
                message.copy(isImportant = !message.isImportant)
            } else {
                message
            }
        }
        _state.value = _state.value.copy(messages = updatedMessages)
    }

    private fun deleteMessage(messageId: String) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                type = "delete_message",
                extras = listOf(
                    AnalyticsEvent.Param("message_id", messageId),
                ),
            ),
        )

        val updatedMessages = _state.value.messages.filter { it.id != messageId }
        _state.value = _state.value.copy(messages = updatedMessages)
    }

    private fun refreshMessages() {
        loadMessages()
    }
}
