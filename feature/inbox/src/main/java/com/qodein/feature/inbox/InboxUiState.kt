package com.qodein.feature.inbox

data class InboxUiState(
    val messages: List<InboxMessage> = emptyList(),
    val isLoading: Boolean = false,
    val selectedFilter: InboxFilter = InboxFilter.ALL,
    val searchQuery: String = ""
)

data class InboxMessage(
    val id: String,
    val title: String,
    val description: String,
    val type: MessageType,
    val timestamp: String,
    val isRead: Boolean = false,
    val isImportant: Boolean = false
)

enum class MessageType {
    PROMO_CODE,
    NOTIFICATION,
    UPDATE,
    OFFER
}

enum class InboxFilter {
    ALL,
    UNREAD,
    IMPORTANT,
    PROMO_CODES
}
