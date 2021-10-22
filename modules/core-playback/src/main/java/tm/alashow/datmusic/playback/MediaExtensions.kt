/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback

import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import timber.log.Timber
import tm.alashow.datmusic.playback.players.QUEUE_CURRENT_INDEX
import tm.alashow.datmusic.playback.players.QUEUE_HAS_NEXT
import tm.alashow.datmusic.playback.players.QUEUE_HAS_PREVIOUS

val NONE_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
    .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
    .build()

val NONE_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
    .build()

fun MediaControllerCompat.playPause() {
    playbackState?.let {
        when {
            it.isPlaying -> transportControls?.sendCustomAction(PAUSE_ACTION, bundleOf(BY_UI_KEY to false))
            it.isPlayEnabled -> transportControls?.sendCustomAction(PLAY_ACTION, bundleOf(BY_UI_KEY to false))
            else -> Timber.d("Couldn't play or pause the media controller")
        }
    }
}

fun MediaControllerCompat.toggleShuffleMode() {
    val new = when (shuffleMode) {
        PlaybackStateCompat.SHUFFLE_MODE_NONE -> PlaybackStateCompat.SHUFFLE_MODE_ALL
        PlaybackStateCompat.SHUFFLE_MODE_ALL -> PlaybackStateCompat.SHUFFLE_MODE_NONE
        else -> {
            Timber.e("Unknown shuffle mode $shuffleMode")
            return
        }
    }
    Timber.i("Toggling shuffle mode from=$shuffleMode, to=$new")
    transportControls.setShuffleMode(new)
}

fun MediaControllerCompat.toggleRepeatMode() {
    transportControls.setRepeatMode(
        when (repeatMode) {
            PlaybackStateCompat.REPEAT_MODE_NONE -> PlaybackStateCompat.REPEAT_MODE_ALL
            PlaybackStateCompat.REPEAT_MODE_ALL -> PlaybackStateCompat.REPEAT_MODE_ONE
            else -> PlaybackStateCompat.REPEAT_MODE_NONE
        }
    )
}

fun createDefaultPlaybackState(): PlaybackStateCompat.Builder {
    return PlaybackStateCompat.Builder().setActions(
        PlaybackStateCompat.ACTION_PLAY
            or PlaybackStateCompat.ACTION_PAUSE
            or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
            or PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
            or PlaybackStateCompat.ACTION_PLAY_PAUSE
            or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            or PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
            or PlaybackStateCompat.ACTION_SET_REPEAT_MODE
            or PlaybackStateCompat.ACTION_SEEK_TO
    )
}

fun MediaSessionCompat.position(): Long {
    return controller.playbackState.position
}

fun MediaSessionCompat.isPlaying(): Boolean {
    return controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING
}

fun MediaSessionCompat.isBuffering(): Boolean {
    return controller.playbackState.state == PlaybackStateCompat.STATE_BUFFERING
}

inline val MediaSessionCompat.repeatMode
    get() = controller.repeatMode

inline val MediaSessionCompat.shuffleMode
    get() = controller.shuffleMode

inline val Pair<PlaybackStateCompat, MediaMetadataCompat>.isActive
    get() = (first.state != PlaybackStateCompat.STATE_NONE && second != NONE_PLAYING)

inline val PlaybackStateCompat.isPrepared
    get() = (state == PlaybackStateCompat.STATE_BUFFERING) ||
        (state == PlaybackStateCompat.STATE_PLAYING) ||
        (state == PlaybackStateCompat.STATE_PAUSED)

inline val PlaybackStateCompat.isPlaying
    get() = (state == PlaybackStateCompat.STATE_PLAYING) || isBuffering

inline val PlaybackStateCompat.isBuffering
    get() = (state == PlaybackStateCompat.STATE_BUFFERING)

inline val PlaybackStateCompat.isStopped
    get() = (state == PlaybackStateCompat.STATE_STOPPED)

inline val PlaybackStateCompat.isIdle
    get() = (state == PlaybackStateCompat.STATE_NONE || state == PlaybackStateCompat.STATE_STOPPED)

inline val PlaybackStateCompat.isError
    get() = (state == PlaybackStateCompat.STATE_ERROR)

inline val PlaybackStateCompat.isPlayEnabled
    get() = (actions and PlaybackStateCompat.ACTION_PLAY != 0L) ||
        (
            (actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L) &&
                (state == PlaybackStateCompat.STATE_PAUSED)
            )

inline val PlaybackStateCompat.currentIndex
    get() = (extras?.getInt(QUEUE_CURRENT_INDEX) ?: 0)

inline val PlaybackStateCompat.hasPrevious
    get() = (extras?.getBoolean(QUEUE_HAS_PREVIOUS) ?: false)

inline val PlaybackStateCompat.hasNext
    get() = (extras?.getBoolean(QUEUE_HAS_NEXT) ?: true)

inline val MediaMetadataCompat.id: String? get() = getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)

inline val MediaMetadataCompat.title: String? get() = getString(MediaMetadataCompat.METADATA_KEY_TITLE)

inline val MediaMetadataCompat.artist: String? get() = getString(MediaMetadataCompat.METADATA_KEY_ARTIST)

inline val MediaMetadataCompat.duration: Long get() = getLong(MediaMetadataCompat.METADATA_KEY_DURATION)

inline val MediaMetadataCompat.album: String? get() = getString(MediaMetadataCompat.METADATA_KEY_ALBUM)

inline val MediaMetadataCompat.displayDescription: String? get() = getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION)

inline val MediaMetadataCompat.artwork: Bitmap? get() = getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)
inline val MediaMetadataCompat.artworkUri: Uri get() = (getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI) ?: "").toUri()
