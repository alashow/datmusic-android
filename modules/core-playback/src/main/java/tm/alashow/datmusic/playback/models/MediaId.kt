/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback.models

import kotlinx.serialization.Serializable
import timber.log.Timber

const val MEDIA_TYPE_AUDIO = "Media.Audio"
const val MEDIA_TYPE_ARTIST = "Media.Artist"
const val MEDIA_TYPE_DOWNLOADS = "Media.Downloads"
const val MEDIA_TYPE_PLAYLIST = "Media.Playlist"
const val MEDIA_TYPE_ALBUM = "Media.Album"
const val MEDIA_TYPE_AUDIO_QUERY = "Media.AudioQuery"
const val MEDIA_TYPE_AUDIO_MINERVA_QUERY = "Media.AudioMinervaQuery"
const val MEDIA_TYPE_AUDIO_FLACS_QUERY = "Media.AudioFlacsQuery"

const val MEDIA_ID_INDEX_SHUFFLED = -1000

private const val MEDIA_ID_SEPARATOR = " | "

@Serializable
data class MediaId(
    val type: String = MEDIA_TYPE_AUDIO,
    val value: String = "0",
    val index: Int = -1,
    val caller: String = CALLER_SELF
) {

    val hasIndex = index >= 0
    val isShuffleIndex = index == MEDIA_ID_INDEX_SHUFFLED

    companion object {
        const val CALLER_SELF = "self"
        const val CALLER_OTHER = "other"
    }

    override fun toString(): String {
        return type +
            MEDIA_ID_SEPARATOR + value +
            MEDIA_ID_SEPARATOR + index +
            MEDIA_ID_SEPARATOR + caller
    }
}

fun String?.toMediaId(): MediaId {
    if (this == null)
        return MediaId()

    val parts = split(MEDIA_ID_SEPARATOR)
    val type = parts[0]

    val knownTypes = listOf(
        MEDIA_TYPE_AUDIO, MEDIA_TYPE_ARTIST,
        MEDIA_TYPE_ALBUM, MEDIA_TYPE_AUDIO_QUERY,
        MEDIA_TYPE_AUDIO_MINERVA_QUERY, MEDIA_TYPE_AUDIO_FLACS_QUERY,
        MEDIA_TYPE_PLAYLIST, MEDIA_TYPE_DOWNLOADS
    )
    if (type !in knownTypes) {
        Timber.e("Unknown media type: $type")
        return MediaId()
    }

    return if (parts.size > 1)
        MediaId(type, parts[1], parts[2].toInt(), parts[3])
    else MediaId()
}
