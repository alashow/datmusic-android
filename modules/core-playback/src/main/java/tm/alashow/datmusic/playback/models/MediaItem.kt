/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.models

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import tm.alashow.datmusic.domain.CoverImageSize
import tm.alashow.datmusic.domain.UNKNOWN_ARTIST
import tm.alashow.datmusic.domain.UNTITLED_SONG
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.mainArtist
import tm.alashow.datmusic.playback.album
import tm.alashow.datmusic.playback.artist
import tm.alashow.datmusic.playback.artworkUri
import tm.alashow.datmusic.playback.duration
import tm.alashow.datmusic.playback.id
import tm.alashow.datmusic.playback.title

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

fun Audio.toMediaMetadata(builder: MediaMetadataCompat.Builder): MediaMetadataCompat.Builder = builder.apply {
    putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
    putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
    putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
    putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, MediaId(MEDIA_TYPE_AUDIO, id).toString())
    putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMillis())
    putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, coverUri(CoverImageSize.LARGE).toString())
    putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null)
}

fun MediaMetadataCompat.toAudio() = Audio(
    id = id.toMediaId().value,
    artist = artist ?: UNKNOWN_ARTIST,
    title = title ?: UNTITLED_SONG,
    duration = (duration / 1000).toInt(),
    coverUrl = artworkUri.toString()
)

fun MediaMetadataCompat.toArtistSearchQuery() = "${artist?.mainArtist()}"
fun MediaMetadataCompat.toAlbumSearchQuery() = "${artist?.mainArtist()} $album"
