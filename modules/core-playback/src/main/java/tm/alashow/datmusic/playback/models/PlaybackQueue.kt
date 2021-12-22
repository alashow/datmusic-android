/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.models

import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.session.MediaControllerCompat
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.AudioId
import tm.alashow.datmusic.playback.currentIndex

data class PlaybackQueue(
    val ids: List<AudioId> = emptyList(),
    val audios: List<Audio> = emptyList(),
    val title: String? = null,
    val initialMediaId: String = "",
    val currentIndex: Int = 0,
    val isIndexValid: Boolean = true
) : List<Audio> by audios {

    val isValid = ids.isNotEmpty() && audios.isNotEmpty() && currentIndex >= 0
    val isLastAudio = currentIndex == lastIndex

    val currentAudio get() = get(currentIndex)

    data class NowPlayingAudio(val audio: Audio, val index: Int) {
        companion object {

            fun from(queue: PlaybackQueue) = NowPlayingAudio(queue.currentAudio, queue.currentIndex)

            fun NowPlayingAudio?.isCurrentAudio(audio: Audio, audioIndex: Int? = null) =
                (this?.audio?.id == audio.id && (audioIndex == null || audioIndex == this.index))
        }
    }
}

fun fromMediaController(mediaController: MediaControllerCompat) = PlaybackQueue(
    title = mediaController.queueTitle?.toString(),
    ids = mediaController.queue.mapNotNull { it.description.mediaId },
    initialMediaId = mediaController.metadata?.getString(METADATA_KEY_MEDIA_ID) ?: "",
    currentIndex = mediaController.playbackState.currentIndex
)
