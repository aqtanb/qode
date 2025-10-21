package com.qodein.feature.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.testing.data.TestUsers
import com.qodein.core.ui.component.QodeRetryableErrorCard
import com.qodein.feature.profile.component.StatsSection
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.qodein.core.designsystem.R as DesignR
import com.qodein.feature.profile.R as FeatureR

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val testUser = TestUsers.sampleUser

    /**
     * Helper function to reduce boilerplate code in tests by setting the content once.
     * Following NIA pattern - no animation workarounds, test composables directly.
     */
    private fun renderProfileScreen(
        state: ProfileUiState,
        onAction: (ProfileAction) -> Unit = {},
        onBackClick: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            QodeTheme {
                // Use the actual ProfileScreen state rendering logic
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    when (state) {
                        is ProfileUiState.Success -> {
                            ProfileContent(
                                user = state.user,
                                onAction = onAction,
                                onBackClick = onBackClick,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        is ProfileUiState.Loading -> {
                            val loadingDescription = context.getString(FeatureR.string.profile_loading_description)
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(SpacingTokens.lg)
                                    .semantics {
                                        contentDescription = loadingDescription
                                    },
                            )
                        }
                        is ProfileUiState.Error -> {
                            QodeRetryableErrorCard(
                                message = state.error.message ?: context.getString(FeatureR.string.profile_error_unknown),
                                onRetry = { onAction(ProfileAction.RetryClicked) },
                                modifier = Modifier
                                    .padding(SpacingTokens.lg),
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    fun profileScreen_whenLoadingState_showsLoadingIndicator() {
        // Given
        val state = ProfileUiState.Loading

        // When
        renderProfileScreen(state = state)

        // Then
        composeTestRule
            .onNodeWithContentDescription(context.getString(FeatureR.string.profile_loading_description))
            .assertExists()
    }

    // MARK: - Direct Component Tests (Following NIA Pattern)

    @Test
    fun profileHeader_displaysUserInformation() {
        composeTestRule.setContent {
            QodeTheme {
                ProfileHeader(
                    user = testUser,
                    onAction = {}, // Empty action for tests
                )
            }
        }

        composeTestRule.onNodeWithText("John Doe").assertExists()
        composeTestRule.onNodeWithText("@johndoe").assertExists()
        composeTestRule.onNodeWithTag("profile_avatar").assertExists()
        composeTestRule.onNodeWithText(context.getString(FeatureR.string.edit_profile_button)).assertExists()
    }

    @Test
    fun statsSection_displaysUserStats() {
        composeTestRule.setContent {
            QodeTheme {
                StatsSection(userStats = testUser.stats)
            }
        }

        composeTestRule.onNodeWithText(context.getString(FeatureR.string.profile_stats_title)).assertExists()
        composeTestRule.onNodeWithText(context.getString(FeatureR.string.profile_promocodes_label)).assertExists()
        composeTestRule.onNodeWithText(context.getString(FeatureR.string.profile_upvotes_label)).assertExists()
        composeTestRule.onNodeWithText(context.getString(FeatureR.string.profile_downvotes_label)).assertExists()
    }

    @Test
    fun activityFeed_displaysActivitySections() {
        var achievementsClicked = false
        var userJourneyClicked = false

        composeTestRule.setContent {
            QodeTheme {
                ActivityFeed(
                    onAction = { action ->
                        when (action) {
                            ProfileAction.AchievementsClicked -> achievementsClicked = true
                            ProfileAction.UserJourneyClicked -> userJourneyClicked = true
                            else -> {}
                        }
                    },
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(FeatureR.string.profile_activity_title)).assertExists()
        composeTestRule.onNodeWithText(context.getString(FeatureR.string.profile_achievements_title)).assertExists()
        composeTestRule.onNodeWithText(context.getString(FeatureR.string.profile_user_journey_title)).assertExists()

        // Test click actions
        composeTestRule.onNodeWithText(context.getString(FeatureR.string.profile_achievements_title)).performClick()
        assert(achievementsClicked)

        composeTestRule.onNodeWithText(context.getString(FeatureR.string.profile_user_journey_title)).performClick()
        assert(userJourneyClicked)
    }

    // MARK: - Integration Tests

    @Test
    fun profileScreen_whenErrorState_displaysErrorMessage() {
        // Given
        val state = ProfileUiState.Error(
            exception = RuntimeException("Network error"),
            isRetryable = true,
        )

        // When
        renderProfileScreen(state = state)

        // Then
        composeTestRule.onNodeWithText(context.getString(DesignR.string.error_default_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(DesignR.string.error_retry_button)).assertIsDisplayed()
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
        renderProfileScreen(
            state = state,
            onAction = { action ->
                if (action is ProfileAction.RetryClicked) {
                    retryClicked = true
                }
            },
        )

        composeTestRule.onNodeWithText(context.getString(DesignR.string.error_retry_button)).performClick()

        // Then
        assert(retryClicked)
    }

    @Test
    fun animatedSignOutButton_whenVisible_triggersSignOutAction() {
        var signOutClicked = false

        composeTestRule.setContent {
            QodeTheme {
                AnimatedSignOutButton(
                    onAction = { action ->
                        if (action is ProfileAction.SignOutClicked) {
                            signOutClicked = true
                        }
                    },
                    isVisible = true,
                )
            }
        }

        composeTestRule.onNodeWithTag("sign_out_button").performClick()

        assert(signOutClicked)
    }
}
