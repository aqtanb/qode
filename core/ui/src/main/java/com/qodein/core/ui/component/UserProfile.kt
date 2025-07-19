package com.qodein.core.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeAvatar
import com.qodein.core.designsystem.component.QodeBadge
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.component.QodeListCard
import com.qodein.core.designsystem.component.QodeSectionHeader
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.theme.QodeAnimation
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.ui.model.PromoCode
import com.qodein.core.ui.model.User
import com.qodein.core.ui.preview.PreviewData
import com.qodein.core.ui.util.toFormattedString
import com.qodein.core.ui.util.toTimeAgo
import java.time.LocalDate

/**
 * User achievement data
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val isUnlocked: Boolean = false,
    val progress: Float = 0f, // 0.0 to 1.0
    val unlockedAt: LocalDate? = null
)

/**
 * User activity item
 */
sealed class UserActivity {
    data class SubmittedCode(val promoCode: PromoCode, val timestamp: java.time.LocalDateTime) : UserActivity()

    data class UpvotedCode(val promoCode: PromoCode, val timestamp: java.time.LocalDateTime) : UserActivity()

    data class FollowedStore(val storeName: String, val timestamp: java.time.LocalDateTime) : UserActivity()

    data class AchievementUnlocked(val achievement: Achievement, val timestamp: java.time.LocalDateTime) : UserActivity()
}

/**
 * UserProfile header component
 *
 * @param user User data
 * @param onEditProfile Called when edit profile is clicked
 * @param onShareProfile Called when share profile is clicked
 * @param modifier Modifier to be applied to the component
 * @param isOwnProfile Whether this is the current user's profile
 */
@Composable
fun UserProfileHeader(
    user: User,
    onEditProfile: () -> Unit,
    onShareProfile: () -> Unit,
    modifier: Modifier = Modifier,
    isOwnProfile: Boolean = true
) {
    QodeCard(
        modifier = modifier,
        variant = QodeCardVariant.Elevated,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Box {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer,
                            ),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(QodeSpacing.md),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    // User info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        QodeAvatar(
                            text = user.username.take(2),
                            size = 80.dp,
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                        )

                        Spacer(modifier = Modifier.width(QodeSpacing.md))

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = user.username,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )

                                if (user.totalUpvotes >= 100) {
                                    Spacer(modifier = Modifier.width(QodeSpacing.xs))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified user",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }

                            Text(
                                text = "Joined ${user.joinedAt.toLocalDate().toFormattedString()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f),
                            )
                        }
                    }

                    // Action buttons
                    Row {
                        if (isOwnProfile) {
                            IconButton(onClick = onEditProfile) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit profile",
                                    tint = Color.White,
                                )
                            }
                        }

                        IconButton(onClick = onShareProfile) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share profile",
                                tint = Color.White,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(QodeSpacing.lg))

                // User statistics
                UserStatsRow(
                    submittedCodes = user.submittedPromoCodes,
                    totalUpvotes = user.totalUpvotes,
                    followedStores = user.followedStores.size,
                    followedCategories = user.followedCategories.size,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * User statistics row
 */
@Composable
private fun UserStatsRow(
    submittedCodes: Int,
    totalUpvotes: Int,
    followedStores: Int,
    followedCategories: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(QodeCorners.md),
        color = Color.White.copy(alpha = 0.9f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QodeSpacing.md),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatItem(
                value = submittedCodes,
                label = "Codes",
                icon = QodeCommerceIcons.PromoCode,
            )

            StatItem(
                value = totalUpvotes,
                label = "Upvotes",
                icon = QodeActionIcons.Thumbs,
            )

            StatItem(
                value = followedStores,
                label = "Stores",
                icon = QodeCommerceIcons.Store,
            )

            StatItem(
                value = followedCategories,
                label = "Categories",
                icon = Icons.Default.Favorite,
            )
        }
    }
}

@Composable
private fun StatItem(
    value: Int,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )

        Spacer(modifier = Modifier.height(QodeSpacing.xs))

        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * User achievements section
 */
