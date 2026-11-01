package com.qodein.shared.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Row of `posts_view`: a post joined with its author. */
@Serializable
data class PostDto(
    val id: String,
    val title: String,
    val content: String = "",
    @SerialName("image_urls") val imageUrls: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    @SerialName("vote_score") val voteScore: Int = 0,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("author_id") val authorId: String,
    @SerialName("author_name") val authorName: String? = null,
    @SerialName("author_avatar_url") val authorAvatarUrl: String? = null
) {
    companion object {
        const val VIEW_NAME = "posts_view"
        const val TABLE_NAME = "posts"
    }
}

@Serializable
data class PostInsertDto(
    val id: String,
    @SerialName("author_id") val authorId: String,
    val title: String,
    val content: String,
    @SerialName("image_urls") val imageUrls: List<String>,
    val tags: List<String>
)
