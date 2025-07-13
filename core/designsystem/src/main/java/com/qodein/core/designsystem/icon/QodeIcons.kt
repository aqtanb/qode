package com.qodein.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Qode app specific icons collection
 */
object QodeIcons {
    // Promo Code related icons
    val PromoCode: ImageVector = Icons.Default.LocalOffer
    val Discount: ImageVector = Icons.Default.Discount
    val CopyCode: ImageVector = Icons.Default.ContentCopy
    val Gift: ImageVector = Icons.Default.CardGiftcard
    val Money: ImageVector = Icons.Default.MonetizationOn

    // Status icons
    val Verified: ImageVector = Icons.Default.Verified
    val VerifiedOutlined: ImageVector = Icons.Outlined.Verified
    val VerifiedUser: ImageVector = Icons.Default.VerifiedUser
    val CheckCircle: ImageVector = Icons.Default.CheckCircle
    val CheckCircleOutlined: ImageVector = Icons.Outlined.CheckCircle
    val Premium: ImageVector = Icons.Default.WorkspacePremium
    val New: ImageVector = Icons.Default.NewReleases

    // Action icons
    val ThumbUp: ImageVector = Icons.Default.ThumbUp
    val ThumbUpOutlined: ImageVector = Icons.Outlined.ThumbUp
    val Follow: ImageVector = Icons.Default.PersonAdd
    val FollowOutlined: ImageVector = Icons.Outlined.PersonAdd
    val Trending: ImageVector = Icons.AutoMirrored.Filled.TrendingUp

    // Store and time icons
    val Store: ImageVector = Icons.Default.Store
    val StoreOutlined: ImageVector = Icons.Outlined.Store
    val Calendar: ImageVector = Icons.Default.CalendarToday
    val Schedule: ImageVector = Icons.Default.Schedule
}

/**
 * Category specific icons
 */
object QodeCategoryIcons {
    val Electronics: ImageVector = Icons.Default.Store
    val Fashion: ImageVector = Icons.Default.Store
    val Food: ImageVector = Icons.Default.Store
    val Beauty: ImageVector = Icons.Default.Store
    val Sports: ImageVector = Icons.Default.Store
    val Home: ImageVector = Icons.Default.Store
    val Books: ImageVector = Icons.Default.Store
    val Travel: ImageVector = Icons.Default.Store
    val Other: ImageVector = Icons.Default.Store
}
