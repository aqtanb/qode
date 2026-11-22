package com.qodein.shared.ios

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.usecase.banner.GetBannersUseCase
import com.qodein.shared.domain.usecase.post.GetPostByIdUseCase
import com.qodein.shared.domain.usecase.post.GetPostsUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodeUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodesUseCase
import com.qodein.shared.domain.usecase.service.GetPopularServicesUseCase
import com.qodein.shared.domain.usecase.service.SearchServicesUseCase
import com.qodein.shared.model.Banner
import com.qodein.shared.model.Discount
import com.qodein.shared.model.PaginationCursor
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.PostSortBy
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.PromocodeSortBy
import com.qodein.shared.model.Service
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Thrown to Swift when a use case returns an error. [code] is the error's simple name
 * (e.g. "Offline", "InvalidCode"), so the UI can pick a message.
 */
class QodeException(val code: String, message: String) : Exception(message) {
    val isOffline: Boolean get() = code == "Offline"
}

internal suspend fun <T> call(block: suspend () -> Result<T, OperationError>): T {
    val result = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        throw QodeException(code = "Unknown", message = e.message ?: "Unexpected error")
    }
    return when (result) {
        is Result.Success -> result.data
        is Result.Error -> throw QodeException(result.error::class.simpleName ?: "Unknown", result.error.toString())
    }
}

enum class DiscountKind { PERCENTAGE, FIXED_AMOUNT, FREE_ITEM }

class PromocodeItem(
    val id: String,
    val code: String,
    val discountKind: DiscountKind,
    val discountValue: Double,
    val freeItem: String?,
    val minimumOrderAmount: Double,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val serviceName: String,
    val serviceLogoUrl: String?,
    val serviceSiteUrl: String?,
    val description: String?,
    val voteScore: Int,
    val isVerified: Boolean,
    val authorId: String,
    val authorUsername: String?,
    val imageUrls: List<String>
)

class PostItem(
    val id: String,
    val title: String,
    val content: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String?,
    val imageUrls: List<String>,
    val tags: List<String>,
    val voteScore: Int,
    val createdEpochMillis: Long
)

class BannerItem(val id: String, val imageUrl: String, val title: String?, val description: String?, val ctaUrl: String?)

class ServiceItem(val id: String, val name: String, val siteUrl: String, val logoUrl: String?, val promocodeCount: Int)

/** Swift-facing entry point to the shared use cases. Results become return values or [QodeException]. */
class QodeApi : KoinComponent {
    private val getPromocodes: GetPromocodesUseCase by inject()
    private val getPromocode: GetPromocodeUseCase by inject()
    private val getPosts: GetPostsUseCase by inject()
    private val getPost: GetPostByIdUseCase by inject()
    private val getBanners: GetBannersUseCase by inject()
    private val getPopularServices: GetPopularServicesUseCase by inject()
    private val searchServicesUseCase: SearchServicesUseCase by inject()

    private var promocodeCursor: PaginationCursor<PromocodeSortBy>? = null
    private var postCursor: PaginationCursor<PostSortBy>? = null

    val hasMorePromocodes: Boolean get() = promocodeCursor != null
    val hasMorePosts: Boolean get() = postCursor != null

    /** Loads the first page when [nextPage] is false, otherwise the page after the last one loaded. */
    @Throws(Exception::class)
    suspend fun promocodes(
        newest: Boolean,
        serviceIds: List<String>,
        nextPage: Boolean
    ): List<PromocodeItem> {
        val cursor = if (nextPage) promocodeCursor ?: return emptyList() else null
        val page = call {
            getPromocodes(
                sortBy = if (newest) PromocodeSortBy.NEWEST else PromocodeSortBy.POPULARITY,
                filterByServices = serviceIds.ifEmpty { null },
                paginationRequest = PaginationRequest(GetPromocodesUseCase.DEFAULT_LIMIT * 2, cursor),
            )
        }
        promocodeCursor = page.nextCursor
        return page.data.map { it.toItem() }
    }

    @Throws(Exception::class)
    suspend fun promocode(id: String): PromocodeItem = call { getPromocode(PromocodeId(id)) }.toItem()

    @Throws(Exception::class)
    suspend fun posts(nextPage: Boolean): List<PostItem> {
        val cursor = if (nextPage) postCursor ?: return emptyList() else null
        val page = call { getPosts(cursor) }
        postCursor = page.nextCursor
        return page.data.map { it.toItem() }
    }

    @Throws(Exception::class)
    suspend fun post(id: String): PostItem = call { getPost(PostId(id)) }.toItem()

    @Throws(Exception::class)
    suspend fun banners(languageCode: String): List<BannerItem> =
        call { getBanners() }.sortedByDescending { it.priority }.map { it.toItem(languageCode) }

    @Throws(Exception::class)
    suspend fun popularServices(): List<ServiceItem> = call { getPopularServices() }.map { it.toItem() }

    @Throws(Exception::class)
    suspend fun searchServices(query: String): List<ServiceItem> = call { searchServicesUseCase(flowOf(query)).first() }.map { it.toItem() }
}

internal fun Promocode.toItem(): PromocodeItem {
    val kind = when (discount) {
        is Discount.Percentage -> DiscountKind.PERCENTAGE
        is Discount.FixedAmount -> DiscountKind.FIXED_AMOUNT
        is Discount.FreeItem -> DiscountKind.FREE_ITEM
    }
    return PromocodeItem(
        id = id.value,
        code = code,
        discountKind = kind,
        discountValue = discount.value,
        freeItem = (discount as? Discount.FreeItem)?.description,
        minimumOrderAmount = minimumOrderAmount,
        startEpochMillis = startDate.toEpochMilliseconds(),
        endEpochMillis = endDate.toEpochMilliseconds(),
        serviceName = serviceName,
        serviceLogoUrl = serviceLogoUrl,
        serviceSiteUrl = serviceSiteUrl,
        description = description,
        voteScore = voteScore,
        isVerified = isVerified,
        authorId = authorId.value,
        authorUsername = authorUsername,
        imageUrls = imageUrls,
    )
}

internal fun Post.toItem() =
    PostItem(
        id = id.value,
        title = title,
        content = content,
        authorId = authorId.value,
        authorName = authorName,
        authorAvatarUrl = authorAvatarUrl,
        imageUrls = imageUrls,
        tags = tags,
        voteScore = voteScore,
        createdEpochMillis = createdAt.toEpochMilliseconds(),
    )

private fun Banner.toItem(languageCode: String) =
    BannerItem(
        id = id.value,
        imageUrl = imageUrl,
        title = ctaTitle[languageCode] ?: ctaTitle["default"],
        description = ctaDescription[languageCode] ?: ctaDescription["default"],
        ctaUrl = ctaUrl,
    )

private fun Service.toItem() = ServiceItem(id.value, name, siteUrl, logoUrl, promocodeCount)
