/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.os.bundleOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.extensions.flowInterval
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.playback.models.MEDIA_TYPE_ALBUM
import tm.alashow.datmusic.playback.models.MEDIA_TYPE_ARTIST
import tm.alashow.datmusic.playback.models.MEDIA_TYPE_AUDIO
import tm.alashow.datmusic.playback.models.MEDIA_TYPE_AUDIO_MINERVA_QUERY
import tm.alashow.datmusic.playback.models.MEDIA_TYPE_AUDIO_QUERY
import tm.alashow.datmusic.playback.models.MediaId
import tm.alashow.datmusic.playback.models.PlaybackModeState
import tm.alashow.datmusic.playback.models.PlaybackProgressState
import tm.alashow.datmusic.playback.models.PlaybackQueue
import tm.alashow.datmusic.playback.models.QueueTitle
import tm.alashow.datmusic.playback.models.fromMediaController
import tm.alashow.datmusic.playback.players.QUEUE_LIST_KEY
import tm.alashow.datmusic.playback.players.QUEUE_MEDIA_ID_KEY
import tm.alashow.datmusic.playback.players.QUEUE_TITLE_KEY

const val PLAYBACK_PROGRESS_INTERVAL = 1000L

interface PlaybackConnection {
    val isConnected: MutableStateFlow<Boolean>
    val playbackState: MutableStateFlow<PlaybackStateCompat>
    val nowPlaying: MutableStateFlow<MediaMetadataCompat>
    val playbackQueue: MutableStateFlow<PlaybackQueue>

    val playbackProgress: MutableStateFlow<PlaybackProgressState>
    val playbackMode: MutableStateFlow<PlaybackModeState>

    val transportControls: MediaControllerCompat.TransportControls?
    var mediaController: MediaControllerCompat?

    fun playAudio(vararg audios: Audio, index: Int = 0, title: QueueTitle = QueueTitle())
    fun playArtist(artist: Artist, index: Int = 0)
    fun playAlbum(album: Album, index: Int = 0)
    fun playWithQuery(query: String, audioId: String)
    fun playWithMinervaQuery(query: String, audioId: String)
}

class PlaybackConnectionImpl(
    context: Context,
    serviceComponent: ComponentName
) : PlaybackConnection {

    override val isConnected = MutableStateFlow(false)
    override val playbackState = MutableStateFlow(NONE_PLAYBACK_STATE)
    override val nowPlaying = MutableStateFlow(NONE_PLAYING)
    override val playbackQueue = MutableStateFlow(PlaybackQueue())

    override val playbackProgress = MutableStateFlow(PlaybackProgressState())
    override val playbackMode = MutableStateFlow(PlaybackModeState())

    override var mediaController: MediaControllerCompat? = null
    override val transportControls: MediaControllerCompat.TransportControls?
        get() = mediaController?.transportControls

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)
    private val mediaBrowser = MediaBrowserCompat(
        context,
        serviceComponent,
        mediaBrowserConnectionCallback, null
    ).apply { connect() }

    private var playbackInterval: Job = Job()

    init {
        MainScope().launch {
            combine(playbackState, nowPlaying, ::Pair).collect { (state, current) ->
                playbackInterval.cancel()
                val duration = current.duration
                val position = state.position

                if (duration < 1) return@collect

                val initial = PlaybackProgressState(duration, position)
                playbackProgress.value = initial

                if (state.isPlaying && !state.isBuffering)
                    playbackInterval = launch {
                        flowInterval(PLAYBACK_PROGRESS_INTERVAL).collect { ticks ->
                            playbackProgress.value = initial.copy(elapsed = PLAYBACK_PROGRESS_INTERVAL * (ticks + 1))
                        }
                    }
            }
        }
    }

    override fun playAudio(vararg audios: Audio, index: Int, title: QueueTitle) {
        val audio = audios[index]
        transportControls?.playFromMediaId(
            MediaId(MEDIA_TYPE_AUDIO, audio.id).toString(),
            Bundle().apply {
                putStringArray(QUEUE_LIST_KEY, audios.map { it.id }.toTypedArray())
                putString(QUEUE_TITLE_KEY, title.toString())
            }
        )
    }

    override fun playArtist(artist: Artist, index: Int) {
        transportControls?.playFromMediaId(MediaId(MEDIA_TYPE_ARTIST, artist.id, index).toString(), null)
    }

    override fun playAlbum(album: Album, index: Int) {
        transportControls?.playFromMediaId(MediaId(MEDIA_TYPE_ALBUM, album.id, index).toString(), null)
    }

    override fun playWithQuery(query: String, audioId: String) {
        transportControls?.playFromMediaId(
            MediaId(MEDIA_TYPE_AUDIO_QUERY, query, -1).toString(),
            bundleOf(
                QUEUE_MEDIA_ID_KEY to audioId
            )
        )
    }

    override fun playWithMinervaQuery(query: String, audioId: String) {
        transportControls?.playFromMediaId(
            MediaId(MEDIA_TYPE_AUDIO_MINERVA_QUERY, query, -1).toString(),
            bundleOf(
                QUEUE_MEDIA_ID_KEY to audioId
            )
        )
    }

    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }

            isConnected.value = true
        }

        override fun onConnectionSuspended() {
            isConnected.value = false
        }

        override fun onConnectionFailed() {
            isConnected.value = false
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackState.value = state ?: return
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            nowPlaying.value = metadata ?: return
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
            Timber.d("New queue: size=${queue?.size}, $queue")
            val newQueue = fromMediaController(mediaController ?: return)
            this@PlaybackConnectionImpl.playbackQueue.value = newQueue
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            playbackMode.value = playbackMode.value.copy(repeatMode = repeatMode)
        }

        override fun onShuffleModeChanged(shuffleMode: Int) {
            playbackMode.value = playbackMode.value.copy(shuffleMode = shuffleMode)
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}
