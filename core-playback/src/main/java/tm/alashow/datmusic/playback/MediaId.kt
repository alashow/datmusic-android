/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback

import kotlinx.coroutines.flow.firstOrNull
import tm.alashow.datmusic.data.db.daos.AlbumsDao
import tm.alashow.datmusic.data.db.daos.ArtistsDao
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.domain.entities.Audio

const val MEDIA_TYPE_AUDIO = "Media.Audio"
const val MEDIA_TYPE_ARTIST = "Media.Artist"
const val MEDIA_TYPE_ALBUM = "Media.Album"

data class MediaId(
    val type: String = MEDIA_TYPE_AUDIO,
    val id: String = "0",
    val index: Int = 0,
    val caller: String = CALLER_SELF
) {

    companion object {
        const val CALLER_SELF = "self"
        const val CALLER_OTHER = "other"
    }

    // var mediaItem: MediaBrowserCompat.MediaItem? = null

    override fun toString(): String {
        return "$type | $id | $index | $caller"
    }
}

fun String.toMediaId(): MediaId {
    val parts = split("|")
    return if (parts.size > 1)
        MediaId(parts[0].trim(), parts[1].trim(), parts[2].trim().toInt(), parts[3].trim())
    else MediaId()
}

suspend fun MediaId.toAudioList(audiosDao: AudiosDao, artistsDao: ArtistsDao, albumsDao: AlbumsDao): List<Audio>? = when (type) {
    MEDIA_TYPE_AUDIO -> listOfNotNull(audiosDao.entry(id).firstOrNull())
    MEDIA_TYPE_ALBUM -> albumsDao.entry(id).firstOrNull()?.audios
    MEDIA_TYPE_ARTIST -> artistsDao.entry(id).firstOrNull()?.audios
    else -> emptyList()
}

suspend fun MediaId.toQueueTitle(audiosDao: AudiosDao, artistsDao: ArtistsDao, albumsDao: AlbumsDao): String? = when (type) {
    MEDIA_TYPE_AUDIO -> audiosDao.entry(id).firstOrNull()?.title
    MEDIA_TYPE_ALBUM -> albumsDao.entry(id).firstOrNull()?.title
    MEDIA_TYPE_ARTIST -> artistsDao.entry(id).firstOrNull()?.name
    else -> null
}
