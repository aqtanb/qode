package com.qodein.core.data.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class PostDto(
    @DocumentId
    val id: String = "",

    @PropertyName("authorId")
    val authorId: String = "",

    @PropertyName("authorName")
    val authorName: String = "",

    @PropertyName("authorAvatarUrl")
    val authorAvatarUrl: String? = null,

    @PropertyName("title")
    val title: String = "",

    @PropertyName("content")
    val content: String? = null,

    @PropertyName("imageUrls")
    val imageUrls: List<String> = emptyList(),

    @PropertyName("tags")
    val tags: List<String> = emptyList(),

    @PropertyName("upvotes")
    val upvotes: Int = 0,

    @PropertyName("downvotes")
    val downvotes: Int = 0,

    @PropertyName("voteScore")
    val voteScore: Int = 0,

    @PropertyName("createdAt")
    @ServerTimestamp
    val createdAt: Timestamp? = null,

    @PropertyName("updatedAt")
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    constructor() : this(
        id = "",
        authorId = "",
        authorName = "",
        authorAvatarUrl = null,
        title = "",
        content = "",
        imageUrls = emptyList(),
        tags = emptyList(),
        upvotes = 0,
        downvotes = 0,
        voteScore = 0,
        createdAt = null,
        updatedAt = null,
    )
}
