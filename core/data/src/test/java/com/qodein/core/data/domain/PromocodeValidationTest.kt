package com.qodein.core.data.domain

import com.qodein.core.testing.data.ServiceMother
import com.qodein.core.testing.data.UserMother
import com.qodein.core.testing.util.assertIsType
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.PromocodeError.CreationFailure
import com.qodein.shared.model.Discount
import com.qodein.shared.model.Promocode
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class PromocodeValidationTest {

    private val start = Instant.parse("2026-11-01T00:00:00Z")

    private fun create(
        code: String = "SAVE20",
        discount: Discount = Discount.Percentage(20.0),
        minimumOrderAmount: Double = 5000.0,
        endDate: Instant = start + 30.days,
        description: String? = null,
        imageUrls: List<String> = emptyList()
    ) = Promocode.create(
        code = code,
        service = ServiceMother.netflix(),
        author = UserMother.alice(),
        discount = discount,
        minimumOrderAmount = minimumOrderAmount,
        startDate = start,
        endDate = endDate,
        isVerified = false,
        description = description,
        imageUrls = imageUrls,
    )

    private fun Result<Promocode, CreationFailure>.promocode(): Promocode = assertIsType<Result.Success<Promocode>>().data

    private fun Result<Promocode, CreationFailure>.error(): CreationFailure = assertIsType<Result.Error<CreationFailure>>().error

    @Test
    fun blankCodeIsRejected() {
        assertEquals(CreationFailure.EmptyCode, create(code = "   ").error())
    }

    @Test
    fun codeLongerThanMaxIsRejected() {
        assertEquals(CreationFailure.CodeTooLong, create(code = "A".repeat(Promocode.CODE_MAX_LENGTH + 1)).error())
    }

    @Test
    fun negativeMinimumOrderIsRejected() {
        assertEquals(CreationFailure.InvalidMinimumAmount, create(minimumOrderAmount = -1.0).error())
    }

    @Test
    fun percentageAboveHundredIsRejected() {
        assertEquals(CreationFailure.InvalidPercentageDiscount, create(discount = Discount.Percentage(101.0)).error())
    }

    @Test
    fun negativeFixedAmountIsRejected() {
        assertEquals(CreationFailure.InvalidFixedAmountDiscount, create(discount = Discount.FixedAmount(-5.0)).error())
    }

    @Test
    fun fixedAmountAboveMinimumOrderIsRejected() {
        val error = create(discount = Discount.FixedAmount(6000.0), minimumOrderAmount = 5000.0).error()

        assertEquals(CreationFailure.DiscountExceedsMinimumAmount, error)
    }

    @Test
    fun freeItemDescriptionRules() {
        assertEquals(CreationFailure.InvalidFreeItemDescription, create(discount = Discount.FreeItem("  ")).error())
        assertEquals(
            CreationFailure.FreeItemDescriptionTooLong,
            create(discount = Discount.FreeItem("a".repeat(Discount.FreeItem.MAX_DESCRIPTION_LENGTH + 1))).error(),
        )
        assertEquals(CreationFailure.FreeItemDescriptionInvalidCharacters, create(discount = Discount.FreeItem("Coffee!")).error())
        assertEquals(Discount.FreeItem("Iced coffee"), create(discount = Discount.FreeItem("Iced coffee")).promocode().discount)
    }

    @Test
    fun endDateMustBeAfterStartDate() {
        assertEquals(CreationFailure.InvalidDateRange, create(endDate = start).error())
    }

    @Test
    fun descriptionLongerThanMaxIsRejected() {
        val description = "a".repeat(Promocode.DESCRIPTION_MAX_LENGTH + 1)

        assertEquals(CreationFailure.DescriptionTooLong, create(description = description).error())
    }

    @Test
    fun moreThanMaxImagesIsRejected() {
        val urls = (1..Promocode.MAX_IMAGES + 1).map { "https://cdn.test/$it" }

        assertEquals(CreationFailure.TooManyImages, create(imageUrls = urls).error())
    }

    @Test
    fun idCombinesNormalizedServiceAndCode() {
        val promocode = create(code = "  Save 20  ").promocode()

        assertEquals("Save 20", promocode.code)
        assertEquals("netflix_save_20", promocode.id.value)
    }
}
