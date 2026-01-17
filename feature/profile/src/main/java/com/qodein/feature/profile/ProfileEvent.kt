package com.qodein.feature.profile

import com.qodein.shared.model.PostId
import com.qodein.shared.model.PromocodeId

sealed interface ProfileEvent {
    data object SignedOut : ProfileEvent
    data object NavigateToAuth : ProfileEvent
    data object NavigateToBlockedUsers : ProfileEvent

    data class NavigateToPromocodeDetail(val promocodeId: PromocodeId) : ProfileEvent
    data class NavigateToPostDetail(val postId: PostId) : ProfileEvent
}
