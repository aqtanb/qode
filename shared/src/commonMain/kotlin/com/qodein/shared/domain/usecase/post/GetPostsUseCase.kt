package com.qodein.shared.domain.usecase.post

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.domain.repository.PostSortBy
import com.qodein.shared.model.Post
import kotlinx.coroutines.flow.Flow

class GetPostsUseCase constructor(private val postRepository: PostRepository) {
    operator fun invoke(
        query: String? = null,
        sortBy: PostSortBy = PostSortBy.NEWEST,
        filterByTag: String? = null,
        filterByAuthor: String? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<Result<List<Post>>> {
        require(limit in 1..100) { "Limit must be between 1 and 100" }
        require(offset >= 0) { "Offset must be non-negative" }

        val trimmedQuery = query?.trim()?.takeIf { it.isNotBlank() }
        val trimmedTag = filterByTag?.trim()?.takeIf { it.isNotBlank() }
        val trimmedAuthor = filterByAuthor?.trim()?.takeIf { it.isNotBlank() }

        return postRepository.getPosts(
            query = trimmedQuery,
            sortBy = sortBy,
            filterByTag = trimmedTag,
            filterByAuthor = trimmedAuthor,
            limit = limit,
            offset = offset,
        ).asResult()
    }
}
