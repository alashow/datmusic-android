/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.models

import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.session.MediaControllerCompat

data class PlaybackQueue(val list: List<String> = emptyList(), val title: String? = null, val currentId: String? = null)

fun fromMediaController(mediaController: MediaControllerCompat) = PlaybackQueue(
    title = mediaController.queueTitle?.toString(),
    list = mediaController.queue.mapNotNull { it.description.mediaId },
    currentId = mediaController.metadata?.getString(METADATA_KEY_MEDIA_ID)
)
