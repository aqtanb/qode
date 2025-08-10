package com.qodein.feature.profile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.model.Email
import com.qodein.core.model.User
import com.qodein.core.model.UserId
import com.qodein.core.model.UserPreferences
import com.qodein.core.model.UserProfile
import com.qodein.core.model.UserStats
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testUser = User(
        id = UserId("test_user_id"),
        email = Email("john.doe@example.com"),
        profile = UserProfile(
            username = "johndoe",
            firstName = "John",
            lastName = "Doe",
            bio = "Test user",
            photoUrl = "https://example.com/profile.jpg",
            birthday = null,
            gender = null,
        ),
        stats = UserStats.initial(UserId("test_user_id")),
        preferences = UserPreferences.default(UserId("test_user_id")),
    )

    @Test
    fun profileScreen_whenLoadingState_showsLoadingIndicator() {
        // Given
        val state = ProfileUiState.Loading

        // When
        composeTestRule.setContent {
            QodeTheme {
                ProfileContent(
                    state = state,
                    onAction = { },
                    onBackClick = { },
                    onSignOut = { },
                    onAchievementsClick = { },
                    onUserJourneyClick = { },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithTag("profile_loading").assertIsDisplayed()
    }

    @Test
    fun profileScreen_whenSuccessState_displaysUserInformation() {
        // Given
        val state = ProfileUiState.Success(user = testUser)

        // When
        composeTestRule.setContent {
            QodeTheme {
                ProfileContent(
                    state = state,
                    onAction = { },
                    onBackClick = { },
                    onSignOut = { },
                    onAchievementsClick = { },
                    onUserJourneyClick = { },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("john.doe@example.com").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Profile picture").assertIsDisplayed()
    }

    @Test
    fun profileScreen_whenSuccessState_displaysStatsSection() {
        // Given
        val state = ProfileUiState.Success(user = testUser)

        // When
        composeTestRule.setContent {
            QodeTheme {
                ProfileContent(
                    state = state,
                    onAction = { },
                    onBackClick = { },
                    onSignOut = { },
                    onAchievementsClick = { },
                    onUserJourneyClick = { },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Your Statistics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Promocodes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Upvotes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Downvotes").assertIsDisplayed()
    }

    @Test
    fun profileScreen_whenSuccessState_displaysActivitySection() {
        // Given
        val state = ProfileUiState.Success(user = testUser)

        // When
        composeTestRule.setContent {
            QodeTheme {
                ProfileContent(
                    state = state,
                    onAction = { },
                    onBackClick = { },
                    onSignOut = { },
                    onAchievementsClick = { },
                    onUserJourneyClick = { },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Recent Activity").assertIsDisplayed()
        composeTestRule.onNodeWithText("Achievements").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your Journey").assertIsDisplayed()
    }

    @Test
    fun profileScreen_whenAchievementsCardClicked_triggersCallback() {
        // Given
        val state = ProfileUiState.Success(user = testUser)
        var achievementsClicked = false

        // When
        composeTestRule.setContent {
            QodeTheme {
                ProfileContent(
                    state = state,
                    onAction = { },
                    onBackClick = { },
                    onSignOut = { },
                    onAchievementsClick = { achievementsClicked = true },
                    onUserJourneyClick = { },
                )
            }
        }

        composeTestRule.onNodeWithText("Achievements").performClick()

        // Then
        assert(achievementsClicked)
    }

    @Test
    fun profileScreen_whenUserJourneyCardClicked_triggersCallback() {
        // Given
        val state = ProfileUiState.Success(user = testUser)
        var userJourneyClicked = false

        // When
        composeTestRule.setContent {
            QodeTheme {
                ProfileContent(
                    state = state,
                    onAction = { },
                    onBackClick = { },
                    onSignOut = { },
                    onAchievementsClick = { },
                    onUserJourneyClick = { userJourneyClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Your Journey").performClick()

        // Then
        assert(userJourneyClicked)
    }

    @Test
    fun profileScreen_whenErrorState_displaysErrorMessage() {
        // Given
        val state = ProfileUiState.Error(
            exception = RuntimeException("Network error"),
            isRetryable = true,
        )

        // When
        composeTestRule.setContent {
            QodeTheme {
                ProfileContent(
                    state = state,
                    onAction = { },
                    onBackClick = { },
                    onSignOut = { },
                    onAchievementsClick = { },
                    onUserJourneyClick = { },
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun profileScreen_whenRetryButtonClicked_triggersRetryAction() {
        // Given
        val state = ProfileUiState.Error(
            exception = RuntimeException("Network error"),
            isRetryable = true,
        )
        var retryClicked = false

        // When
        composeTestRule.setContent {
            QodeTheme {
                ProfileContent(
                    state = state,
                    onAction = { action ->
                        if (action is ProfileAction.RetryClicked) {
                            retryClicked = true
                        }
                    },
                    onBackClick = { },
                    onSignOut = { },
                    onAchievementsClick = { },
                    onUserJourneyClick = { },
                )
            }
        }

        composeTestRule.onNodeWithText("Retry").performClick()

        // Then
        assert(retryClicked)
    }

    @Test
    fun profileScreen_whenSignOutButtonClicked_triggersSignOutAction() {
        // Given
        val state = ProfileUiState.Success(user = testUser)
        var signOutClicked = false

        // When
        composeTestRule.setContent {
            QodeTheme {
                ProfileContent(
                    state = state,
                    onAction = { action ->
                        if (action is ProfileAction.SignOutClicked) {
                            signOutClicked = true
                        }
                    },
                    onBackClick = { },
                    onSignOut = { },
                    onAchievementsClick = { },
                    onUserJourneyClick = { },
                )
            }
        }

        composeTestRule.onNodeWithText("Sign Out").performClick()

        // Then
        assert(signOutClicked)
    }
}
