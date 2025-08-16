package com.qodein.qode.navigation

import androidx.navigation.NavController
import com.qodein.core.domain.AuthState
import com.qodein.core.testing.data.TestUsers
import com.qodein.feature.auth.navigation.navigateToAuth
import com.qodein.feature.profile.navigation.navigateToProfile
import com.qodein.feature.settings.navigation.navigateToSettings
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [NavigationHandler] following Google's official Android testing style.
 *
 * Test naming convention: subjectUnderTest_actionOrInput_resultState
 * Example: handleNavigation_navigateToProfile_whenAuthenticated_navigatesToProfileScreen
 */
class NavigationHandlerTest {

    @MockK
    private lateinit var navController: NavController

    private lateinit var navigationHandler: NavigationHandler
    private lateinit var navigateToTopLevel: (TopLevelDestination) -> Unit

    private val authenticatedUser = TestUsers.sampleUser

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        navigationHandler = NavigationHandler()
        navigateToTopLevel = mockk(relaxed = true)

        // Mock navigation extension functions
        every { navController.navigateToProfile() } just runs
        every { navController.navigateToAuth() } just runs
        every { navController.navigateToSettings() } just runs
        every { navController.popBackStack() } returns true
    }

    // NavigateToProfile Action Tests

    @Test
    fun handleNavigation_navigateToProfile_whenAuthenticated_navigatesToProfileScreen() {
        // Given
        val authState = AuthState.Authenticated(authenticatedUser)
        val action = NavigationActions.NavigateToProfile

        // When
        navigationHandler.handleNavigation(
            action = action,
            navController = navController,
            authState = authState,
            navigateToTopLevel = navigateToTopLevel,
        )

        // Then
        verify { navController.navigateToProfile() }
        verify(exactly = 0) { navController.navigateToAuth() }
    }

    @Test
    fun handleNavigation_navigateToProfile_whenUnauthenticated_navigatesToAuthScreen() {
        // Given
        val authState = AuthState.Unauthenticated
        val action = NavigationActions.NavigateToProfile

        // When
        navigationHandler.handleNavigation(
            action = action,
            navController = navController,
            authState = authState,
            navigateToTopLevel = navigateToTopLevel,
        )

        // Then
        verify { navController.navigateToAuth() }
        verify(exactly = 0) { navController.navigateToProfile() }
    }

    @Test
    fun handleNavigation_navigateToProfile_whenLoading_doesNotNavigate() {
        // Given
        val authState = AuthState.Loading
        val action = NavigationActions.NavigateToProfile

        // When
        navigationHandler.handleNavigation(
            action = action,
            navController = navController,
            authState = authState,
            navigateToTopLevel = navigateToTopLevel,
        )

        // Then - no navigation calls should be made during loading
        verify(exactly = 0) { navController.navigateToProfile() }
        verify(exactly = 0) { navController.navigateToAuth() }
    }

    // NavigateToTab Action Tests

    @Test
    fun handleNavigation_navigateToTab_home_callsNavigateToTopLevelWithHomeDestination() {
        // Given
        val action = NavigationActions.NavigateToTab(TopLevelDestination.HOME)
        val authState = AuthState.Unauthenticated
        val destinationSlot = slot<TopLevelDestination>()

        // When
        navigationHandler.handleNavigation(
            action = action,
            navController = navController,
            authState = authState,
            navigateToTopLevel = navigateToTopLevel,
        )

        // Then
        verify { navigateToTopLevel(capture(destinationSlot)) }
        assert(destinationSlot.captured == TopLevelDestination.HOME)
    }

    @Test
    fun handleNavigation_navigateToTab_search_callsNavigateToTopLevelWithSearchDestination() {
        // Given
        val action = NavigationActions.NavigateToTab(TopLevelDestination.SEARCH)
        val authState = AuthState.Unauthenticated
        val destinationSlot = slot<TopLevelDestination>()

        // When
        navigationHandler.handleNavigation(
            action = action,
            navController = navController,
            authState = authState,
            navigateToTopLevel = navigateToTopLevel,
        )

        // Then
        verify { navigateToTopLevel(capture(destinationSlot)) }
        assert(destinationSlot.captured == TopLevelDestination.SEARCH)
    }

    @Test
    fun handleNavigation_navigateToTab_inbox_callsNavigateToTopLevelWithInboxDestination() {
        // Given
        val action = NavigationActions.NavigateToTab(TopLevelDestination.INBOX)
        val authState = AuthState.Unauthenticated
        val destinationSlot = slot<TopLevelDestination>()

        // When
        navigationHandler.handleNavigation(
            action = action,
            navController = navController,
            authState = authState,
            navigateToTopLevel = navigateToTopLevel,
        )

        // Then
        verify { navigateToTopLevel(capture(destinationSlot)) }
        assert(destinationSlot.captured == TopLevelDestination.INBOX)
    }

    // NavigateBack Action Tests

    @Test
    fun handleNavigation_navigateBack_whenBackStackExists_callsPopBackStack() {
        // Given
        val action = NavigationActions.NavigateBack
        val authState = AuthState.Unauthenticated
        every { navController.popBackStack() } returns true

        // When
        navigationHandler.handleNavigation(
            action = action,
            navController = navController,
            authState = authState,
            navigateToTopLevel = navigateToTopLevel,
        )

        // Then
        verify { navController.popBackStack() }
        verify(exactly = 0) { navigateToTopLevel(any()) }
    }

    @Test
    fun handleNavigation_navigateBack_whenNoBackStack_navigatesToHome() {
        // Given
        val action = NavigationActions.NavigateBack
        val authState = AuthState.Unauthenticated
        val destinationSlot = slot<TopLevelDestination>()
        every { navController.popBackStack() } returns false

        // When
        navigationHandler.handleNavigation(
            action = action,
            navController = navController,
            authState = authState,
            navigateToTopLevel = navigateToTopLevel,
        )

        // Then
        verify { navController.popBackStack() }
        verify { navigateToTopLevel(capture(destinationSlot)) }
        assert(destinationSlot.captured == TopLevelDestination.HOME)
    }

    // NavigateToHome Action Tests

    @Test
    fun handleNavigation_navigateToHome_callsNavigateToTopLevelWithHomeDestination() {
        // Given
        val action = NavigationActions.NavigateToHome
        val authState = AuthState.Unauthenticated
        val destinationSlot = slot<TopLevelDestination>()

        // When
        navigationHandler.handleNavigation(
            action = action,
            navController = navController,
            authState = authState,
            navigateToTopLevel = navigateToTopLevel,
        )

        // Then
        verify { navigateToTopLevel(capture(destinationSlot)) }
        assert(destinationSlot.captured == TopLevelDestination.HOME)
    }

    // NavigateToFavorites Action Tests (Currently routes to Profile)

    @Test
    fun handleNavigation_navigateToFavorites_whenAuthenticated_routesToProfile() {
        // Given
        val authState = AuthState.Authenticated(authenticatedUser)
        val action = NavigationActions.NavigateToFavorites

        // When
        navigationHandler.handleNavigation(
            action = action,
            navController = navController,
            authState = authState,
            navigateToTopLevel = navigateToTopLevel,
        )

        // Then - Should route to profile since favorites feature doesn't exist yet
        verify { navController.navigateToProfile() }
    }

    @Test
    fun handleNavigation_navigateToFavorites_whenUnauthenticated_routesToAuth() {
        // Given
        val authState = AuthState.Unauthenticated
        val action = NavigationActions.NavigateToFavorites

        // When
        navigationHandler.handleNavigation(
            action = action,
            navController = navController,
            authState = authState,
            navigateToTopLevel = navigateToTopLevel,
        )

        // Then - Should route to auth since favorites feature doesn't exist yet
        verify { navController.navigateToAuth() }
    }

    // NavigateToSettings Action Tests

    @Test
    fun handleNavigation_navigateToSettings_whenAuthenticated_routesToSettings() {
        // Given
        val authState = AuthState.Authenticated(authenticatedUser)
        val action = NavigationActions.NavigateToSettings

        // When
        navigationHandler.handleNavigation(
            action = action,
            navController = navController,
            authState = authState,
            navigateToTopLevel = navigateToTopLevel,
        )

        // Then - Should route to settings since the feature exists
        verify { navController.navigateToSettings() }
    }

    @Test
    fun handleNavigation_navigateToSettings_whenUnauthenticated_routesToSettings() {
        // Given
        val authState = AuthState.Unauthenticated
        val action = NavigationActions.NavigateToSettings

        // When
        navigationHandler.handleNavigation(
            action = action,
            navController = navController,
            authState = authState,
            navigateToTopLevel = navigateToTopLevel,
        )

        // Then - Should route to settings since the feature exists
        verify { navController.navigateToSettings() }
    }

    // getProfileNavigationAction Tests

    @Test
    fun getProfileNavigationAction_whenAuthenticated_returnsNavigateToProfile() {
        // Given
        val authState = AuthState.Authenticated(authenticatedUser)

        // When
        val result = navigationHandler.getProfileNavigationAction(authState)

        // Then
        assert(result == NavigationActions.NavigateToProfile)
    }

    @Test
    fun getProfileNavigationAction_whenUnauthenticated_returnsNavigateToProfile() {
        // Given
        val authState = AuthState.Unauthenticated

        // When
        val result = navigationHandler.getProfileNavigationAction(authState)

        // Then
        // Note: Returns NavigateToProfile in all cases, the routing decision is made in handleNavigation
        assert(result == NavigationActions.NavigateToProfile)
    }

    @Test
    fun getProfileNavigationAction_whenLoading_returnsNavigateToProfile() {
        // Given
        val authState = AuthState.Loading

        // When
        val result = navigationHandler.getProfileNavigationAction(authState)

        // Then
        // Note: Returns NavigateToProfile in all cases, the routing decision is made in handleNavigation
        assert(result == NavigationActions.NavigateToProfile)
    }
}
