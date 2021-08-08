/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.notifications

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import javax.inject.Inject
import timber.log.Timber
import tm.alashow.base.inititializer.AppInitializer
import tm.alashow.base.util.extensions.orBlank
import tm.alashow.datmusic.R
import tm.alashow.datmusic.ui.buildArtistDetailsIntent
import tm.alashow.datmusic.ui.buildSearchIntent

const val defaultChannel = "default"

class NotificationsInitializer @Inject constructor() : AppInitializer {
    override fun init(application: Application) {
        if (Build.VERSION.SDK_INT < 26) return
        with(application) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val defaultChannel = NotificationChannel(
                defaultChannel,
                getString(R.string.notifications_defaultChannel),
                NotificationManager.IMPORTANCE_HIGH
            )
            defaultChannel.enableLights(true)
            defaultChannel.lightColor = ResourcesCompat.getColor(resources, R.color.primary, null)
            defaultChannel.enableVibration(true)
            notificationManager.createNotificationChannel(defaultChannel)
        }
    }
}

fun Context.notify(notification: AppNotification) {
    val (title, message, type, value) = notification
    try {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var notificationIntent: Intent? = null
        var pendingIntent: PendingIntent? = null
        when (type) {
            "url" -> notificationIntent = Intent(Intent.ACTION_VIEW, value?.toUri())
            "query" -> pendingIntent = buildSearchIntent(value.orBlank())
            "artist_details" -> pendingIntent = buildArtistDetailsIntent(value.orBlank())
            else -> Timber.d("Unhandled notification type: $type")
        }
        if (pendingIntent == null && notificationIntent != null)
            pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, defaultChannel)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setColor(ResourcesCompat.getColor(resources, R.color.primary, null))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
        notificationManager.notify(type.hashCode(), builder.build())
    } catch (e: Exception) {
        Timber.e(e)
    }
}
