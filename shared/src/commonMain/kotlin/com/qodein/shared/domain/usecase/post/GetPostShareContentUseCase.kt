package com.qodein.shared.domain.usecase.post

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.DeeplinkConfig
import com.qodein.shared.domain.provider.ShareStringProvider
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.ShareContent

class GetPostShareContentUseCase(
    private val getPostByIdUseCase: GetPostByIdUseCase,
    private val deeplinkConfig: DeeplinkConfig,
    private val stringProvider: ShareStringProvider
) {
    suspend operator fun invoke(postId: PostId): Result<ShareContent, OperationError> =
        when (val result = getPostByIdUseCase(postId)) {
            is Result.Error -> Result.Error(result.error)
            is Result.Success -> {
                val post = result.data
                val shareContent = buildShareContent(post)
                Result.Success(shareContent)
            }
        }

    private fun buildShareContent(post: Post): ShareContent {
        val shareText = buildString {
            append(stringProvider.getPostShareHeader(post.title))
            append("\n\n")

            post.content.takeIf { it.isNotBlank() }?.let { content ->
                val preview = if (content.length > 150) {
                    content.take(147) + "..."
                } else {
                    content
                }
                append(preview)
                append("\n\n")
            }

            append(stringProvider.getAuthorAttribution(post.authorName))
            append("\n\n")

            append(stringProvider.getPostShareCallToAction())
            append("\n\n")

            append(buildDeeplink(post.id))
        }

        return ShareContent(
            title = post.title,
            text = shareText,
            url = buildDeeplink(post.id),
        )
    }

    private fun buildDeeplink(postId: PostId): String = deeplinkConfig.getPostWebUrl(postId.value)
}
