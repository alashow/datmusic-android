/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.models

import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.session.MediaControllerCompat
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.playback.currentIndex

data class PlaybackQueue(
    val list: List<String> = emptyList(),
    val audiosList: List<Audio> = emptyList(),
    val title: String? = null,
    val initialMediaId: String = "",
    val currentIndex: Int = 0,
) : List<Audio> by audiosList {

    val isValid = list.isNotEmpty() && audiosList.isNotEmpty() && currentIndex >= 0
}

fun fromMediaController(mediaController: MediaControllerCompat) = PlaybackQueue(
    title = mediaController.queueTitle?.toString(),
    list = mediaController.queue.mapNotNull { it.description.mediaId },
    initialMediaId = mediaController.metadata?.getString(METADATA_KEY_MEDIA_ID) ?: "",
    currentIndex = mediaController.playbackState.currentIndex
)
