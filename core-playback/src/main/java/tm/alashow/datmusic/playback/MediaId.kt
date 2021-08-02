/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import tm.alashow.datmusic.data.db.daos.AlbumsDao
import tm.alashow.datmusic.data.db.daos.ArtistsDao
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams.Companion.withTypes
import tm.alashow.datmusic.domain.entities.Audio

const val MEDIA_TYPE_AUDIO = "Media.Audio"
const val MEDIA_TYPE_ARTIST = "Media.Artist"
const val MEDIA_TYPE_ALBUM = "Media.Album"
const val MEDIA_TYPE_AUDIO_QUERY = "Media.AudioQuery"
const val MEDIA_TYPE_AUDIO_MINERVA_QUERY = "Media.AudioMinervaQuery"

data class MediaId(
    val type: String = MEDIA_TYPE_AUDIO,
    val value: String = "0",
    val index: Int = 0,
    val caller: String = CALLER_SELF
) {

    companion object {
        const val CALLER_SELF = "self"
        const val CALLER_OTHER = "other"
    }

    // var mediaItem: MediaBrowserCompat.MediaItem? = null

    override fun toString(): String {
        return "$type | $value | $index | $caller"
    }
}

fun String.toMediaId(): MediaId {
    val parts = split("|")
    val type = parts[0].trim()

    if (type !in listOf(MEDIA_TYPE_AUDIO, MEDIA_TYPE_ARTIST, MEDIA_TYPE_ALBUM, MEDIA_TYPE_AUDIO_QUERY, MEDIA_TYPE_AUDIO_MINERVA_QUERY)) {
        Timber.e("Unknown media type: $type")
        return MediaId()
    }

    return if (parts.size > 1)
        MediaId(type, parts[1].trim(), parts[2].trim().toInt(), parts[3].trim())
    else MediaId()
}

suspend fun MediaId.toAudioList(audiosDao: AudiosDao, artistsDao: ArtistsDao, albumsDao: AlbumsDao):
    List<Audio>? = when (type) {
    MEDIA_TYPE_AUDIO -> listOfNotNull(audiosDao.entry(value).firstOrNull())
    MEDIA_TYPE_ALBUM -> albumsDao.entry(value).firstOrNull()?.audios
    MEDIA_TYPE_ARTIST -> artistsDao.entry(value).firstOrNull()?.audios
    MEDIA_TYPE_AUDIO_QUERY, MEDIA_TYPE_AUDIO_MINERVA_QUERY -> {
        val params = DatmusicSearchParams(value).run {
            when (type == MEDIA_TYPE_AUDIO_MINERVA_QUERY) {
                true -> withTypes(DatmusicSearchParams.BackendType.MINERVA)
                else -> this
            }
        }
        audiosDao.entries(params).first()
    }
    else -> emptyList()
}

suspend fun MediaId.toQueueTitle(audiosDao: AudiosDao, artistsDao: ArtistsDao, albumsDao: AlbumsDao): String? = when (type) {
    MEDIA_TYPE_AUDIO -> audiosDao.entry(value).firstOrNull()?.title
    MEDIA_TYPE_ALBUM -> albumsDao.entry(value).firstOrNull()?.title
    MEDIA_TYPE_ARTIST -> artistsDao.entry(value).firstOrNull()?.name
    MEDIA_TYPE_AUDIO_QUERY, MEDIA_TYPE_AUDIO_MINERVA_QUERY -> value
    else -> null
}
