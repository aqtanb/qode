package com.qodein.feature.promocode.navigation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.qodein.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Tests for the SavedStateHandle re-selection bug.
 *
 * BUG: When using SavedStateHandle.getStateFlow(), setting the SAME value twice
 * does NOT emit the second time, causing the UI to miss the update.
 *
 * This reproduces the bug where:
 * 1. User selects service BTV
 * 2. User switches to manual entry (clears service in ViewModel, but NOT in SavedStateHandle)
 * 3. User tries to select BTV again
 * 4. SavedStateHandle.set([BTV]) is called but StateFlow doesn't emit (same value)
 * 5. UI never receives the selection
 *
 * FIX: Clear SavedStateHandle to null after processing, so next selection triggers emission.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SavedStateHandleReselectionTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `REGRESSION - StateFlow does not emit when setting same value twice`() =
        runTest {
            // Given
            val savedStateHandle = SavedStateHandle()
            val key = "test_key"

            // When - collect emissions from StateFlow
            savedStateHandle.getStateFlow<List<String>?>(key, null)
                .filterNotNull()
                .test {
                    // First selection: BTV
                    savedStateHandle.set(key, listOf("BTV"))
                    assertEquals(listOf("BTV"), awaitItem())

                    // User switches to manual entry (clears in ViewModel, but NOT in SavedStateHandle)
                    // SavedStateHandle still has ["BTV"]

                    // Second selection: BTV again
                    savedStateHandle.set(key, listOf("BTV"))

                    // Then - StateFlow does NOT emit because value didn't change
                    expectNoEvents()
                    // ^ This is the BUG! UI never receives the second selection
                }
        }

    @Test
    fun `REGRESSION FIX - Clearing SavedStateHandle allows re-selecting same value`() =
        runTest {
            // Given
            val savedStateHandle = SavedStateHandle()
            val key = "test_key"

            // When - collect emissions from StateFlow
            savedStateHandle.getStateFlow<List<String>?>(key, null)
                .filterNotNull()
                .test {
                    // First selection: BTV
                    savedStateHandle.set(key, listOf("BTV"))
                    assertEquals(listOf("BTV"), awaitItem())

                    // FIX: Clear SavedStateHandle after processing
                    savedStateHandle.set<List<String>?>(key, null)

                    // Second selection: BTV again
                    savedStateHandle.set(key, listOf("BTV"))

                    // Then - StateFlow DOES emit because value changed from null → ["BTV"]
                    assertEquals(listOf("BTV"), awaitItem())
                    // ^ The fix works! UI receives the second selection
                }
        }

    @Test
    fun `StateFlow emits when value actually changes`() =
        runTest {
            // Given
            val savedStateHandle = SavedStateHandle()
            val key = "test_key"

            // When - collect emissions from StateFlow
            savedStateHandle.getStateFlow<List<String>?>(key, null)
                .filterNotNull()
                .test {
                    // Selection 1: BTV
                    savedStateHandle.set(key, listOf("BTV"))
                    assertEquals(listOf("BTV"), awaitItem())

                    // Selection 2: Different service (Halyk)
                    savedStateHandle.set(key, listOf("Halyk"))
                    assertEquals(listOf("Halyk"), awaitItem())

                    // Selection 3: BTV again (different from previous)
                    savedStateHandle.set(key, listOf("BTV"))
                    assertEquals(listOf("BTV"), awaitItem())

                    // Then - All emissions work because values are different
                }
        }
}
