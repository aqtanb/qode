package com.qodein.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Diversity1
import androidx.compose.material.icons.filled.FiberNew
import androidx.compose.material.icons.filled.Filter1
import androidx.compose.material.icons.filled.Filter2
import androidx.compose.material.icons.filled.Filter3
import androidx.compose.material.icons.filled.Filter4
import androidx.compose.material.icons.filled.Filter5
import androidx.compose.material.icons.filled.Filter6
import androidx.compose.material.icons.filled.Filter7
import androidx.compose.material.icons.filled.Filter8
import androidx.compose.material.icons.filled.Filter9
import androidx.compose.material.icons.filled.Filter9Plus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Diversity1
import androidx.compose.material.icons.outlined.EmojiSymbols
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FeatherIcons
import compose.icons.SimpleIcons
import compose.icons.TablerIcons
import compose.icons.feathericons.Activity
import compose.icons.feathericons.AlertCircle
import compose.icons.feathericons.ArrowDown
import compose.icons.feathericons.ArrowUp
import compose.icons.feathericons.Bell
import compose.icons.feathericons.Box
import compose.icons.feathericons.Calendar
import compose.icons.feathericons.Check
import compose.icons.feathericons.CheckCircle
import compose.icons.feathericons.ChevronLeft
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.Coffee
import compose.icons.feathericons.DollarSign
import compose.icons.feathericons.Eye
import compose.icons.feathericons.Gift
import compose.icons.feathericons.Image
import compose.icons.feathericons.Info
import compose.icons.feathericons.Loader
import compose.icons.feathericons.MessageCircle
import compose.icons.feathericons.MessageSquare
import compose.icons.feathericons.Percent
import compose.icons.feathericons.Plus
import compose.icons.feathericons.RefreshCw
import compose.icons.feathericons.Search
import compose.icons.feathericons.Settings
import compose.icons.feathericons.Share2
import compose.icons.feathericons.X
import compose.icons.simpleicons.Discord
import compose.icons.simpleicons.Google
import compose.icons.simpleicons.Telegram
import compose.icons.simpleicons.Twitter
import compose.icons.tablericons.Certificate
import compose.icons.tablericons.CurrencyDollar
import compose.icons.tablericons.Language
import compose.icons.tablericons.Ticket

object PromocodeIcons {
    val Promocode: ImageVector = TablerIcons.Ticket

    val Verified: ImageVector = Icons.Outlined.Verified
    val DiscountType: ImageVector = Icons.Outlined.EmojiSymbols
    val DiscountValue: ImageVector = Icons.Outlined.Sell
    val MinimumOrder: ImageVector = Icons.Outlined.Money
    val StartDate: ImageVector = Icons.Outlined.CalendarToday
    val EndDate: ImageVector = Icons.Outlined.EventBusy
    val Description: ImageVector = Icons.Outlined.Description
    val Percentage: ImageVector = FeatherIcons.Percent
    val FixedAmount: ImageVector = FeatherIcons.DollarSign
    val FreeItem: ImageVector = FeatherIcons.Gift
}

object PostIcons {
    val Post = Icons.AutoMirrored.Outlined.TextSnippet
    val Hashtag: ImageVector = Icons.Outlined.Tag
    val PostAdd: ImageVector = Icons.Outlined.PostAdd
}

object QodeIcons {
    val Feed: ImageVector = Icons.Outlined.Diversity1
    val FeedFilled: ImageVector = Icons.Filled.Diversity1
    val Home: ImageVector = Icons.Outlined.Home
    val HomeFilled: ImageVector = Icons.Filled.Home
    val Sale: ImageVector = FeatherIcons.Percent
    val Dollar: ImageVector = TablerIcons.CurrencyDollar
    val Service: ImageVector = Icons.Outlined.Store
}

object QodeCategoryIcons {
    val Language: ImageVector = TablerIcons.Language
    val Certification: ImageVector = TablerIcons.Certificate
    val Consulting: ImageVector = FeatherIcons.MessageCircle
}

