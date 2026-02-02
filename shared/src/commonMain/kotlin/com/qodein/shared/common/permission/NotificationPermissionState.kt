package com.qodein.shared.common.permission

sealed interface NotificationPermissionState {
    data object Granted : NotificationPermissionState
    data object Denied : NotificationPermissionState
    data object NotRequired : NotificationPermissionState
}
