package com.qodein.shared.domain.usecase.post

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.model.Post
import com.qodein.shared.model.Tag
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow

class CreatePostUseCase constructor(private val postRepository: PostRepository) {
    operator fun invoke(
        authorId: UserId,
        authorUsername: String,
        authorAvatarUrl: String? = null,
        authorCountry: String? = null,
        title: String? = null,
        content: String,
        imageUrls: List<String> = emptyList(),
        tags: List<Tag> = emptyList()
    ): Flow<Result<Post>> {
        require(content.isNotBlank()) { "Post content cannot be blank" }
        require(content.length <= 5000) { "Post content cannot exceed 5000 characters" }
        require(authorUsername.isNotBlank()) { "Author username cannot be blank" }
        require(imageUrls.size <= 10) { "Post cannot have more than 10 images" }
        require(tags.size <= 10) { "Post cannot have more than 10 tags" }

        title?.let {
            require(it.length <= 200) { "Post title cannot exceed 200 characters" }
        }

        val post = Post.create(
            authorId = authorId,
            authorUsername = authorUsername.trim(),
            authorAvatarUrl = authorAvatarUrl,
            authorCountry = authorCountry?.uppercase(),
            title = title?.trim(),
            content = content.trim(),
            imageUrls = imageUrls.map { it.trim() }.filter { it.isNotBlank() },
            tags = tags.distinctBy { it.name },
        ).getOrThrow()

        return postRepository.createPost(post).asResult()
    }
}
