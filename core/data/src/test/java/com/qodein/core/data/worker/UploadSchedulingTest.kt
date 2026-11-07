package com.qodein.core.data.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.BackoffPolicy
import androidx.work.NetworkType
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.qodein.core.data.post.WorkManagerPostSubmissionScheduler
import com.qodein.shared.model.UserId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UploadSchedulingTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var workManager: WorkManager

    @Before
    fun setup() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        workManager = WorkManager.getInstance(context)
    }

    private fun schedule(title: String) =
        WorkManagerPostSubmissionScheduler(workManager).schedulePostSubmission(
            authorId = UserId("alice-id"),
            authorName = "Alice",
            authorAvatarUrl = null,
            title = title,
            content = null,
            imageUris = listOf("content://images/1"),
            tags = emptyList(),
        )

    private fun postWorkName(title: String) =
        uploadWorkName("post", "alice-id", title, null, listOf("content://images/1"), emptyList<String>())

    @Test
    fun doubleSubmitEnqueuesOneUpload() {
        schedule("Free coffee")
        schedule("Free coffee")

        val infos = workManager.getWorkInfosForUniqueWork(postWorkName("Free coffee")).get()
        assertEquals(1, infos.size)
    }

    @Test
    fun differentSubmissionsGetDifferentNames() {
        assertNotEquals(postWorkName("Free coffee"), postWorkName("Free tea"))
    }

    @Test
    fun uploadRequiresNetworkAndBacksOffExponentially() {
        val spec = UploadPostWorker.createWorkRequest(
            title = "Free coffee",
            content = null,
            imageUris = emptyList(),
            tags = emptyList(),
            authorId = "alice-id",
            authorUsername = "Alice",
            authorAvatarUrl = null,
        ).workSpec

        assertEquals(NetworkType.CONNECTED, spec.constraints.requiredNetworkType)
        assertEquals(BackoffPolicy.EXPONENTIAL, spec.backoffPolicy)
        assertEquals(30_000L, spec.backoffDelayDuration)
    }
}
