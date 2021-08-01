/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.services

import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.core.os.bundleOf
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.datmusic.data.db.daos.AlbumsDao
import tm.alashow.datmusic.data.db.daos.ArtistsDao
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.playback.BY_UI_KEY
import tm.alashow.datmusic.playback.MediaId
import tm.alashow.datmusic.playback.MediaId.Companion.CALLER_OTHER
import tm.alashow.datmusic.playback.MediaId.Companion.CALLER_SELF
import tm.alashow.datmusic.playback.MediaNotificationsImpl
import tm.alashow.datmusic.playback.NEXT
import tm.alashow.datmusic.playback.NOTIFICATION_ID
import tm.alashow.datmusic.playback.PAUSE_ACTION
import tm.alashow.datmusic.playback.PLAY_ACTION
import tm.alashow.datmusic.playback.PLAY_PAUSE
import tm.alashow.datmusic.playback.PREVIOUS
import tm.alashow.datmusic.playback.isPlayEnabled
import tm.alashow.datmusic.playback.isPlaying
import tm.alashow.datmusic.playback.players.DatmusicPlayerImpl
import tm.alashow.datmusic.playback.receivers.BecomingNoisyReceiver
import tm.alashow.datmusic.playback.toAudioList
import tm.alashow.datmusic.playback.toMediaId
import tm.alashow.datmusic.playback.toMediaItems

@AndroidEntryPoint
class PlayerService : MediaBrowserServiceCompat(), CoroutineScope by MainScope() {

    companion object {
        var IS_RUNNING = false
    }

    @Inject
    protected lateinit var dispatchers: CoroutineDispatchers

    @Inject
    protected lateinit var datmusicPlayer: DatmusicPlayerImpl

    @Inject
    protected lateinit var mediaNotifications: MediaNotificationsImpl

    @Inject
    protected lateinit var audiosDao: AudiosDao

    @Inject
    protected lateinit var artistsDao: ArtistsDao

    @Inject
    protected lateinit var albumsDao: AlbumsDao

    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver

    override fun onCreate() {
        super.onCreate()

        sessionToken = datmusicPlayer.getSession().sessionToken
        becomingNoisyReceiver = BecomingNoisyReceiver(this, sessionToken!!)

        datmusicPlayer.onPlayingState { isPlaying, byUi ->
            if (isPlaying) {
                startForeground(NOTIFICATION_ID, mediaNotifications.buildNotification(getSession()))
                becomingNoisyReceiver.register()
            } else {
                becomingNoisyReceiver.unregister()
                stopForeground(byUi)
                mediaNotifications.updateNotification(getSession())
                launch { datmusicPlayer.saveQueueState() }
            }
            IS_RUNNING = isPlaying
        }

        datmusicPlayer.onMetaDataChanged {
            mediaNotifications.updateNotification(getSession())
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_STICKY
        }

        val mediaSession = datmusicPlayer.getSession()
        val controller = mediaSession.controller

        when (intent.action) {
            PLAY_PAUSE -> {
                controller.playbackState?.let { playbackState ->
                    when {
                        playbackState.isPlaying -> controller.transportControls.sendCustomAction(
                            PAUSE_ACTION,
                            bundleOf(BY_UI_KEY to false)
                        )
                        playbackState.isPlayEnabled -> controller.transportControls.sendCustomAction(
                            PLAY_ACTION,
                            bundleOf(BY_UI_KEY to false)
                        )
                    }
                }
            }
            NEXT -> controller.transportControls.skipToNext()
            PREVIOUS -> controller.transportControls.skipToPrevious()
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

    private fun loadChildren(parentId: String): MutableList<MediaBrowserCompat.MediaItem> {
        val list = mutableListOf<MediaBrowserCompat.MediaItem>()
        val mediaId = parentId.toMediaId()

        launch {
            list.addAll(mediaId.toAudioList(audiosDao, artistsDao, albumsDao).toMediaItems())
        }
        return list
    }
}
