package com.qodein.feature.post.detail

import com.qodein.shared.common.error.OperationError

sealed class PostDetailEvent {
    data class ShowError(val error: OperationError) : PostDetailEvent()
}