@Composable
fun UserAchievements(
    achievements: List<Achievement>,
    onAchievementClick: (Achievement) -> Unit,
    modifier: Modifier = Modifier,
    showAll: Boolean = false
) {
    var expanded by remember { mutableStateOf(showAll) }
    val displayedAchievements = if (expanded) achievements else achievements.take(6)

    QodeCard(
        modifier = modifier,
        variant = QodeCardVariant.Outlined,
    ) {
        Column(
            modifier = Modifier.animateContentSize(
                animationSpec = tween(durationMillis = QodeAnimation.MEDIUM),
            ),
        ) {
            QodeSectionHeader(
                title = "Achievements",
                subtitle = "${achievements.count { it.isUnlocked }}/${achievements.size} unlocked",
                action = if (achievements.size > 6) {
                    {
                        QodeButton(
                            onClick = { expanded = !expanded },
                            text = if (expanded) "Show Less" else "Show All",
                            variant = QodeButtonVariant.Text,
                            size = QodeButtonSize.Small,
                        )
                    }
                } else {
                    null
                },
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = QodeSpacing.xs,
                ),
            ) {
                items(displayedAchievements) { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        onClick = { onAchievementClick(achievement) },
                        modifier = Modifier.width(120.dp),
                    )
                }
            }
        }
    }
}

/**
 * Individual achievement card
 */
@Composable
private fun AchievementCard(
    achievement: Achievement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    QodeCard(
        modifier = modifier,
        variant = if (achievement.isUnlocked) QodeCardVariant.Elevated else QodeCardVariant.Outlined,
        onClick = onClick,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Achievement icon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(QodeCorners.md),
                color = if (achievement.isUnlocked) {
                    achievement.color.copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = achievement.icon,
                        contentDescription = null,
                        tint = if (achievement.isUnlocked) {
                            achievement.color
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(QodeSpacing.sm))

            Text(
                text = achievement.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )

            if (!achievement.isUnlocked && achievement.progress > 0) {
                Spacer(modifier = Modifier.height(QodeSpacing.xs))
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { achievement.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = achievement.color,
                )
            }
        }
    }
}

/**
 * User activity feed
 */
