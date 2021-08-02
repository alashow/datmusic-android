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
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.playback.players.QUEUE_LIST_KEY
import tm.alashow.datmusic.playback.players.QUEUE_MEDIA_ID_KEY
import tm.alashow.datmusic.playback.players.QUEUE_TITLE_KEY

interface PlaybackConnection {
    val isConnected: MutableStateFlow<Boolean>
    val playbackState: MutableStateFlow<PlaybackStateCompat>
    val nowPlaying: MutableStateFlow<MediaMetadataCompat>
    val playbackQueue: MutableStateFlow<PlaybackQueue>
    val transportControls: MediaControllerCompat.TransportControls?
    var mediaController: MediaControllerCompat?

    fun playAudio(vararg audios: Audio, index: Int = 0)
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

    override var mediaController: MediaControllerCompat? = null

    override val transportControls: MediaControllerCompat.TransportControls?
        get() = mediaController?.transportControls

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)
    private val mediaBrowser = MediaBrowserCompat(
        context,
        serviceComponent,
        mediaBrowserConnectionCallback, null
    ).apply { connect() }

    override fun playAudio(vararg audios: Audio, index: Int) {
        val audio = audios[index]
        transportControls?.playFromMediaId(
            MediaId(MEDIA_TYPE_AUDIO, audio.id).toString(),
            Bundle().apply {
                putStringArray(QUEUE_LIST_KEY, audios.map { it.id }.toTypedArray())
                putString(QUEUE_TITLE_KEY, audio.title)
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
            Timber.d("onQueueChanged: $queue, mediaController=${mediaController != null}")
            val newQueue = fromMediaController(mediaController ?: return)
            this@PlaybackConnectionImpl.playbackQueue.value = newQueue ?: return
            Timber.d("Queue set: $newQueue")
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}
