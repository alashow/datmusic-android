/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.models

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.session.MediaControllerCompat
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.playback.id

data class PlaybackQueue(
    val list: List<String> = emptyList(),
    val audiosList: List<Audio> = emptyList(),
    val title: String? = null,
    val initialMediaId: String = "",
) {
    fun findAudio(media: MediaMetadataCompat): Pair<Int, Audio>? = audiosList.indexOfFirst { it.id == media.id.toMediaId().value }
        .let { index ->
            if (index < 0) return null
            return index to audiosList[index]
        }
}

fun fromMediaController(mediaController: MediaControllerCompat) = PlaybackQueue(
    title = mediaController.queueTitle?.toString(),
    list = mediaController.queue.mapNotNull { it.description.mediaId },
    initialMediaId = mediaController.metadata?.getString(METADATA_KEY_MEDIA_ID) ?: "",
)
