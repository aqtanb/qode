// core/designsystem/src/main/kotlin/com/qodein/core/designsystem/icon/QodeIconsPreviews.kt
package com.qodein.core.designsystem.icon

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.QodeTheme

@Composable
private fun IconItem(
    icon: ImageVector,
    name: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = name,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconGroup(
    title: String,
    icons: List<Pair<ImageVector, String>>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            icons.forEach { (icon, name) ->
                IconItem(icon = icon, name = name)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QodeIconsPromoCodePreview() {
    QodeTheme {
        Surface {
            IconGroup(
                title = "Promo Code Icons",
                icons = listOf(
                    QodeIcons.PromoCode to "PromoCode",
                    QodeIcons.Discount to "Discount",
                    QodeIcons.CopyCode to "CopyCode",
                    QodeIcons.Gift to "Gift",
                    QodeIcons.Money to "Money",
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QodeIconsStatusPreview() {
    QodeTheme {
        Surface {
            IconGroup(
                title = "Status Icons",
                icons = listOf(
                    QodeIcons.Verified to "Verified",
                    QodeIcons.VerifiedOutlined to "VerifiedOutlined",
                    QodeIcons.CheckCircle to "CheckCircle",
                    QodeIcons.CheckCircleOutlined to "CheckCircleOutlined",
                    QodeIcons.Premium to "Premium",
                    QodeIcons.New to "New",
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QodeIconsActionPreview() {
    QodeTheme {
        Surface {
            IconGroup(
                title = "Action Icons",
                icons = listOf(
                    QodeIcons.ThumbUp to "ThumbUp",
                    QodeIcons.ThumbUpOutlined to "ThumbUpOutlined",
                    QodeIcons.Follow to "Follow",
                    QodeIcons.FollowOutlined to "FollowOutlined",
                    QodeIcons.Trending to "Trending",
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QodeCategoryIconsPreview() {
    QodeTheme {
        Surface {
            IconGroup(
                title = "Category Icons",
                icons = listOf(
                    QodeCategoryIcons.Electronics to "Electronics",
                    QodeCategoryIcons.Fashion to "Fashion",
                    QodeCategoryIcons.Food to "Food",
                    QodeCategoryIcons.Beauty to "Beauty",
                    QodeCategoryIcons.Sports to "Sports",
                    QodeCategoryIcons.Home to "Home",
                    QodeCategoryIcons.Books to "Books",
                    QodeCategoryIcons.Travel to "Travel",
                ),
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun AllQodeIconsPreview() {
    QodeTheme {
        Surface {
            Column {
                IconGroup(
                    title = "Promo Code",
                    icons = listOf(
                        QodeIcons.PromoCode to "PromoCode",
                        QodeIcons.Discount to "Discount",
                        QodeIcons.CopyCode to "Copy",
                        QodeIcons.Gift to "Gift",
                    ),
                )
                IconGroup(
                    title = "Status",
                    icons = listOf(
                        QodeIcons.Verified to "Verified",
                        QodeIcons.CheckCircle to "Check",
                        QodeIcons.Premium to "Premium",
                        QodeIcons.New to "New",
                    ),
                )
                IconGroup(
                    title = "Actions",
                    icons = listOf(
                        QodeIcons.ThumbUp to "Like",
                        QodeIcons.Follow to "Follow",
                        QodeIcons.Trending to "Trending",
                        QodeIcons.Store to "Store",
                    ),
                )
            }
        }
    }
}
