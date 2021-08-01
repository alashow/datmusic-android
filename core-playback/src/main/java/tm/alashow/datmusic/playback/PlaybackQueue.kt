/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import tm.alashow.datmusic.domain.entities.Audio

data class PlaybackQueue(val title: String = "Untitled", val list: List<String> = emptyList(), val currentId: String? = null)

fun fromMediaController(mediaController: MediaControllerCompat): PlaybackQueue? {
    return PlaybackQueue(
        title = mediaController.queueTitle?.toString().orEmpty(),
        list = mediaController.queue.mapNotNull { it.description.mediaId },
        currentId = mediaController.metadata?.getString(METADATA_KEY_MEDIA_ID)
    )
}

fun List<MediaSessionCompat.QueueItem>?.toMediaIdList(): List<String> {
    return this?.map { it.description.mediaId ?: "" } ?: emptyList()
}

fun List<Audio?>.toQueue(): List<MediaSessionCompat.QueueItem> {
    return filterNotNull().map {
        MediaSessionCompat.QueueItem(it.toDescription(), it.primaryKey.hashCode().toLong())
    }
}

fun Audio.toDescription(): MediaDescriptionCompat {
    return MediaDescriptionCompat.Builder()
        .setTitle(title)
        .setMediaId(id)
        .setSubtitle(artist)
        .setDescription(album)
        .setIconUri(coverUri()).build()
}

fun Audio.toMediaItem(): MediaBrowserCompat.MediaItem {
    return MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
            .setMediaId(MediaId(MEDIA_TYPE_AUDIO, id).toString())
            .setTitle(title)
            .setIconUri(coverUri())
            .setSubtitle(artist)
            .build(),
        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
    )
}

fun List<Audio>?.toMediaItems() = this?.map { it.toMediaItem() } ?: emptyList()
