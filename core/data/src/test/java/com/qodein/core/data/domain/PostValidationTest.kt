package com.qodein.core.data.domain

import com.qodein.core.testing.util.assertIsType
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.PostError
import com.qodein.shared.model.Post
import com.qodein.shared.model.UserId
import org.junit.Assert.assertEquals
import org.junit.Test

class PostValidationTest {

    private fun create(
        title: String = "Free coffee",
        content: String = "",
        tags: List<String> = emptyList(),
        imageUrls: List<String> = emptyList()
    ) = Post.create(
        authorId = UserId("alice-id"),
        authorName = "Alice",
        title = title,
        content = content,
        imageUrls = imageUrls,
        tags = tags,
        authorAvatarUrl = null,
    )

    private fun Result<Post, PostError.CreationFailure>.post(): Post = assertIsType<Result.Success<Post>>().data

    private fun Result<Post, PostError.CreationFailure>.error(): PostError.CreationFailure =
        assertIsType<Result.Error<PostError.CreationFailure>>().error

    @Test
    fun blankTitleIsRejected() {
        assertEquals(PostError.CreationFailure.EmptyTitle, create(title = "   ").error())
    }

    @Test
    fun titleIsTrimmedAndCutToMaxLength() {
        val post = create(title = "  ${"a".repeat(Post.TITLE_MAX_LENGTH + 10)}  ").post()

        assertEquals("a".repeat(Post.TITLE_MAX_LENGTH), post.title)
    }

    @Test
    fun moreThanMaxTagsIsRejected() {
        val tags = (1..Post.MAX_TAGS + 1).map { "tag$it" }

        assertEquals(PostError.CreationFailure.TooManyTags, create(tags = tags).error())
    }

    @Test
    fun tagsAreNormalizedAndDeduplicated() {
        val post = create(tags = listOf("Coffee!", "coffee", " ", "Free_Stuff")).post()

        assertEquals(listOf("coffee", "free_stuff"), post.tags)
    }

    @Test
    fun moreThanMaxImagesIsRejected() {
        val urls = (1..Post.MAX_IMAGES + 1).map { "https://cdn.test/$it" }

        assertEquals(PostError.CreationFailure.TooManyImages, create(imageUrls = urls).error())
    }

    @Test
    fun blankImageUrlsAreDropped() {
        val post = create(imageUrls = listOf(" https://cdn.test/1 ", "  ")).post()

        assertEquals(listOf("https://cdn.test/1"), post.imageUrls)
    }

    @Test
    fun contentCollapsesBlankLinesAndKeepsMaxLines() {
        val content = (1..Post.CONTENT_MAX_LINES + 3).joinToString("\n\n\n") { "line $it" }

        val lines = create(content = content).post().content.split('\n')

        assertEquals(Post.CONTENT_MAX_LINES, lines.size)
        assertEquals("line 1", lines.first())
    }

    @Test
    fun newPostStartsWithNoVotes() {
        val post = create().post()

        assertEquals(0, post.upvotes)
        assertEquals(0, post.voteScore)
    }
}
