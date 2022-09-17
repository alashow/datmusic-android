/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.previews

import android.content.Context
import android.graphics.BitmapFactory
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.domain.entities.AlbumId
import tm.alashow.datmusic.domain.entities.ArtistId
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.AudioId
import tm.alashow.datmusic.domain.entities.PlaylistId
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.models.PlaybackModeState
import tm.alashow.datmusic.playback.models.PlaybackProgressState
import tm.alashow.datmusic.playback.models.PlaybackQueue
import tm.alashow.datmusic.playback.models.QueueTitle
import tm.alashow.datmusic.ui.R

lateinit var previewPlaybackConnectionInstance: PlaybackConnection

@Composable
internal fun previewPlaybackConnection(): PlaybackConnection {
    if (!::previewPlaybackConnectionInstance.isInitialized) {
        previewPlaybackConnectionInstance = PreviewPlaybackConnection(LocalContext.current)
    }
    return previewPlaybackConnectionInstance
}

private class PreviewPlaybackConnection constructor(context: Context) : PlaybackConnection {
    private val previewPlaybackState = MutableStateFlow(
        PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PLAYING, 4000, 1.0f)
            .build()
    )
    private val previewPlaybackNowPlaying = MutableStateFlow(
        MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Artist")
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Title")
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Album")
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "id")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 40000)
            .putBitmap(
                MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                BitmapFactory.decodeResource(context.resources, R.drawable.preview_artwork)
            )
            .build()
    )

    private val previewPlaybackQueue = MutableStateFlow(
        PlaybackQueue(audios = SampleData.list(20) { audio() })
            .let { it.copy(ids = it.audios.map { it.id }) }
    )

    private val previewPlaybackProgress = MutableStateFlow(
        PlaybackProgressState(
            total = 148000,
            lastPosition = 23255,
            elapsed = 12134,
            buffered = 52000,
        )
    )

    override val playbackState: StateFlow<PlaybackStateCompat> = previewPlaybackState
    override val nowPlaying: StateFlow<MediaMetadataCompat> = previewPlaybackNowPlaying
    override val playbackQueue: StateFlow<PlaybackQueue> = previewPlaybackQueue
    override val playbackProgress: StateFlow<PlaybackProgressState> = previewPlaybackProgress

    override val isConnected: StateFlow<Boolean> = MutableStateFlow(true)
    override val nowPlayingAudio: StateFlow<PlaybackQueue.NowPlayingAudio?> = MutableStateFlow(null)
    override val playbackMode: StateFlow<PlaybackModeState> = MutableStateFlow(PlaybackModeState())
    override val mediaController: MediaControllerCompat? = null
    override val transportControls: MediaControllerCompat.TransportControls? = null

    override fun playAudio(audio: Audio, title: QueueTitle) {}
    override fun playAudios(audios: List<Audio>, index: Int, title: QueueTitle) {}
    override fun playArtist(artistId: ArtistId, index: Int) {}
    override fun playPlaylist(playlistId: PlaylistId, index: Int, queue: List<AudioId>) {}
    override fun playAlbum(albumId: AlbumId, index: Int) {}
    override fun playFromDownloads(index: Int, queue: List<AudioId>) {}
    override fun playWithQuery(query: String, audioId: String) {}
    override fun playWithMinervaQuery(query: String, audioId: String) {}
    override fun playWithFlacsQuery(query: String, audioId: String) {}
    override fun swapQueue(from: Int, to: Int) {}
    override fun removeById(id: String) {}

    override fun playNextAudio(audio: Audio) {
        previewPlaybackQueue.value = previewPlaybackQueue.value.copy(
            audios = previewPlaybackQueue.value.audios + audio,
            ids = previewPlaybackQueue.value.ids + audio.id
        )
    }

    override fun removeByPosition(position: Int) {
        previewPlaybackQueue.value = previewPlaybackQueue.value.copy(
            audios = previewPlaybackQueue.value.audios.toMutableList().apply { removeAt(position) },
            ids = previewPlaybackQueue.value.ids.toMutableList().apply { removeAt(position) },
        )
    }
}
