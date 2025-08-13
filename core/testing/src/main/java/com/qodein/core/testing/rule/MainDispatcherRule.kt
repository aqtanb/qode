package com.qodein.core.testing.rule

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit TestRule that swaps the background executor used by the Architecture Components
 * with a different one which executes each task synchronously.
 *
 * This rule automatically sets up and tears down the test dispatcher for coroutine testing.
 *
 * Usage:
 * ```kotlin
 * @OptIn(ExperimentalCoroutinesApi::class)
 * class MyViewModelTest {
 *     @get:Rule
 *     val mainDispatcherRule = MainDispatcherRule()
 *
 *     @Test
 *     fun myTest() = runTest {
 *         // Test implementation
 *     }
 * }
 * ```
 *
 * Based on Now in Android (nowinandroid) testing patterns:
 * https://github.com/android/nowinandroid/blob/main/core/testing/src/main/kotlin/com/google/samples/apps/nowinandroid/core/testing/util/MainDispatcherRule.kt
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
