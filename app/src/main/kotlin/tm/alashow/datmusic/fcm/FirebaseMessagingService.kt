/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.fcm

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.messaging.FirebaseMessagingService as FirebaseService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.datmusic.R
import tm.alashow.datmusic.data.interactors.RegisterFcmToken

@AndroidEntryPoint
class FirebaseMessagingService : FirebaseService(), CoroutineScope by MainScope() {

    @Inject
    lateinit var registerFcmToken: RegisterFcmToken

    override fun onNewToken(token: String) {
        launch(Dispatchers.Default) {
            registerFcmToken(RegisterFcmToken.Params(token)).collect { result ->
                Timber.d("Result: $result")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            sendNotification(remoteMessage)
        } else {
            super.onMessageReceived(remoteMessage)
        }
    }

    @SuppressLint("InlinedApi")
    private fun sendNotification(remoteMessage: RemoteMessage) {
        initChannels()
        try {
            val data = remoteMessage.data
            var title = data["title"]
            var message = data["message"]
            val notification = remoteMessage.notification
            if (notification != null) {
                title = notification.title
                message = notification.body
            }
            val type = data["type"]
            val value = data["value"]
            if (type == "url") {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                var notificationIntent: Intent? = null
                when (type) {
                    "url" -> notificationIntent = Intent(Intent.ACTION_VIEW, Uri.parse(value))
                }
                val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
                val mBuilder = NotificationCompat.Builder(this, "default")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(
                        NotificationCompat.BigTextStyle().bigText(message)
                    )
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setColor(ResourcesCompat.getColor(resources, R.color.primary, null))
                    .setContentIntent(pendingIntent).setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                notificationManager.notify(type.hashCode(), mBuilder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initChannels() {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val othersChannel = NotificationChannel("default", "Default notifications", NotificationManager.IMPORTANCE_HIGH)
        othersChannel.enableLights(true)
        othersChannel.lightColor = Color.parseColor("#16053D")
        othersChannel.enableVibration(true)
        notificationManager.createNotificationChannel(othersChannel)
    }
}
