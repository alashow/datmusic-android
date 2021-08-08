/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat as NotificationMediaCompat
import androidx.palette.graphics.Palette
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tm.alashow.base.ui.utils.extensions.systemService
import tm.alashow.base.util.extensions.isOreo
import tm.alashow.datmusic.playback.receivers.MediaButtonReceiver.Companion.buildMediaButtonPendingIntent
import tm.alashow.datmusic.playback.services.PlayerService

const val NOTIFICATION_ID = 2000
const val CHANNEL_ID = "audio-player"

const val PREVIOUS = "action_previous"
const val NEXT = "action_next"
const val STOP_PLAYBACK = "action_stop"
const val PLAY_PAUSE = "action_play_or_pause"
const val REPEAT_ONE = "action_repeat_one"
const val REPEAT_ALL = "action_repeat_all"
const val PLAY_ALL_SHUFFLED = "action_play_all_shuffled"
const val PLAY_NEXT = "action_play_next"
const val REMOVE_QUEUE_ITEM_BY_POSITION = "action_remove_queue_item_by_position"
const val REMOVE_QUEUE_ITEM_BY_ID = "action_remove_queue_item_by_id"
const val UPDATE_QUEUE = "action_update_queue"
const val SET_MEDIA_STATE = "action_set_media_state"
const val PLAY_ACTION = "action_play"
const val PAUSE_ACTION = "action_pause"
const val SWAP_ACTION = "swap_action"
const val BY_UI_KEY = "by_ui_key"

interface MediaNotifications {
    fun updateNotification(mediaSession: MediaSessionCompat)
    fun buildNotification(mediaSession: MediaSessionCompat): Notification
    fun clearNotifications()
}

class MediaNotificationsImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : MediaNotifications {

    private val notificationManager: NotificationManager = context.systemService(Context.NOTIFICATION_SERVICE)

    override fun updateNotification(mediaSession: MediaSessionCompat) {
        if (!PlayerService.IS_FOREGROUND) return
        GlobalScope.launch {
            notificationManager.notify(NOTIFICATION_ID, buildNotification(mediaSession))
        }
    }

    override fun buildNotification(mediaSession: MediaSessionCompat): Notification {
        if (mediaSession.controller.metadata == null || mediaSession.controller.playbackState == null) {
            return createEmptyNotification()
        }

        val albumName = mediaSession.controller.metadata.album
        val artistName = mediaSession.controller.metadata.artist
        val trackName = mediaSession.controller.metadata.title
        val artwork = mediaSession.controller.metadata.artwork
        val isPlaying = mediaSession.isPlaying()
        val isBuffering = mediaSession.isBuffering()
        val description = mediaSession.controller.metadata.displayDescription

        val pm: PackageManager = context.packageManager
        val nowPlayingIntent = pm.getLaunchIntentForPackage(context.packageName)
        val clickIntent = PendingIntent.getActivity(context, 0, nowPlayingIntent, FLAG_UPDATE_CURRENT or FLAG_MUTABLE)

        createNotificationChannel()

        val style = NotificationMediaCompat.MediaStyle()
            .setMediaSession(mediaSession.sessionToken)
            .setShowCancelButton(true)
            .setShowActionsInCompactView(0, 1, 2)
            .setCancelButtonIntent(buildMediaButtonPendingIntent(context, ACTION_STOP))

        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setStyle(style)
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setLargeIcon(artwork)
            setContentIntent(clickIntent)
            setContentTitle(trackName)
            setContentText("$artistName - $albumName")
            setSubText(description)
            setColorized(true)
            setShowWhen(false)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setDeleteIntent(buildMediaButtonPendingIntent(context, ACTION_STOP))
            addAction(getPreviousAction(context))
            if (isBuffering)
                addAction(getBufferingAction(context))
            else
                addAction(getPlayPauseAction(context, if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow))
            addAction(getNextAction(context))
            addAction(getStopAction(context))
        }

        if (artwork != null) {
            builder.color = Palette.from(artwork)
                .generate()
                .getDominantColor(Color.parseColor("#16053D"))
        }

        return builder.build()
    }

    override fun clearNotifications() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun getBufferingAction(context: Context): NotificationCompat.Action {
        return NotificationCompat.Action(R.drawable.ic_hourglass_empty, "", null)
    }

    private fun getStopAction(context: Context): NotificationCompat.Action {
        val actionIntent = Intent(context, PlayerService::class.java).apply { action = STOP_PLAYBACK }
        val pendingIntent = PendingIntent.getService(context, 0, actionIntent, FLAG_IMMUTABLE)
        return NotificationCompat.Action(R.drawable.ic_stop, "", pendingIntent)
    }

    private fun getPreviousAction(context: Context): NotificationCompat.Action {
        val actionIntent = Intent(context, PlayerService::class.java).apply { action = PREVIOUS }
        val pendingIntent = PendingIntent.getService(context, 0, actionIntent, FLAG_IMMUTABLE)
        return NotificationCompat.Action(R.drawable.ic_skip_previous, "", pendingIntent)
    }

    private fun getPlayPauseAction(
        context: Context,
        @DrawableRes playButtonResId: Int
    ): NotificationCompat.Action {
        val actionIntent = Intent(context, PlayerService::class.java).apply { action = PLAY_PAUSE }
        val pendingIntent = PendingIntent.getService(context, 0, actionIntent, FLAG_IMMUTABLE)
        return NotificationCompat.Action(playButtonResId, "", pendingIntent)
    }

    private fun getNextAction(context: Context): NotificationCompat.Action {
        val actionIntent = Intent(context, PlayerService::class.java).apply {
            action = NEXT
        }
        val pendingIntent = PendingIntent.getService(context, 0, actionIntent, FLAG_IMMUTABLE)
        return NotificationCompat.Action(R.drawable.ic_skip_next, "", pendingIntent)
    }

    private fun createEmptyNotification(): Notification {
        createNotificationChannel()
        return NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentTitle(context.getString(R.string.app_name))
            setColorized(true)
            setShowWhen(false)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }.build()
    }

    private fun createNotificationChannel() {
        if (!isOreo()) return
        val name = context.getString(R.string.app_name)
        val channel = NotificationChannel(CHANNEL_ID, name, IMPORTANCE_LOW).apply {
            description = context.getString(R.string.app_name)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }
}
