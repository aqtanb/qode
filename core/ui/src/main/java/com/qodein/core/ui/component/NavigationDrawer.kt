package com.qodein.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeAvatar
import com.qodein.core.designsystem.component.QodeBadge
import com.qodein.core.designsystem.component.QodeListCard
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.model.User
import java.time.LocalDateTime

/**
 * Navigation drawer menu item data
 */
data class DrawerMenuItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val badge: String? = null,
    val isComingSoon: Boolean = false,
    val isPremium: Boolean = false,
    val isEnabled: Boolean = true,
    val onClick: () -> Unit = {}
)

/**
 * Navigation drawer content component
 *
 * @param user Current user data (null if not logged in)
 * @param onMenuItemClick Called when a menu item is clicked
 * @param onUserProfileClick Called when user profile section is clicked
 * @param onLoginClick Called when login button is clicked
 * @param modifier Modifier to be applied to the drawer
 * @param appVersion App version string to display
 */
@Composable
fun NavigationDrawerContent(
    user: User?,
    onMenuItemClick: (DrawerMenuItem) -> Unit,
    onUserProfileClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    appVersion: String = "1.0.0"
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        item {
            // User profile header
            UserProfileHeader(
                user = user,
                onProfileClick = onUserProfileClick,
                onLoginClick = onLoginClick,
            )
        }

        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = SpacingTokens.sm),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }

        // Main menu items
        items(getMainMenuItems()) { menuItem ->
            DrawerMenuItemCard(
                item = menuItem,
                onClick = { onMenuItemClick(menuItem) },
            )
        }

        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = SpacingTokens.sm),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }

        // Settings and info items
        items(getSecondaryMenuItems()) { menuItem ->
            DrawerMenuItemCard(
                item = menuItem,
                onClick = { onMenuItemClick(menuItem) },
            )
        }

        item {
            // App version footer
            Text(
                text = "Version $appVersion",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(SpacingTokens.md),
            )
        }
    }
}

/**
 * User profile header section
 */
