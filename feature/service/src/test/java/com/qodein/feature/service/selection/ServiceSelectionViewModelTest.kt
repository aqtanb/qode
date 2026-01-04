package com.qodein.feature.service.selection

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.qodein.core.testing.data.ServiceMother
import com.qodein.core.testing.rule.MainDispatcherRule
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.usecase.service.GetPopularServicesUseCase
import com.qodein.shared.domain.usecase.service.SearchServicesUseCase
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ServiceSelectionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getPopularServicesUseCase: GetPopularServicesUseCase
    private lateinit var searchServicesUseCase: SearchServicesUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: ServiceSelectionViewModel

    private val testServices = listOf(
        ServiceMother.netflix(),
        ServiceMother.spotify(),
        ServiceMother.amazon(),
    )

    @Before
    fun setup() {
        getPopularServicesUseCase = mockk(relaxed = true)
        searchServicesUseCase = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle()

        // Default mock for popular services - tests can override if needed
        coEvery { getPopularServicesUseCase() } returns Result.Success(testServices)
    }

    @Test
    fun `initial state is correct`() =
        runTest {
            // Given
            coEvery { getPopularServicesUseCase() } returns Result.Success(testServices)

            // When
            viewModel = ServiceSelectionViewModel(savedStateHandle, getPopularServicesUseCase, searchServicesUseCase)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertEquals("", state.searchText)
            assertEquals(SearchUiState.Idle, state.searchStatus)
            assertTrue(state.popularStatus is PopularStatus.Success)
            assertEquals(SelectionMode.Multi, state.selectionMode)
            assertTrue(state.selectedServiceIds.isEmpty())
        }

    @Test
    fun `initialize with single selection mode sets correct state`() =
        runTest {
            // Given
            val resultFlow = flowOf<Result<List<Service>, SystemError>>(Result.Success(testServices))
            coEvery { searchServicesUseCase(any()) } returns resultFlow
            viewModel = ServiceSelectionViewModel(savedStateHandle, getPopularServicesUseCase, searchServicesUseCase)

            val initialServiceIds = setOf(ServiceId("netflix"))

            // When
            viewModel.initialize(initialServiceIds, isSingleSelection = true)

            // Then
            val state = viewModel.uiState.value
            assertEquals(SelectionMode.Single, state.selectionMode)
            assertEquals(initialServiceIds, state.selectedServiceIds)
        }

    @Test
    fun `initialize with multi selection mode sets correct state`() =
        runTest {
            // Given
            val resultFlow = flowOf<Result<List<Service>, SystemError>>(Result.Success(testServices))
            coEvery { searchServicesUseCase(any()) } returns resultFlow
            viewModel = ServiceSelectionViewModel(savedStateHandle, getPopularServicesUseCase, searchServicesUseCase)

            val initialServiceIds = setOf(ServiceId("netflix"), ServiceId("spotify"))

            // When
            viewModel.initialize(initialServiceIds, isSingleSelection = false)

            // Then
            val state = viewModel.uiState.value
            assertEquals(SelectionMode.Multi, state.selectionMode)
            assertEquals(initialServiceIds, state.selectedServiceIds)
        }

    @Test
    fun `initialize only works once`() =
        runTest {
            // Given
            val resultFlow = flowOf<Result<List<Service>, SystemError>>(Result.Success(testServices))
            coEvery { searchServicesUseCase(any()) } returns resultFlow
            viewModel = ServiceSelectionViewModel(savedStateHandle, getPopularServicesUseCase, searchServicesUseCase)

            val initialServiceIds = setOf(ServiceId("netflix"))

            // When
            viewModel.initialize(initialServiceIds, isSingleSelection = true)
            viewModel.initialize(setOf(ServiceId("spotify")), isSingleSelection = false)

            // Then - should still have the first initialization
            val state = viewModel.uiState.value
            assertEquals(SelectionMode.Single, state.selectionMode)
            assertEquals(initialServiceIds, state.selectedServiceIds)
        }

    @Test
    fun `updating search query triggers search with loading state`() =
        runTest {
            // Given
            val searchResults = listOf(testServices[0]) // Netflix only
            val resultFlow = MutableStateFlow<Result<List<Service>, SystemError>>(Result.Success(testServices))
            coEvery { searchServicesUseCase(any()) } returns resultFlow

            viewModel = ServiceSelectionViewModel(savedStateHandle, getPopularServicesUseCase, searchServicesUseCase)
            advanceUntilIdle()

            // When
            viewModel.onAction(ServiceSelectionAction.UpdateQuery("Net"))

            // Then - search should be loading initially
            val stateAfterUpdate = viewModel.uiState.value
            assertEquals("Net", stateAfterUpdate.searchText)
            assertEquals(SearchUiState.Loading, stateAfterUpdate.searchStatus)
        }

    @Test
    fun `successful search updates searchStatus`() =
        runTest {
            // Given
            val searchResults = listOf(testServices[0])
            val resultFlow = MutableStateFlow<Result<List<Service>, SystemError>>(Result.Success(testServices))
            coEvery { searchServicesUseCase(any()) } returns resultFlow

            viewModel = ServiceSelectionViewModel(savedStateHandle, getPopularServicesUseCase, searchServicesUseCase)
            advanceUntilIdle()

            // When
            viewModel.onAction(ServiceSelectionAction.UpdateQuery("Netflix"))
            resultFlow.value = Result.Success(searchResults)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertEquals("Netflix", state.searchText)
            assertTrue(state.searchStatus is SearchUiState.Success)
            assertEquals(searchResults, (state.searchStatus as SearchUiState.Success).services)
        }

    @Test
    fun `failed search updates searchStatus with error`() =
        runTest {
            // Given
            val error = SystemError.Unknown
            val resultFlow = MutableStateFlow<Result<List<Service>, SystemError>>(Result.Success(testServices))
            coEvery { searchServicesUseCase(any()) } returns resultFlow

            viewModel = ServiceSelectionViewModel(savedStateHandle, getPopularServicesUseCase, searchServicesUseCase)
            advanceUntilIdle()

            // When
            viewModel.onAction(ServiceSelectionAction.UpdateQuery("Netflix"))
            resultFlow.value = Result.Error(error)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state.searchStatus is SearchUiState.Error)
            assertEquals(error, (state.searchStatus as SearchUiState.Error).error)
        }

    @Test
    fun `clearing search query loads popular services`() =
        runTest {
            // Given
            val resultFlow = flowOf<Result<List<Service>, SystemError>>(Result.Success(testServices))
            coEvery { searchServicesUseCase(any()) } returns resultFlow

            viewModel = ServiceSelectionViewModel(savedStateHandle, getPopularServicesUseCase, searchServicesUseCase)
            advanceUntilIdle()

            // Popular services should be loaded on init
            val initialState = viewModel.uiState.value
            assertTrue(initialState.popularStatus is PopularStatus.Success)

            // When - do a search
            viewModel.onAction(ServiceSelectionAction.UpdateQuery("Netflix"))
            advanceUntilIdle()

            // Then - clear the search
            viewModel.onAction(ServiceSelectionAction.UpdateQuery(""))
            advanceUntilIdle()

            // Then - should show popular services (already loaded, not reloaded)
            val state = viewModel.uiState.value
            assertEquals("", state.searchText)
            assertEquals(SearchUiState.Idle, state.searchStatus)
            assertTrue(state.popularStatus is PopularStatus.Success)
            // Popular services should still be the same (not reloaded)
            assertEquals(testServices, (state.popularStatus as PopularStatus.Success).services)
        }

    @Test
    fun `toggle service in multi selection mode adds service`() =
        runTest {
            // Given
            val resultFlow = flowOf<Result<List<Service>, SystemError>>(Result.Success(testServices))
            coEvery { searchServicesUseCase(any()) } returns resultFlow

            viewModel = ServiceSelectionViewModel(savedStateHandle, getPopularServicesUseCase, searchServicesUseCase)
            viewModel.initialize(emptySet(), isSingleSelection = false)
            advanceUntilIdle()

            // When
            viewModel.onAction(ServiceSelectionAction.ToggleService(ServiceId("netflix")))

            // Then
            val state = viewModel.uiState.value
            assertTrue(state.selectedServiceIds.contains(ServiceId("netflix")))
            assertEquals(1, state.selectedServiceIds.size)
        }

    @Test
    fun `toggle service in multi selection mode removes service if already selected`() =
        runTest {
            // Given
            val resultFlow = flowOf<Result<List<Service>, SystemError>>(Result.Success(testServices))
            coEvery { searchServicesUseCase(any()) } returns resultFlow

            viewModel = ServiceSelectionViewModel(savedStateHandle, getPopularServicesUseCase, searchServicesUseCase)
            viewModel.initialize(setOf(ServiceId("netflix")), isSingleSelection = false)
            advanceUntilIdle()

            // When
            viewModel.onAction(ServiceSelectionAction.ToggleService(ServiceId("netflix")))

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.selectedServiceIds.contains(ServiceId("netflix")))
            assertTrue(state.selectedServiceIds.isEmpty())
        }

    @Test
    fun `toggle multiple services in multi selection mode`() =
        runTest {
            // Given
            val resultFlow = flowOf<Result<List<Service>, SystemError>>(Result.Success(testServices))
            coEvery { searchServicesUseCase(any()) } returns resultFlow

            viewModel = ServiceSelectionViewModel(savedStateHandle, getPopularServicesUseCase, searchServicesUseCase)
            viewModel.initialize(emptySet(), isSingleSelection = false)
            advanceUntilIdle()

            // When
            viewModel.onAction(ServiceSelectionAction.ToggleService(ServiceId("netflix")))
            viewModel.onAction(ServiceSelectionAction.ToggleService(ServiceId("spotify")))

            // Then
            val state = viewModel.uiState.value
            assertEquals(2, state.selectedServiceIds.size)
            assertTrue(state.selectedServiceIds.contains(ServiceId("netflix")))
            assertTrue(state.selectedServiceIds.contains(ServiceId("spotify")))
        }

    @Test
    fun `toggle service in single selection mode replaces previous selection`() =
        runTest {
            // Given
            val resultFlow = flowOf<Result<List<Service>, SystemError>>(Result.Success(testServices))
            coEvery { searchServicesUseCase(any()) } returns resultFlow

            viewModel = ServiceSelectionViewModel(savedStateHandle, getPopularServicesUseCase, searchServicesUseCase)
            viewModel.initialize(setOf(ServiceId("netflix")), isSingleSelection = true)
            advanceUntilIdle()

            // When
            viewModel.onAction(ServiceSelectionAction.ToggleService(ServiceId("spotify")))

            // Then
            val state = viewModel.uiState.value
            assertEquals(1, state.selectedServiceIds.size)
            assertTrue(state.selectedServiceIds.contains(ServiceId("spotify")))
            assertFalse(state.selectedServiceIds.contains(ServiceId("netflix")))
        }

    @Test
    fun `toggle service in single selection mode emits selection event`() =
        runTest {
            // Given
            val resultFlow = flowOf<Result<List<Service>, SystemError>>(Result.Success(testServices))
            coEvery { searchServicesUseCase(any()) } returns resultFlow

            viewModel = ServiceSelectionViewModel(savedStateHandle, getPopularServicesUseCase, searchServicesUseCase)
            viewModel.initialize(emptySet(), isSingleSelection = true)
            advanceUntilIdle()

            // When & Then
            viewModel.events.test {
                viewModel.onAction(ServiceSelectionAction.ToggleService(ServiceId("netflix")))

                val event = awaitItem()
                assertTrue(event is ServiceSelectionEvent.ServiceSelected)
                assertEquals(setOf(ServiceId("netflix")), (event as ServiceSelectionEvent.ServiceSelected).selectedServiceIds)
            }
        }

    @Test
    fun `toggle service in multi selection mode does not emit event immediately`() =
        runTest {
            // Given
            val resultFlow = flowOf<Result<List<Service>, SystemError>>(Result.Success(testServices))
            coEvery { searchServicesUseCase(any()) } returns resultFlow

            viewModel = ServiceSelectionViewModel(savedStateHandle, getPopularServicesUseCase, searchServicesUseCase)
            viewModel.initialize(emptySet(), isSingleSelection = false)
            advanceUntilIdle()

            // When & Then
            viewModel.events.test {
                viewModel.onAction(ServiceSelectionAction.ToggleService(ServiceId("netflix")))

                // Should not emit event for multi-selection
                expectNoEvents()
            }
        }

    @Test
    fun `retry load services triggers search again`() =
        runTest {
            // Given - initial load fails
            coEvery { getPopularServicesUseCase() } returns Result.Error(SystemError.Unknown)

            viewModel = ServiceSelectionViewModel(savedStateHandle, getPopularServicesUseCase, searchServicesUseCase)
            advanceUntilIdle()

            // Verify error state
            assertTrue(viewModel.uiState.value.popularStatus is PopularStatus.Error)

            // When - fix the data source and retry
            coEvery { getPopularServicesUseCase() } returns Result.Success(testServices)
            viewModel.onAction(ServiceSelectionAction.RetryLoadServices)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state.popularStatus is PopularStatus.Success)
            assertEquals(testServices, (state.popularStatus as PopularStatus.Success).services)
        }

    @Test
    fun `retry load services reloads popular when not searching`() =
        runTest {
            // Given - initial load fails
            coEvery { getPopularServicesUseCase() } returns Result.Error(SystemError.Unknown)

            viewModel = ServiceSelectionViewModel(savedStateHandle, getPopularServicesUseCase, searchServicesUseCase)
            advanceUntilIdle()

            // Verify error state
            assertTrue(viewModel.uiState.value.popularStatus is PopularStatus.Error)

            // When - fix the data source and retry
            coEvery { getPopularServicesUseCase() } returns Result.Success(testServices)
            viewModel.onAction(ServiceSelectionAction.RetryLoadServices)
            advanceUntilIdle()

            // Then - should load successfully
            val state = viewModel.uiState.value
            assertTrue(state.popularStatus is PopularStatus.Success)
            assertEquals(testServices, (state.popularStatus as PopularStatus.Success).services)
        }

    @Test
    fun `retry load services retriggers search when searching`() =
        runTest {
            // Given
            val resultFlow = MutableStateFlow<Result<List<Service>, SystemError>>(Result.Success(testServices))
            coEvery { searchServicesUseCase(any()) } returns resultFlow

            viewModel = ServiceSelectionViewModel(savedStateHandle, getPopularServicesUseCase, searchServicesUseCase)
            advanceUntilIdle()

            // User searches but gets error
            viewModel.onAction(ServiceSelectionAction.UpdateQuery("Netflix"))
            resultFlow.value = Result.Error(SystemError.Unknown)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.searchStatus is SearchUiState.Error)

            // When - fix data and retry
            resultFlow.value = Result.Success(listOf(testServices[0]))
            viewModel.onAction(ServiceSelectionAction.RetryLoadServices)
            advanceUntilIdle()

            // Then - should reload search results
            val state = viewModel.uiState.value
            assertTrue(state.searchStatus is SearchUiState.Success)
            assertEquals(listOf(testServices[0]), (state.searchStatus as SearchUiState.Success).services)
        }

    @Test
    fun `popular services show error state when loading fails`() =
        runTest {
            // Given
            val error = SystemError.Unknown
            coEvery { getPopularServicesUseCase() } returns Result.Error(error)

            // When
            viewModel = ServiceSelectionViewModel(savedStateHandle, getPopularServicesUseCase, searchServicesUseCase)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state.popularStatus is PopularStatus.Error)
            assertEquals(error, (state.popularStatus as PopularStatus.Error).error)
        }
}
