package com.qodein.shared.domain.usecase.promocode

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.CommentRepository
import com.qodein.shared.model.Comment
import com.qodein.shared.model.CommentParentType
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow

class AddCommentUseCase constructor(private val commentRepository: CommentRepository) {
    operator fun invoke(
        parentId: String, // PromoCodeId.value or PostId.value
        parentType: CommentParentType,
        userId: UserId,
        authorUsername: String,
        authorAvatarUrl: String? = null,
        authorCountry: String? = null,
        comment: String,
        imageUrls: List<String> = emptyList()
    ): Flow<Result<Comment>> {
        val trimmedComment = comment.trim()
        require(trimmedComment.isNotBlank()) { "Comment cannot be blank" }
        require(trimmedComment.length <= 1000) { "Comment too long (max 1000 characters)" }
        require(authorUsername.isNotBlank()) { "Author username cannot be blank" }

        return commentRepository.createComment(
            parentId = parentId,
            parentType = parentType,
            authorId = userId,
            authorUsername = authorUsername,
            authorAvatarUrl = authorAvatarUrl,
            authorCountry = authorCountry,
            content = trimmedComment,
            imageUrls = imageUrls,
        ).asResult()
    }
}
