package com.qodein.core.testing.util

import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest

/**
 * Extension functions for common test patterns
 */

/**
 * Test a Flow emission with a more concise syntax
 *
 * Usage:
 * ```kotlin
 * viewModel.state.testFlow {
 *     val firstState = awaitItem()
 *     assertTrue(firstState is LoadingState)
 * }
 * ```
 */
suspend fun <T> Flow<T>.testFlow(validate: suspend TurbineTestContext<T>.() -> Unit) {
    test {
        validate()
    }
}

/**
 * Assert that a state is of a specific type with better error messages
 */
inline fun <reified T> Any?.assertIsType(): T =
    when (this) {
        is T -> this
        null -> throw AssertionError("Expected ${T::class.simpleName} but was null")
        else -> throw AssertionError("Expected ${T::class.simpleName} but was ${this::class.simpleName}")
    }

/**
 * Run test with better naming for readability
 */
fun runViewModelTest(testBody: suspend () -> Unit) =
    runTest {
        testBody()
    }