object ActionIcons {
    val MoreVert = Icons.Outlined.MoreVert
    val SignOut = Icons.AutoMirrored.Filled.Logout

    val Add: ImageVector = FeatherIcons.Plus
    val Clear: ImageVector = Icons.Outlined.Clear
    val Check: ImageVector = FeatherIcons.Check
    val Copy: ImageVector = Icons.Outlined.ContentCopy
    val Up: ImageVector = FeatherIcons.ArrowUp
    val Down: ImageVector = FeatherIcons.ArrowDown
    val Close: ImageVector = FeatherIcons.X
    val Previous: ImageVector = FeatherIcons.ChevronLeft
    val Share: ImageVector = FeatherIcons.Share2
    val Preview: ImageVector = FeatherIcons.Eye
}

object NavigationIcons {
    val Back = Icons.AutoMirrored.Default.ArrowBack
    val Close = Icons.Default.Close

    val ChevronLeft = Icons.Default.ChevronLeft
    val ChevronRight = Icons.Default.ChevronRight
    val Upward = Icons.Default.ArrowUpward
    val Downward = Icons.Default.ArrowDownward

    val Search: ImageVector = FeatherIcons.Search
    val Settings: ImageVector = FeatherIcons.Settings
    val Notifications: ImageVector = FeatherIcons.Bell
    val Gallery: ImageVector = FeatherIcons.Image
    val Refresh: ImageVector = FeatherIcons.RefreshCw
    val Loading: ImageVector = FeatherIcons.Loader
    val Error: ImageVector = FeatherIcons.AlertCircle
    val Info: ImageVector = FeatherIcons.Info
    val Success: ImageVector = FeatherIcons.CheckCircle
    val Feedback: ImageVector = FeatherIcons.MessageSquare
}

object QodeStatusIcons {
    val Trending: ImageVector = FeatherIcons.ArrowUp
}

object QodeSocialIcons {
    val Google: ImageVector = SimpleIcons.Google
    val Twitter: ImageVector = SimpleIcons.Twitter
    val Discord: ImageVector = SimpleIcons.Discord
    val Telegram: ImageVector = SimpleIcons.Telegram
}

object UIIcons {
    val DarkMode: ImageVector = Icons.Outlined.DarkMode
    val VoteScore = Icons.Outlined.Star
    val Paste: ImageVector = Icons.Outlined.ContentPaste
    val Block: ImageVector = Icons.Outlined.Block
    val Report: ImageVector = Icons.Outlined.Flag

    val AccountCircle: ImageVector = Icons.Outlined.AccountCircle

    val Popular: ImageVector = Icons.Filled.Diamond
    val Newest: ImageVector = Icons.Filled.FiberNew
    val Expiring: ImageVector = Icons.Filled.Timelapse

    val Empty: ImageVector = FeatherIcons.Coffee
    val Error: ImageVector = FeatherIcons.AlertCircle

    val Filter1: ImageVector = Icons.Filled.Filter1
    val Filter2: ImageVector = Icons.Filled.Filter2
    val Filter3: ImageVector = Icons.Filled.Filter3
    val Filter4: ImageVector = Icons.Filled.Filter4
    val Filter5: ImageVector = Icons.Filled.Filter5
    val Filter6: ImageVector = Icons.Filled.Filter6
    val Filter7: ImageVector = Icons.Filled.Filter7
    val Filter8: ImageVector = Icons.Filled.Filter8
    val Filter9: ImageVector = Icons.Filled.Filter9
    val Filter9Plus: ImageVector = Icons.Filled.Filter9Plus
}

object QodeCalendarIcons {

    val Datepicker: ImageVector = FeatherIcons.Calendar
}

object QodeBusinessIcons {
    val Asset: ImageVector = FeatherIcons.Box
}

object UserIcons {
    val Activity = FeatherIcons.Activity
}
