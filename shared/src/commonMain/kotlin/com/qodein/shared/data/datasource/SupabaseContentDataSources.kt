package com.qodein.shared.data.datasource

import com.qodein.shared.data.dto.AppUpdateConfigDto
import com.qodein.shared.data.dto.BannerDto
import com.qodein.shared.data.dto.PostDto
import com.qodein.shared.data.dto.PostInsertDto
import com.qodein.shared.data.dto.PromocodeDto
import com.qodein.shared.data.dto.PromocodeInsertDto
import com.qodein.shared.data.dto.ReportInsertDto
import com.qodein.shared.data.dto.ServiceDto
import com.qodein.shared.data.dto.ServiceInsertDto
import com.qodein.shared.data.util.Page
import com.qodein.shared.data.util.PageCursor
import com.qodein.shared.data.util.notIn
import com.qodein.shared.data.util.startAfter
import com.qodein.shared.data.util.toPage
import com.qodein.shared.model.PromocodeSortBy
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Clock
import kotlin.time.Instant

private const val COLUMN_ID = "id"
private const val COLUMN_CREATED_AT = "created_at"
private const val COLUMN_AUTHOR_ID = "author_id"

private fun normalized(timestamp: String): String = Instant.parse(timestamp).toString()

class SupabasePromocodeDataSource(private val postgrest: Postgrest) {
    suspend fun create(dto: PromocodeInsertDto) {
        postgrest.from(PromocodeDto.TABLE_NAME).insert(dto)
    }

    suspend fun getPromocodes(
        sortBy: PromocodeSortBy,
        serviceNames: List<String>?,
        limit: Int,
        cursor: PageCursor?,
        blockedUserIds: Set<String>
    ): Page<PromocodeDto> {
        val sort = sortOf(sortBy)
        val now = Clock.System.now().toString()
        return postgrest.from(PromocodeDto.VIEW_NAME).select {
            filter {
                gte("end_date", now)
                notIn(COLUMN_AUTHOR_ID, blockedUserIds)
                serviceNames?.takeIf { it.isNotEmpty() }?.let { isIn("service_name", it) }
                cursor?.let { startAfter(it, sort.column, sort.ascending) }
            }
            order(sort.column, if (sort.ascending) Order.ASCENDING else Order.DESCENDING)
            order(COLUMN_ID, Order.ASCENDING)
            limit(limit + 1L)
        }.decodeList<PromocodeDto>().toPage(limit) { PageCursor(sort.valueOf(it), it.id) }
    }

    suspend fun getById(id: String): PromocodeDto? =
        postgrest.from(PromocodeDto.VIEW_NAME).select { filter { eq(COLUMN_ID, id) } }.decodeSingleOrNull()

    suspend fun getByUser(
        userId: String,
        limit: Int,
        cursor: PageCursor?
    ): Page<PromocodeDto> =
        postgrest.from(PromocodeDto.VIEW_NAME).select {
            filter {
                eq(COLUMN_AUTHOR_ID, userId)
                cursor?.let { startAfter(it, COLUMN_CREATED_AT, ascending = false) }
            }
            order(COLUMN_CREATED_AT, Order.DESCENDING)
            order(COLUMN_ID, Order.ASCENDING)
            limit(limit + 1L)
        }.decodeList<PromocodeDto>().toPage(limit) { PageCursor(normalized(it.createdAt), it.id) }

    private data class Sort(val column: String, val ascending: Boolean, val valueOf: (PromocodeDto) -> String)

    private fun sortOf(sortBy: PromocodeSortBy): Sort =
        when (sortBy) {
            PromocodeSortBy.POPULARITY -> Sort("vote_score", ascending = false) { it.voteScore.toString() }
            PromocodeSortBy.NEWEST -> Sort(COLUMN_CREATED_AT, ascending = false) { normalized(it.createdAt) }
            PromocodeSortBy.EXPIRING_SOON -> Sort("end_date", ascending = true) { normalized(it.endDate) }
        }
}

