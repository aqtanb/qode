package com.qodein.shared.domain.usecase.post

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import kotlinx.coroutines.flow.Flow

/**
 * Use case for fetching a single post by ID.
 *
 * Delegates to repository for data fetching.
 */
class GetPostByIdUseCase(private val postRepository: PostRepository) {

    operator fun invoke(id: PostId): Flow<Result<Post?, OperationError>> = postRepository.getPostById(id)
}
