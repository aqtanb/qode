package com.qodein.shared.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("submitted_promocodes_count") val submittedPromocodesCount: Int = 0,
    @SerialName("submitted_posts_count") val submittedPostsCount: Int = 0,
    @SerialName("legal_policies_accepted_at") val legalPoliciesAcceptedAt: String? = null
) {
    companion object {
        const val TABLE_NAME = "users"
    }
}

@Serializable
data class UserInsertDto(
    val id: String,
    @SerialName("display_name") val displayName: String?,
    @SerialName("photo_url") val photoUrl: String?,
    @SerialName("legal_policies_accepted_at") val legalPoliciesAcceptedAt: String?
)

@Serializable
data class BlockDto(
    @SerialName("user_id") val userId: String,
    @SerialName("blocked_user_id") val blockedUserId: String,
    @SerialName("blocked_at") val blockedAt: String? = null
) {
    companion object {
        const val TABLE_NAME = "blocks"
    }
}

@Serializable
data class UserInteractionDto(
    @SerialName("user_id") val userId: String,
    @SerialName("item_type") val itemType: String,
    @SerialName("item_id") val itemId: String,
    @SerialName("vote_state") val voteState: String = "NONE",
    val bookmarked: Boolean = false
) {
    companion object {
        const val TABLE_NAME = "interactions"
    }
}
