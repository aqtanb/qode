package com.qodein.core.data

import com.qodein.shared.data.dto.PromocodeDto
import com.qodein.shared.data.dto.UserInteractionDto
import com.qodein.shared.data.mapper.toDomain
import com.qodein.shared.data.mapper.toInsertDto
import com.qodein.shared.data.util.PageCursor
import com.qodein.shared.data.util.toPage
import com.qodein.shared.model.Discount
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SupabaseMappingTest {
    private val rowJson =
        """
        {
          "id": "shop_code", "code": "CODE10", "start_date": "2026-11-01T10:00:00.123456+00:00",
          "end_date": "2026-12-01T10:00:00+00:00", "discount_type": "FreeItem", "free_item_description": "Coffee",
          "minimum_order_amount": 5000, "created_at": "2026-11-01T10:00:00+00:00", "vote_score": -2,
          "service_id": "shop", "service_name": "Shop", "author_id": "author", "unknown_column": true
        }
        """.trimIndent()

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun promocodeRowMapsToDomainAndBackToInsert() {
        val promocode = json.decodeFromString<PromocodeDto>(rowJson).toDomain()

        assertEquals(Discount.FreeItem("Coffee"), promocode.discount)
        assertEquals(-2, promocode.voteScore)

        val insert = promocode.toInsertDto()
        assertEquals(PromocodeDto.TYPE_FREE_ITEM, insert.discountType)
        assertNull(insert.discountValue)
        assertEquals("shop", insert.serviceId)
    }

    @Test
    fun unknownInteractionValuesAreDropped() {
        val dto = UserInteractionDto(userId = "user", itemType = "COMMENT", itemId = "item")

        assertNull(dto.toDomain())
    }

    @Test
    fun extraRowMeansAnotherPage() {
        val page = listOf("a", "b", "c").toPage(limit = 2) { PageCursor(it, it) }

        assertEquals(listOf("a", "b"), page.items)
        assertEquals(PageCursor("b", "b"), page.nextCursor)
    }

    @Test
    fun lastPageHasNoCursor() {
        val page = listOf("a", "b").toPage(limit = 2) { PageCursor(it, it) }

        assertNull(page.nextCursor)
    }
}
