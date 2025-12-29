package com.qodein.feature.promocode.detail

import com.qodein.core.ui.AuthPromptAction
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.UserId

sealed class PromocodeDetailEvent {
    data object NavigateBack : PromocodeDetailEvent()
    data class NavigateToAuth(val action: AuthPromptAction) : PromocodeDetailEvent()
    data class NavigateToReport(val reportedItemId: String, val itemTitle: String, val itemAuthor: String?) : PromocodeDetailEvent()
    data class NavigateToBlockUser(val userId: UserId, val username: String?, val photoUrl: String?) : PromocodeDetailEvent()

    data class SharePromocode(val promocode: Promocode) : PromocodeDetailEvent()
    data class CopyCodeToClipboard(val code: String) : PromocodeDetailEvent()
    data class ShowError(val error: OperationError) : PromocodeDetailEvent()
}
