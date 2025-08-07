package com.qodein.core.ui.component

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.model.Store
import com.qodein.core.ui.model.StoreCategory

/**
 * StoreCard component for displaying store information
 *
 * @param store The store data to display
 * @param onStoreClick Called when the store card is clicked
 * @param onFollowClick Called when follow button is clicked
 * @param modifier Modifier to be applied to the card
 * @param isLoggedIn Whether the user is logged in
 * @param promoCodesCount Number of active promo codes for this store
 * @param showFollowButton Whether to show the follow button
 */
@Composable
fun StoreCard(
    store: Store,
    onStoreClick: () -> Unit,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean = false,
    promoCodesCount: Int = 0,
    showFollowButton: Boolean = true
) {
    QodeCard(
        modifier = modifier,
        variant = QodeCardVariant.Elevated,
        onClick = onStoreClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Store logo
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(ShapeTokens.Corner.medium),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = QodeCommerceIcons.Store,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(SpacingTokens.md))

            // Store info
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = store.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(SpacingTokens.xs))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                ) {
                    if (promoCodesCount > 0) {
                        Text(
                            text = "$promoCodesCount codes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    if (store.followersCount > 0) {
                        Text(
                            text = "${store.followersCount} followers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Text(
                        text = store.category.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Follow button
            if (showFollowButton && isLoggedIn) {
                Spacer(modifier = Modifier.width(SpacingTokens.sm))
                QodeButton(
                    onClick = onFollowClick,
                    text = if (store.isFollowed) "Following" else "Follow",
                    variant = if (store.isFollowed) QodeButtonVariant.Outlined else QodeButtonVariant.Primary,
                    size = QodeButtonSize.Small,
                )
            }
        }
    }
}

/**
 * Compact version of StoreCard for use in lists
 */
@Composable
fun CompactStoreCard(
    store: Store,
    onStoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    promoCodesCount: Int = 0
) {
    Surface(
        modifier = modifier,
        onClick = onStoreClick,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Store logo
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(ShapeTokens.Corner.small),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = QodeCommerceIcons.Store,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(SpacingTokens.md))

            // Store info
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = store.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (promoCodesCount > 0) {
                    Text(
                        text = "$promoCodesCount active codes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Arrow indicator
            Icon(
                imageVector = QodeCommerceIcons.Store, // TODO: Use arrow icon
                contentDescription = "View store",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Grid item version of StoreCard
 */
@Composable
fun GridStoreCard(
    store: Store,
    onStoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    promoCodesCount: Int = 0
) {
    QodeCard(
        modifier = modifier,
        variant = QodeCardVariant.Elevated,
        onClick = onStoreClick,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Store logo
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(ShapeTokens.Corner.medium),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = QodeCommerceIcons.Store,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Store name
            Text(
                text = store.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (promoCodesCount > 0) {
                Spacer(modifier = Modifier.height(SpacingTokens.xs))
                Text(
                    text = "$promoCodesCount codes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

// Preview
@Preview(name = "StoreCard Variants", showBackground = true)
@Composable
private fun StoreCardPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            val sampleStore = Store(
                id = "kaspi",
                name = "Kaspi Bank",
                category = StoreCategory.Electronics,
                followersCount = 1250,
                isFollowed = false,
            )

            val followedStore = sampleStore.copy(
                name = "Arbuz.kz",
                isFollowed = true,
            )

            Text("Regular StoreCard", style = MaterialTheme.typography.titleMedium)
            StoreCard(
                store = sampleStore,
                onStoreClick = {},
                onFollowClick = {},
                isLoggedIn = true,
                promoCodesCount = 12,
            )

            Text("Followed Store", style = MaterialTheme.typography.titleMedium)
            StoreCard(
                store = followedStore,
                onStoreClick = {},
                onFollowClick = {},
                isLoggedIn = true,
                promoCodesCount = 8,
            )

            Text("Compact StoreCard", style = MaterialTheme.typography.titleMedium)
            CompactStoreCard(
                store = sampleStore,
                onStoreClick = {},
                promoCodesCount = 5,
            )

            Text("Grid StoreCard", style = MaterialTheme.typography.titleMedium)
            Row(
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                GridStoreCard(
                    store = sampleStore,
                    onStoreClick = {},
                    promoCodesCount = 12,
                    modifier = Modifier.weight(1f),
                )
                GridStoreCard(
                    store = followedStore,
                    onStoreClick = {},
                    promoCodesCount = 8,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
