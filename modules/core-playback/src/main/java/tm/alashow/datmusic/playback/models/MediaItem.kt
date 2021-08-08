/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.models

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import tm.alashow.datmusic.domain.entities.Audio

fun List<MediaSessionCompat.QueueItem>?.toMediaIdList(): List<MediaId> {
    return this?.map { it.description.mediaId?.toMediaId() ?: MediaId() } ?: emptyList()
}

fun List<String>.toMediaIds(): List<MediaId> {
    return this.map { it.toMediaId() }
}

fun List<String>.toMediaAudioIds(): List<String> {
    return this.map { it.toMediaId().value }
}

fun List<Audio?>.toQueueItems(): List<MediaSessionCompat.QueueItem> {
    return filterNotNull().mapIndexed { index, audio ->
        MediaSessionCompat.QueueItem(audio.toMediaDescription(), (audio.id + index).hashCode().toLong())
    }
}

fun Audio.toMediaDescription(): MediaDescriptionCompat {
    return MediaDescriptionCompat.Builder()
        .setTitle(title)
        .setMediaId(MediaId(MEDIA_TYPE_AUDIO, id).toString())
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
