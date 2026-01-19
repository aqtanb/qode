package com.qodein.core.data.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class PostDto(
    @DocumentId
    val id: String = "",

    val authorId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String? = null,

    val title: String = "",
    val content: String = "",
    val imageUrls: List<String> = emptyList(),
    val tags: List<String> = emptyList(),

    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val voteScore: Int = 0,

    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    companion object {
        const val COLLECTION_NAME = "posts"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_AUTHOR_ID = "authorId"
    }
}
