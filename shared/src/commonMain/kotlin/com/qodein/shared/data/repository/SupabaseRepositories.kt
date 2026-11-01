package com.qodein.shared.data.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.DatabaseError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.ServiceError
import com.qodein.shared.common.flatMap
import com.qodein.shared.data.datasource.SupabaseAppConfigDataSource
import com.qodein.shared.data.datasource.SupabaseBannerDataSource
import com.qodein.shared.data.datasource.SupabasePostDataSource
import com.qodein.shared.data.datasource.SupabasePromocodeDataSource
import com.qodein.shared.data.datasource.SupabaseServiceDataSource
import com.qodein.shared.data.mapper.toDomain
import com.qodein.shared.data.mapper.toInsertDto
import com.qodein.shared.data.util.PageCursor
import com.qodein.shared.data.util.remoteCall
import com.qodein.shared.domain.repository.AppUpdateConfigRepository
import com.qodein.shared.domain.repository.BannerRepository
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.domain.repository.ServiceRepository
import com.qodein.shared.model.AppUpdateConfig
import com.qodein.shared.model.Banner
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationCursor
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.PostSortBy
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.PromocodeSortBy
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId
import com.qodein.shared.model.UserId

private fun pageCursor(cursor: Any?): PageCursor? = (cursor as? PaginationCursor<*>)?.value as? PageCursor

class PromocodeRepositoryImpl(private val dataSource: SupabasePromocodeDataSource) : PromocodeRepository {
    override suspend fun createPromocode(promocode: Promocode): Result<Unit, OperationError> =
        remoteCall("Create promocode") { dataSource.create(promocode.toInsertDto()) }

    override suspend fun getPromocodes(
        sortBy: PromocodeSortBy,
        filterByServices: List<String>?,
        paginationRequest: PaginationRequest<PromocodeSortBy>,
        blockedUserIds: Set<UserId>
    ): Result<PaginatedResult<Promocode, PromocodeSortBy>, OperationError> =
        remoteCall("Get promocodes") {
            val page = dataSource.getPromocodes(
                sortBy = sortBy,
                serviceNames = filterByServices,
                limit = paginationRequest.limit,
                cursor = pageCursor(paginationRequest.cursor),
                blockedUserIds = blockedUserIds.map { it.value }.toSet(),
            )
            PaginatedResult(page.items.map { it.toDomain() }, page.nextCursor?.let { PaginationCursor(it, sortBy) })
        }

    override suspend fun getPromocodeById(id: PromocodeId): Result<Promocode, OperationError> =
        remoteCall("Get promocode") { dataSource.getById(id.value) }.flatMap { dto ->
            dto?.let { Result.Success(it.toDomain()) } ?: Result.Error(DatabaseError.NotFound)
        }

    override suspend fun getPromocodesByUser(
        userId: UserId,
        cursor: Any?,
        limit: Int
    ): Result<PaginatedResult<Promocode, PromocodeSortBy>, OperationError> =
        remoteCall("Get user promocodes") {
            val page = dataSource.getByUser(userId.value, limit, pageCursor(cursor))
            PaginatedResult(page.items.map { it.toDomain() }, page.nextCursor?.let { PaginationCursor(it, PromocodeSortBy.NEWEST) })
        }
}

class PostRepositoryImpl(private val dataSource: SupabasePostDataSource) : PostRepository {
    override suspend fun createPost(post: Post): Result<Unit, OperationError> =
        remoteCall("Create post") { dataSource.create(post.toInsertDto()) }

    override suspend fun getPosts(
        limit: Int,
        blockedUserIds: Set<UserId>,
        reportedPostIds: Set<PostId>,
        cursor: Any?
    ): Result<PaginatedResult<Post, PostSortBy>, OperationError> =
        remoteCall("Get posts") {
            val page = dataSource.getPosts(
                limit = limit,
                blockedUserIds = blockedUserIds.map { it.value }.toSet(),
                reportedPostIds = reportedPostIds.map { it.value }.toSet(),
                cursor = pageCursor(cursor),
            )
            PaginatedResult(page.items.map { it.toDomain() }, page.nextCursor?.let { PaginationCursor(it, PostSortBy.NEWEST) })
        }

    override suspend fun getPostById(id: PostId): Result<Post, OperationError> =
        remoteCall("Get post") { dataSource.getById(id.value) }.flatMap { dto ->
            dto?.let { Result.Success(it.toDomain()) } ?: Result.Error(DatabaseError.NotFound)
        }

    override suspend fun getPostsByUser(
        userId: UserId,
        cursor: Any?,
        limit: Int
    ): Result<PaginatedResult<Post, PostSortBy>, OperationError> =
        remoteCall("Get user posts") {
            val page = dataSource.getByUser(userId.value, limit, pageCursor(cursor))
            PaginatedResult(page.items.map { it.toDomain() }, page.nextCursor?.let { PaginationCursor(it, PostSortBy.NEWEST) })
        }
}

class ServiceRepositoryImpl(private val dataSource: SupabaseServiceDataSource) : ServiceRepository {
    private var popularServices: Map<Long, List<Service>> = emptyMap()

    override suspend fun getById(id: ServiceId): Result<Service, OperationError> =
        remoteCall("Get service") { dataSource.getById(id.value) }.flatMap { dto ->
            dto?.let { Result.Success(it.toDomain()) } ?: Result.Error(ServiceError.RetrievalFailure.NotFound)
        }

    override suspend fun findByName(name: String): Result<Service, OperationError> =
        remoteCall("Find service") { dataSource.findByName(name) }.flatMap { dto ->
            dto?.let { Result.Success(it.toDomain()) } ?: Result.Error(ServiceError.RetrievalFailure.NotFound)
        }

    override suspend fun create(service: Service): Result<Service, OperationError> =
        remoteCall("Create service") {
            dataSource.create(service.toInsertDto())
            service
        }

    override suspend fun searchServices(
        query: String,
        limit: Int
    ): Result<List<Service>, OperationError> = remoteCall("Search services") { dataSource.search(query, limit).map { it.toDomain() } }

    /** Popular services change slowly, so one fetch per app run is enough. */
    override suspend fun getPopularServices(limit: Long): Result<List<Service>, OperationError> {
        popularServices[limit]?.let { return Result.Success(it) }
        return remoteCall("Get popular services") {
            dataSource.getPopular(limit).map { it.toDomain() }.also { popularServices = popularServices + (limit to it) }
        }
    }
}

class BannerRepositoryImpl(private val dataSource: SupabaseBannerDataSource) : BannerRepository {
    override suspend fun getBanners(limit: Long): Result<List<Banner>, OperationError> =
        remoteCall("Get banners") { dataSource.getBanners(limit).map { it.toDomain() } }
}

class AppUpdateConfigRepositoryImpl(private val dataSource: SupabaseAppConfigDataSource) : AppUpdateConfigRepository {
    override suspend fun getAppUpdateConfig(): Result<AppUpdateConfig, OperationError> =
        remoteCall("Get app update config") { dataSource.getAppUpdateConfig() }.flatMap { dto ->
            dto?.let { Result.Success(it.toDomain()) } ?: Result.Error(DatabaseError.NotFound)
        }
}
