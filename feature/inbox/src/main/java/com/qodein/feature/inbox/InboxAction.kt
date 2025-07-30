package com.qodein.feature.inbox

sealed interface InboxAction {
    data object LoadMessages : InboxAction
    data class FilterMessages(val filter: InboxFilter) : InboxAction
    data class SearchMessages(val query: String) : InboxAction
    data class MarkAsRead(val messageId: String) : InboxAction
    data class ToggleImportant(val messageId: String) : InboxAction
    data class DeleteMessage(val messageId: String) : InboxAction
    data object RefreshMessages : InboxAction
}
