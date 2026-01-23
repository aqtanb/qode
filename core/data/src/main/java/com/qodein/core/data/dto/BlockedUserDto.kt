package com.qodein.core.data.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class BlockedUserDto(@DocumentId val blockedUserId: String = "", val blockedAt: Timestamp? = null) {
    companion object {
        const val FIELD_BLOCKED_AT = "blockedAt"
    }
}