@Composable
fun UserActivityFeed(
    activities: List<UserActivity>,
    onActivityClick: (UserActivity) -> Unit,
    modifier: Modifier = Modifier,
    maxItems: Int = 10
) {
    QodeCard(
        modifier = modifier,
        variant = QodeCardVariant.Outlined,
    ) {
        Column {
            QodeSectionHeader(
                title = "Recent Activity",
                subtitle = "Your latest contributions",
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(QodeSpacing.sm),
            ) {
                activities.take(maxItems).forEach { activity ->
                    ActivityItem(
                        activity = activity,
                        onClick = { onActivityClick(activity) },
                    )
                }

                if (activities.size > maxItems) {
                    QodeButton(
                        onClick = { /* Navigate to full activity feed */ },
                        text = "View All Activity",
                        variant = QodeButtonVariant.Text,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/**
 * Individual activity item
 */
@Composable
private fun ActivityItem(
    activity: UserActivity,
    onClick: () -> Unit
) {
    val (icon, title, subtitle, timestamp) = when (activity) {
        is UserActivity.SubmittedCode -> {
            val code = activity.promoCode
            Tuple4(
                QodeCommerceIcons.PromoCode,
                "Submitted \"${code.title}\"",
                "New promo code for ${code.store.name}",
                activity.timestamp,
            )
        }
        is UserActivity.UpvotedCode -> {
            val code = activity.promoCode
            Tuple4(
                QodeActionIcons.Thumbs,
                "Upvoted \"${code.title}\"",
                "Helped verify this code",
                activity.timestamp,
            )
        }
        is UserActivity.FollowedStore -> Tuple4(
            QodeActionIcons.Follow,
            "Followed ${activity.storeName}",
            "Started following store",
            activity.timestamp,
        )
        is UserActivity.AchievementUnlocked -> Tuple4(
            activity.achievement.icon,
            "Unlocked \"${activity.achievement.title}\"",
            activity.achievement.description,
            activity.timestamp,
        )
    }

    QodeListCard(
        title = title,
        subtitle = "$subtitle • ${timestamp.toTimeAgo()}",
        leadingContent = {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(QodeCorners.sm),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        onClick = onClick,
    )
}

// Helper class for multiple return values
private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * User level and progress component
 */
@Composable
fun UserLevelProgress(
    currentLevel: Int,
    currentXP: Int,
    xpToNextLevel: Int,
    modifier: Modifier = Modifier
) {
    QodeCard(
        modifier = modifier,
        variant = QodeCardVariant.Filled,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Level $currentLevel",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "$currentXP / ${currentXP + xpToNextLevel} XP",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                QodeBadge(
                    text = "$xpToNextLevel XP to go",
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Spacer(modifier = Modifier.height(QodeSpacing.md))

            androidx.compose.material3.LinearProgressIndicator(
                progress = { currentXP.toFloat() / (currentXP + xpToNextLevel) },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// Sample data for previews
private fun getSampleAchievements(): List<Achievement> =
    listOf(
        Achievement(
            id = "first_code",
            title = "First Code",
            description = "Submit your first promo code",
            icon = Icons.Default.Star,
            color = Color(0xFFFFD700),
            isUnlocked = true,
            unlockedAt = LocalDate.now().minusDays(30),
        ),
        Achievement(
            id = "helpful",
            title = "Helpful",
            description = "Get 10 upvotes on your submissions",
            icon = Icons.Default.ThumbUp,
            color = Color(0xFF4CAF50),
            isUnlocked = true,
            unlockedAt = LocalDate.now().minusDays(15),
        ),
        Achievement(
            id = "popular",
            title = "Popular",
            description = "Get 100 total upvotes",
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF2196F3),
            isUnlocked = false,
            progress = 0.7f,
        ),
        Achievement(
            id = "contributor",
            title = "Contributor",
            description = "Submit 50 promo codes",
            icon = Icons.Default.EmojiEvents,
            color = Color(0xFF9C27B0),
            isUnlocked = false,
            progress = 0.4f,
        ),
    )

private fun getSampleActivities(): List<UserActivity> {
    val sampleCode = PreviewData.samplePromoCodes[0]
    return listOf(
        UserActivity.SubmittedCode(
            promoCode = sampleCode,
            timestamp = java.time.LocalDateTime.now().minusHours(2),
        ),
        UserActivity.UpvotedCode(
            promoCode = sampleCode,
            timestamp = java.time.LocalDateTime.now().minusHours(5),
        ),
        UserActivity.FollowedStore(
            storeName = "Kaspi Bank",
            timestamp = java.time.LocalDateTime.now().minusDays(1),
        ),
        UserActivity.AchievementUnlocked(
            achievement = getSampleAchievements()[1],
            timestamp = java.time.LocalDateTime.now().minusDays(2),
        ),
    )
}

// Preview
@Preview(name = "UserProfile Components", showBackground = true)
@Composable
private fun UserProfilePreview() {
    QodeTheme {
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(QodeSpacing.md),
            verticalArrangement = Arrangement.spacedBy(QodeSpacing.md),
        ) {
            item {
                UserProfileHeader(
                    user = PreviewData.sampleUsers[0],
                    onEditProfile = {},
                    onShareProfile = {},
                )
            }

            item {
                UserLevelProgress(
                    currentLevel = 5,
                    currentXP = 750,
                    xpToNextLevel = 250,
                )
            }

            item {
                UserAchievements(
                    achievements = getSampleAchievements(),
                    onAchievementClick = {},
                )
            }

            item {
                UserActivityFeed(
                    activities = getSampleActivities(),
                    onActivityClick = {},
                )
            }
        }
    }
}