@Composable
private fun UserProfileHeader(
    user: User?,
    onProfileClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer,
                    ),
                ),
            )
            .padding(SpacingTokens.md),
    ) {
        if (user != null) {
            // Logged in user
            QodeListCard(
                title = user.username,
                subtitle = buildString {
                    append("${user.submittedPromoCodes} codes submitted")
                    if (user.totalUpvotes > 0) {
                        append(" • ${user.totalUpvotes} upvotes")
                    }
                },
                leadingContent = {
                    QodeAvatar(
                        text = user.username.take(2),
                        size = 56.dp,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                    )
                },
                trailingContent = {
                    if (user.totalUpvotes >= 100) {
                        QodeBadge(
                            text = "VIP",
                            containerColor = Color(0xFFFFD700),
                            contentColor = Color.Black,
                        )
                    }
                },
                onClick = onProfileClick,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            // Guest user
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                QodeAvatar(
                    size = 64.dp,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(SpacingTokens.sm))

                Text(
                    text = "Welcome to Qode!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )

                Text(
                    text = "Join to submit codes and earn points",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                )

                Spacer(modifier = Modifier.height(SpacingTokens.sm))

                Surface(
                    onClick = onLoginClick,
                    shape = RoundedCornerShape(QodeCorners.md),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = SpacingTokens.md,
                            vertical = SpacingTokens.sm,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(SpacingTokens.sm))
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual drawer menu item
 */
@Composable
private fun DrawerMenuItemCard(
    item: DrawerMenuItem,
    onClick: () -> Unit
) {
    QodeListCard(
        title = item.title,
        subtitle = item.subtitle,
        leadingContent = {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(QodeCorners.sm),
                color = if (item.isEnabled) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (item.isEnabled) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
            ) {
                when {
                    item.badge != null -> {
                        QodeBadge(
                            text = item.badge,
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        )
                    }
                    item.isComingSoon -> {
                        QodeBadge(
                            text = "Soon",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                    item.isPremium -> {
                        QodeBadge(
                            text = "Premium",
                            containerColor = Color(0xFFFFD700), // Gold color
                            contentColor = Color.Black,
                        )
                    }
                }
            }
        },
        onClick = if (item.isEnabled && !item.isComingSoon) {
            { onClick() }
        } else {
            {}
        },
        enabled = item.isEnabled && !item.isComingSoon,
    )
}

/**
 * Get main menu items
 */
private fun getMainMenuItems(): List<DrawerMenuItem> =
    listOf(
        DrawerMenuItem(
            id = "categories",
            title = "Categories",
            subtitle = "Browse by category",
            icon = Icons.Default.Category,
        ),
        DrawerMenuItem(
            id = "following",
            title = "Following",
            subtitle = "Stores & categories you follow",
            icon = Icons.Default.PersonAdd,
        ),
        DrawerMenuItem(
            id = "favorites",
            title = "Favorites",
            subtitle = "Saved promo codes",
            icon = Icons.Default.Favorite,
            isComingSoon = true,
        ),
        DrawerMenuItem(
            id = "submit",
            title = "Submit Promo Code",
            subtitle = "Share a new code",
            icon = Icons.Default.Add,
        ),
        DrawerMenuItem(
            id = "achievements",
            title = "Achievements",
            subtitle = "Your progress & badges",
            icon = Icons.Default.EmojiEvents,
            isComingSoon = true,
        ),
        DrawerMenuItem(
            id = "family",
            title = "Family Subscriptions",
            subtitle = "Share premium with family",
            icon = Icons.Default.Group,
            isPremium = true,
        ),
    )

/**
 * Get secondary menu items
 */
private fun getSecondaryMenuItems(): List<DrawerMenuItem> =
    listOf(
        DrawerMenuItem(
            id = "settings",
            title = "Settings",
            subtitle = "App preferences",
            icon = Icons.Default.Settings,
        ),
        DrawerMenuItem(
            id = "help",
            title = "Help & Support",
            subtitle = "FAQ and contact",
            icon = Icons.Default.HelpOutline,
        ),
        DrawerMenuItem(
            id = "about",
            title = "About Qode",
            subtitle = "App info and version",
            icon = Icons.Default.Info,
        ),
    )

/**
 * Compact navigation drawer for smaller screens
 */
@Composable
fun CompactNavigationDrawer(
    user: User?,
    selectedItemId: String?,
    onMenuItemClick: (DrawerMenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val compactItems = listOf(
        DrawerMenuItem(
            id = "categories",
            title = "Categories",
            icon = Icons.Default.Category,
        ),
        DrawerMenuItem(
            id = "following",
            title = "Following",
            icon = Icons.Default.PersonAdd,
        ),
        DrawerMenuItem(
            id = "submit",
            title = "Submit",
            icon = Icons.Default.Add,
        ),
        DrawerMenuItem(
            id = "achievements",
            title = "Rewards",
            icon = Icons.Default.Star,
            isComingSoon = true,
        ),
    )

    Column(
        modifier = modifier.padding(SpacingTokens.sm),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        // User avatar
        QodeAvatar(
            text = user?.username?.take(2) ?: "G",
            size = 48.dp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(SpacingTokens.sm))

        // Compact menu items
        compactItems.forEach { item ->
            Surface(
                onClick = { onMenuItemClick(item) },
                shape = RoundedCornerShape(QodeCorners.sm),
                color = if (selectedItemId == item.id) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                },
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(SpacingTokens.sm),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (selectedItemId == item.id) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

// Sample data for preview
private fun getSampleUser(): User =
    User(
        id = "user123",
        username = "AkezhanB",
        email = "akezhan@example.com",
        totalUpvotes = 156,
        submittedPromoCodes = 23,
        followedStores = listOf("kaspi", "arbuz"),
        followedCategories = listOf("electronics", "food"),
        joinedAt = LocalDateTime.now().minusMonths(3),
    )

// Preview
@Preview(name = "NavigationDrawer", showBackground = true)
@Composable
private fun NavigationDrawerPreview() {
    QodeTheme {
        Row {
            // Full drawer
            Surface(
                modifier = Modifier
                    .width(320.dp)
                    .height(600.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                NavigationDrawerContent(
                    user = getSampleUser(),
                    onMenuItemClick = {},
                    onUserProfileClick = {},
                    onLoginClick = {},
                )
            }

            // Compact drawer
            Surface(
                modifier = Modifier
                    .width(80.dp)
                    .height(600.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                CompactNavigationDrawer(
                    user = getSampleUser(),
                    selectedItemId = "categories",
                    onMenuItemClick = {},
                )
            }
        }
    }
}

@Preview(name = "NavigationDrawer Guest", showBackground = true)
@Composable
private fun NavigationDrawerGuestPreview() {
    QodeTheme {
        Surface(
            modifier = Modifier
                .width(320.dp)
                .height(600.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            NavigationDrawerContent(
                user = null,
                onMenuItemClick = {},
                onUserProfileClick = {},
                onLoginClick = {},
            )
        }
    }
}
