/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.services

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.datmusic.playback.MediaNotificationsImpl
import tm.alashow.datmusic.playback.MediaQueueBuilder
import tm.alashow.datmusic.playback.NEXT
import tm.alashow.datmusic.playback.NOTIFICATION_ID
import tm.alashow.datmusic.playback.PLAY_PAUSE
import tm.alashow.datmusic.playback.PREVIOUS
import tm.alashow.datmusic.playback.STOP_PLAYBACK
import tm.alashow.datmusic.playback.isIdle
import tm.alashow.datmusic.playback.models.MediaId
import tm.alashow.datmusic.playback.models.MediaId.Companion.CALLER_OTHER
import tm.alashow.datmusic.playback.models.MediaId.Companion.CALLER_SELF
import tm.alashow.datmusic.playback.models.toMediaId
import tm.alashow.datmusic.playback.models.toMediaItems
import tm.alashow.datmusic.playback.playPause
import tm.alashow.datmusic.playback.players.DatmusicPlayerImpl
import tm.alashow.datmusic.playback.receivers.BecomingNoisyReceiver

@AndroidEntryPoint
class PlayerService : MediaBrowserServiceCompat(), CoroutineScope by MainScope() {

    companion object {
        var IS_FOREGROUND = false
    }

    @Inject
    protected lateinit var dispatchers: CoroutineDispatchers

    @Inject
    protected lateinit var datmusicPlayer: DatmusicPlayerImpl

    @Inject
    protected lateinit var mediaNotifications: MediaNotificationsImpl

    @Inject
    protected lateinit var mediaQueueBuilder: MediaQueueBuilder

    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver

    override fun onCreate() {
        super.onCreate()

        sessionToken = datmusicPlayer.getSession().sessionToken
        becomingNoisyReceiver = BecomingNoisyReceiver(this, sessionToken!!)

        datmusicPlayer.onPlayingState { isPlaying, byUi ->
            val isIdle = datmusicPlayer.getSession().controller.playbackState.isIdle
            if (!isPlaying && isIdle) {
                pauseForeground(byUi)
                mediaNotifications.clearNotifications()
            } else {
                startForeground()
            }

            mediaNotifications.updateNotification(getSession())
        }

        datmusicPlayer.onMetaDataChanged {
            mediaNotifications.updateNotification(getSession())
        }
    }

    private fun startForeground() {
        if (IS_FOREGROUND) {
            Timber.w("Tried to start foreground, but was already in foreground")
            return
        }
        Timber.d("Starting foreground service")
        startForeground(NOTIFICATION_ID, mediaNotifications.buildNotification(datmusicPlayer.getSession()))
        becomingNoisyReceiver.register()
        IS_FOREGROUND = true
    }

    private fun pauseForeground(removeNotification: Boolean) {
        if (!IS_FOREGROUND) {
            Timber.w("Tried to stop foreground, but was already NOT in foreground")
            return
        }
        Timber.d("Stopping foreground service")
        becomingNoisyReceiver.unregister()
        stopForeground(removeNotification)
        IS_FOREGROUND = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_STICKY
        }

        val mediaSession = datmusicPlayer.getSession()
        val controller = mediaSession.controller

        when (intent.action) {
            PLAY_PAUSE -> controller.playPause()
            NEXT -> controller.transportControls.skipToNext()
            PREVIOUS -> controller.transportControls.skipToPrevious()
            STOP_PLAYBACK -> controller.transportControls.stop()
        }

        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return START_STICKY
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        val caller = if (clientPackageName == applicationContext.packageName) CALLER_SELF else CALLER_OTHER
        return BrowserRoot(MediaId("-1", caller = caller).toString(), null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.detach()
        launch {
            val itemList = withContext(dispatchers.io) { loadChildren(parentId) }
            result.sendResult(itemList)
        }
    }

    private suspend fun loadChildren(parentId: String): MutableList<MediaBrowserCompat.MediaItem> {
        val list = mutableListOf<MediaBrowserCompat.MediaItem>()
        val mediaId = parentId.toMediaId()
        list.addAll(mediaQueueBuilder.buildAudioList(mediaId).toMediaItems())
        return list
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        launch {
            datmusicPlayer.pause()
            datmusicPlayer.saveQueueState()
            datmusicPlayer.stop(byUser = false)
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        launch {
            datmusicPlayer.saveQueueState()
            datmusicPlayer.release()
        }
    }
}