class SupabasePostDataSource(private val postgrest: Postgrest) {
    suspend fun create(dto: PostInsertDto) {
        postgrest.from(PostDto.TABLE_NAME).insert(dto)
    }

    suspend fun getPosts(
        limit: Int,
        blockedUserIds: Set<String>,
        reportedPostIds: Set<String>,
        cursor: PageCursor?
    ): Page<PostDto> =
        postgrest.from(PostDto.VIEW_NAME).select {
            filter {
                notIn(COLUMN_AUTHOR_ID, blockedUserIds)
                notIn(COLUMN_ID, reportedPostIds)
                cursor?.let { startAfter(it, COLUMN_CREATED_AT, ascending = false) }
            }
            order(COLUMN_CREATED_AT, Order.DESCENDING)
            order(COLUMN_ID, Order.ASCENDING)
            limit(limit + 1L)
        }.decodeList<PostDto>().toPage(limit) { PageCursor(normalized(it.createdAt), it.id) }

    suspend fun getById(id: String): PostDto? =
        postgrest.from(PostDto.VIEW_NAME).select { filter { eq(COLUMN_ID, id) } }.decodeSingleOrNull()

    suspend fun getByUser(
        userId: String,
        limit: Int,
        cursor: PageCursor?
    ): Page<PostDto> =
        postgrest.from(PostDto.VIEW_NAME).select {
            filter {
                eq(COLUMN_AUTHOR_ID, userId)
                cursor?.let { startAfter(it, COLUMN_CREATED_AT, ascending = false) }
            }
            order(COLUMN_CREATED_AT, Order.DESCENDING)
            order(COLUMN_ID, Order.ASCENDING)
            limit(limit + 1L)
        }.decodeList<PostDto>().toPage(limit) { PageCursor(normalized(it.createdAt), it.id) }
}

class SupabaseServiceDataSource(private val postgrest: Postgrest) {
    suspend fun getById(id: String): ServiceDto? =
        postgrest.from(ServiceDto.TABLE_NAME).select { filter { eq(COLUMN_ID, id) } }.decodeSingleOrNull()

    suspend fun findByName(name: String): ServiceDto? =
        postgrest.from(ServiceDto.TABLE_NAME).select {
            filter { eq("name", name) }
            limit(1)
        }.decodeList<ServiceDto>().firstOrNull()

    suspend fun create(dto: ServiceInsertDto) {
        postgrest.from(ServiceDto.TABLE_NAME).insert(dto)
    }

    suspend fun search(
        query: String,
        limit: Int
    ): List<ServiceDto> =
        postgrest.rpc(
            function = "search_services",
            parameters = buildJsonObject {
                put("search_query", query)
                put("result_limit", limit)
            },
        ).decodeList()

    suspend fun getPopular(limit: Long): List<ServiceDto> =
        postgrest.from(ServiceDto.TABLE_NAME).select {
            order("promocode_count", Order.DESCENDING)
            order("name", Order.ASCENDING)
            limit(limit)
        }.decodeList()
}

class SupabaseBannerDataSource(private val postgrest: Postgrest) {
    suspend fun getBanners(limit: Long): List<BannerDto> =
        postgrest.from(BannerDto.TABLE_NAME).select {
            filter {
                eq("is_active", true)
                gt("expires_at", Clock.System.now().toString())
            }
            order("priority", Order.DESCENDING)
            limit(limit)
        }.decodeList()
}

class SupabaseAppConfigDataSource(private val postgrest: Postgrest) {
    suspend fun getAppUpdateConfig(): AppUpdateConfigDto? =
        postgrest.from(AppUpdateConfigDto.TABLE_NAME).select {
            filter { eq(COLUMN_ID, AppUpdateConfigDto.UPDATE_POLICY_ID) }
        }.decodeSingleOrNull()
}

class SupabaseReportDataSource(private val postgrest: Postgrest) {
    suspend fun submit(dto: ReportInsertDto) {
        postgrest.from(ReportInsertDto.TABLE_NAME).insert(dto)
    }
}
