package com.qodein.core.data.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.qodein.core.notifications.UploadContentType
import com.qodein.core.testing.data.ServiceMother
import com.qodein.core.testing.data.UserMother
import com.qodein.core.testing.fake.FakeNotifier
import com.qodein.core.testing.fake.FakePromocodeRepository
import com.qodein.core.testing.fake.FakeServiceRepository
import com.qodein.core.testing.fake.FakeStorageRepository
import com.qodein.core.testing.fake.FakeUserRepository
import com.qodein.shared.common.error.DatabaseError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.usecase.promocode.SubmitPromocodeUseCase
import com.qodein.shared.domain.usecase.service.GetOrCreateServiceUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.model.Discount
import com.qodein.shared.model.ServiceRef
import com.qodein.shared.model.UserId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
class UploadPromocodeWorkerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val promocodeRepository = FakePromocodeRepository()
    private val serviceRepository = FakeServiceRepository()
    private val storageRepository = FakeStorageRepository()
    private val userRepository = FakeUserRepository()
    private val notifier = FakeNotifier()
    private val netflix = ServiceMother.netflix()
    private val alice = UserMother.alice()

    private val factory = object : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ) = UploadPromocodeWorker(
            appContext,
            workerParameters,
            SubmitPromocodeUseCase(promocodeRepository, GetOrCreateServiceUseCase(serviceRepository), storageRepository),
            GetUserByIdUseCase(userRepository),
            notifier,
        )
    }

    @Before
    fun setup() {
        serviceRepository.services += netflix
        userRepository.users[alice.id.value] = alice
    }

    private fun worker(
        discount: Discount = Discount.Percentage(20.0),
        runAttemptCount: Int = 0
    ): UploadPromocodeWorker {
        val start = Instant.parse("2026-11-01T00:00:00Z")
        val input = UploadPromocodeWorker.createWorkRequest(
            code = "SAVE20",
            service = ServiceRef.ById(netflix.id),
            userId = UserId(alice.id.value),
            discount = discount,
            minimumOrderAmount = 5000.0,
            startDate = start,
            endDate = start + 30.days,
            description = null,
            imageUris = emptyList(),
            isFirstUserOnly = false,
            isOneTimeUseOnly = false,
            isVerified = false,
        ).workSpec.input
        return TestListenableWorkerBuilder<UploadPromocodeWorker>(context)
            .setWorkerFactory(factory)
            .setInputData(input)
            .setRunAttemptCount(runAttemptCount)
            .build()
    }

    @Test
    fun createsPromocodeAndNotifiesSuccess() =
        runTest {
            val result = worker().doWork()

            assertEquals(ListenableWorker.Result.success(), result)
            val promocode = promocodeRepository.promocodes.values.single()
            assertEquals("SAVE20", promocode.code)
            assertEquals(Discount.Percentage(20.0), promocode.discount)
            assertEquals(UploadContentType.PROMOCODE, notifier.successes.single().contentType)
        }

    @Test
    fun offlineUserLookupRetries() =
        runTest {
            userRepository.getUserError = SystemError.Offline

            assertEquals(ListenableWorker.Result.retry(), worker().doWork())
            assertTrue(notifier.errors.isEmpty())
        }

    @Test
    fun timeoutOnCreateFailsOnLastAttempt() =
        runTest {
            promocodeRepository.createError = DatabaseError.DeadlineExceeded

            assertEquals(ListenableWorker.Result.retry(), worker(runAttemptCount = 0).doWork())
            assertEquals(
                ListenableWorker.Result.failure(),
                worker(runAttemptCount = UploadFailurePolicy.MAX_ATTEMPTS - 1).doWork(),
            )
            assertEquals(1, notifier.errors.size)
        }

    @Test
    fun deletedUserFailsImmediately() =
        runTest {
            userRepository.users.clear()

            assertEquals(ListenableWorker.Result.failure(), worker().doWork())
            assertEquals(1, notifier.errors.size)
        }

    @Test
    fun duplicateCodeFailsImmediately() =
        runTest {
            worker().doWork()

            assertEquals(ListenableWorker.Result.failure(), worker().doWork())
            assertEquals(1, notifier.errors.size)
        }

    @Test
    fun invalidDiscountFailsImmediately() =
        runTest {
            assertEquals(ListenableWorker.Result.failure(), worker(discount = Discount.Percentage(150.0)).doWork())
            assertTrue(promocodeRepository.promocodes.isEmpty())
        }
}
