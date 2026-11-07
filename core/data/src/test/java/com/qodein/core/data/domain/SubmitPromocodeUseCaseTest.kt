package com.qodein.core.data.domain

import com.qodein.core.testing.data.ServiceMother
import com.qodein.core.testing.data.UserMother
import com.qodein.core.testing.fake.FakePromocodeRepository
import com.qodein.core.testing.fake.FakeServiceRepository
import com.qodein.core.testing.fake.FakeStorageRepository
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.DatabaseError
import com.qodein.shared.common.error.PromocodeError
import com.qodein.shared.common.error.ServiceError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.usecase.promocode.SubmitPromocodeRequest
import com.qodein.shared.domain.usecase.promocode.SubmitPromocodeUseCase
import com.qodein.shared.domain.usecase.service.GetOrCreateServiceUseCase
import com.qodein.shared.model.Discount
import com.qodein.shared.model.ServiceRef
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class SubmitPromocodeUseCaseTest {

    private val promocodeRepository = FakePromocodeRepository()
    private val serviceRepository = FakeServiceRepository()
    private val submitPromocode = SubmitPromocodeUseCase(
        promocodeRepository,
        GetOrCreateServiceUseCase(serviceRepository),
        FakeStorageRepository(),
    )
    private val netflix = ServiceMother.netflix()

    @Before
    fun setup() {
        serviceRepository.services += netflix
    }

    private fun request(
        service: ServiceRef = ServiceRef.ById(netflix.id),
        code: String = "SAVE20"
    ): SubmitPromocodeRequest {
        val start = Instant.parse("2026-11-01T00:00:00Z")
        return SubmitPromocodeRequest(
            code = code,
            service = service,
            currentUser = UserMother.alice(),
            discount = Discount.Percentage(20.0),
            minimumOrderAmount = 5000.0,
            startDate = start,
            endDate = start + 30.days,
            description = null,
            imageUris = emptyList(),
            isFirstUserOnly = false,
            isOneTimeUseOnly = false,
            isVerified = false,
        )
    }

    @Test
    fun savesPromocodeAndReturnsItsId() =
        runTest {
            val result = submitPromocode(request())

            assertEquals(Result.Success("netflix_save20"), result)
            assertEquals(netflix.id, promocodeRepository.promocodes.values.single().serviceId)
        }

    @Test
    fun sameCodeForSameServiceIsDuplicate() =
        runTest {
            submitPromocode(request(code = "SAVE20"))

            val result = submitPromocode(request(code = "save20"))

            assertEquals(Result.Error(PromocodeError.SubmissionFailure.DuplicateCode), result)
            assertEquals(1, promocodeRepository.promocodes.size)
        }

    @Test
    fun failedDuplicateCheckIsReturnedInsteadOfSaving() =
        runTest {
            promocodeRepository.lookupError = SystemError.Offline

            assertEquals(Result.Error(SystemError.Offline), submitPromocode(request()))
            assertTrue(promocodeRepository.promocodes.isEmpty())
        }

    @Test
    fun unknownServiceByNameIsCreated() =
        runTest {
            val result = submitPromocode(request(service = ServiceRef.ByName("Kaspi Shop", "kaspi.kz")))

            assertEquals(Result.Success("kaspi_shop_save20"), result)
            assertTrue(serviceRepository.services.any { it.name == "Kaspi Shop" })
        }

    @Test
    fun invalidNewServiceStopsSubmission() =
        runTest {
            val result = submitPromocode(request(service = ServiceRef.ByName("", "kaspi.kz")))

            assertEquals(Result.Error(ServiceError.CreationFailure.EmptyName), result)
            assertTrue(promocodeRepository.promocodes.isEmpty())
        }

    @Test
    fun missingServiceByIdIsReturned() =
        runTest {
            serviceRepository.services.clear()

            assertEquals(Result.Error(DatabaseError.NotFound), submitPromocode(request()))
        }
}
