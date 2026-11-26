package com.resolum.intiva.core.fcm.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.resolum.intiva.MainActivity
import com.resolum.intiva.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.absoluteValue

private const val FCM_NOTIFICATION_LOG_TAG = "FCM_NOTIFICATION"

@Singleton
class FcmNotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)
    private val defaultChannelId = context.getString(R.string.default_notification_channel_id)

    fun showNotification(
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ) {
        if (!canPostNotifications()) return

        val pendingIntent = PendingIntent.getActivity(
            context,
            data["eventId"]?.hashCode() ?: REQUEST_CODE_OPEN_APP,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_NOTIFICATION_TYPE, data["type"])
                putExtra(EXTRA_SOURCE, data["source"])
                putExtra(EXTRA_SOURCE_ID, data["sourceId"])
                putExtra("plan_id", data["planId"])
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, defaultChannelId)
            .setSmallIcon(R.drawable.intiva_notification_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            val eventId = data["eventId"]
            if (eventId.isNullOrBlank()) {
                notificationManager.notify(data.notificationId(), notification)
            } else {
                notificationManager.notify(eventId, 0, notification)
            }
        } catch (exception: SecurityException) {
            Log.w(
                FCM_NOTIFICATION_LOG_TAG,
                "Notification permission was rejected before showing the push.",
                exception
            )
        }
    }

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun Map<String, String>.notificationId(): Int {
        val sourceId = this["sourceId"]?.toIntOrNull()
        if (sourceId != null) return sourceId.absoluteValue

        return System.currentTimeMillis().toInt().absoluteValue
    }

    companion object {
        private const val REQUEST_CODE_OPEN_APP = 3001
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
        const val EXTRA_SOURCE = "notification_source"
        const val EXTRA_SOURCE_ID = "notification_source_id"
    }
}
