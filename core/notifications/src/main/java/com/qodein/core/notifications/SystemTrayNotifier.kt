package com.qodein.core.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The fully qualified name of the main activity to launch when tapping notifications.
 */
private const val TARGET_ACTIVITY_NAME = "com.qodein.qode.MainActivity"

/**
 * Notification channel ID for upload-related notifications.
 * Users can customize this channel's behavior in system settings.
 */
private const val UPLOAD_CHANNEL_ID = "post_upload"

/**
 * Group identifier for visually grouping multiple upload notifications together.
 * All notifications with this group tag will be stacked in the notification drawer.
 */
private const val UPLOAD_NOTIFICATION_GROUP = "post_uploads"

/**
 * Fixed notification ID for the group summary notification.
 * This ID must remain constant so Android can update/cancel the summary notification.
 */
private const val UPLOAD_SUMMARY_NOTIFICATION_ID = 1

/**
 * Deep link scheme for navigating to app content from notifications.
 */
private const val DEEP_LINK_SCHEME = "qodein"

/**
 * Deep link path segment for navigating to post detail screens.
 */
private const val DEEP_LINK_POST_PATH = "post"

/**
 * Implementation of [Notifier] that displays notifications in the system tray.
 */
@SuppressLint("MissingPermission")
@Singleton
internal class SystemTrayNotifier @Inject constructor(@param:ApplicationContext private val context: Context) : Notifier {
    override fun showUploadProgress(
        uploadId: String,
        progress: Int,
        max: Int
    ) {
        if (!hasNotificationPermission()) return

        val notification = context.createUploadNotification {
            setContentTitle(context.getString(R.string.core_notifications_upload_in_progress))
            setContentText(context.getString(R.string.core_notifications_upload_progress, progress, max))
            setSmallIcon(R.drawable.core_notifications_ic_qode_notification)
            setProgress(max, progress, false)
            setOngoing(true)
            setGroup(UPLOAD_NOTIFICATION_GROUP)
        }

        NotificationManagerCompat.from(context).notify(uploadId.hashCode(), notification)
        showUploadSummary()
    }

    override fun showUploadSuccess(uploadId: String) {
        if (!hasNotificationPermission()) return

        NotificationManagerCompat.from(context).cancel(uploadId.hashCode())
        NotificationManagerCompat.from(context).cancel(UPLOAD_SUMMARY_NOTIFICATION_ID)

        val notification = context.createUploadNotification {
            setContentTitle(context.getString(R.string.core_notifications_upload_success))
            setContentText(context.getString(R.string.core_notifications_tap_to_view))
            setContentIntent(context.createPostDeepLinkIntent(uploadId))
            setSmallIcon(R.drawable.core_notifications_ic_qode_notification)
            setAutoCancel(true)
        }

        NotificationManagerCompat.from(context).notify(uploadId.hashCode(), notification)
    }

    override fun showUploadError(uploadId: String) {
        if (!hasNotificationPermission()) return

        NotificationManagerCompat.from(context).cancel(uploadId.hashCode())
        NotificationManagerCompat.from(context).cancel(UPLOAD_SUMMARY_NOTIFICATION_ID)

        // TODO: Implement retry intent using BroadcastReceiver
        val notification = context.createUploadNotification {
            setContentTitle(context.getString(R.string.core_notifications_upload_failed))
            setContentText(context.getString(R.string.core_notifications_upload_failed_content))
            setSmallIcon(R.drawable.core_notifications_ic_qode_notification)
            setAutoCancel(true)
        }

        NotificationManagerCompat.from(context).notify(uploadId.hashCode(), notification)
    }

    private fun hasNotificationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PERMISSION_GRANTED
        } else {
            true
        }

    private fun showUploadSummary() {
        if (!hasNotificationPermission()) return

        val summaryNotification = context.createUploadNotification {
            setContentTitle(context.getString(R.string.core_notifications_uploading_posts))
            setContentText(context.getString(R.string.core_notifications_multiple_uploads))
            setSmallIcon(R.drawable.core_notifications_ic_qode_notification)
            setGroup(UPLOAD_NOTIFICATION_GROUP)
            setGroupSummary(true)
        }

        NotificationManagerCompat.from(context).notify(UPLOAD_SUMMARY_NOTIFICATION_ID, summaryNotification)
    }
}

/**
 * Creates a notification for configured for upload updates
 */
private fun Context.createUploadNotification(block: NotificationCompat.Builder.() -> Unit): Notification {
    ensureNotificationChannelExists()

    return NotificationCompat.Builder(this, UPLOAD_CHANNEL_ID).apply(block).build()
}

/**
 * Ensures that a notification channel is present if applicable
 */
private fun Context.ensureNotificationChannelExists() {
    val channel = NotificationChannel(
        UPLOAD_CHANNEL_ID,
        getString(R.string.core_notifications_upload_channel_name),
        NotificationManager.IMPORTANCE_LOW,
    ).apply {
        description = getString(R.string.core_notifications_upload_channel_description)
    }

    NotificationManagerCompat.from(this).createNotificationChannel(channel)
}

/**
 * Creates a PendingIntent that opens the post detail screen via deep link
 */
private fun Context.createPostDeepLinkIntent(postId: String): PendingIntent {
    val deepLinkUri = "$DEEP_LINK_SCHEME://$DEEP_LINK_POST_PATH/$postId".toUri()

    val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
        component = ComponentName(
            packageName,
            TARGET_ACTIVITY_NAME,
        )
    }

    return PendingIntent.getActivity(
        this,
        postId.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}
