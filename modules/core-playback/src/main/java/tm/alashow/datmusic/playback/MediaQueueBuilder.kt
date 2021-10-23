/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.playback

import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import tm.alashow.datmusic.data.DatmusicSearchParams
import tm.alashow.datmusic.data.DatmusicSearchParams.Companion.withTypes
import tm.alashow.datmusic.data.db.daos.AlbumsDao
import tm.alashow.datmusic.data.db.daos.ArtistsDao
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.playback.models.MEDIA_TYPE_ALBUM
import tm.alashow.datmusic.playback.models.MEDIA_TYPE_ARTIST
import tm.alashow.datmusic.playback.models.MEDIA_TYPE_AUDIO
import tm.alashow.datmusic.playback.models.MEDIA_TYPE_AUDIO_FLACS_QUERY
import tm.alashow.datmusic.playback.models.MEDIA_TYPE_AUDIO_MINERVA_QUERY
import tm.alashow.datmusic.playback.models.MEDIA_TYPE_AUDIO_QUERY
import tm.alashow.datmusic.playback.models.MEDIA_TYPE_DOWNLOADS
import tm.alashow.datmusic.playback.models.MEDIA_TYPE_PLAYLIST
import tm.alashow.datmusic.playback.models.MediaId
import tm.alashow.datmusic.playback.models.QueueTitle

class MediaQueueBuilder @Inject constructor(
    private val audiosDao: AudiosDao,
    private val artistsDao: ArtistsDao,
    private val albumsDao: AlbumsDao,
    private val playlistsRepo: PlaylistsRepo,
    private val downloader: Downloader
) {
    suspend fun buildAudioList(source: MediaId): List<Audio> = with(source) {
        when (type) {
            MEDIA_TYPE_AUDIO -> listOfNotNull(audiosDao.entry(value).firstOrNull())
            MEDIA_TYPE_ALBUM -> albumsDao.entry(value).firstOrNull()?.audios
            MEDIA_TYPE_ARTIST -> artistsDao.entry(value).firstOrNull()?.audios
            MEDIA_TYPE_PLAYLIST -> playlistsRepo.playlistWithAudios(value.toLong()).firstOrNull()?.audios
            MEDIA_TYPE_DOWNLOADS -> downloader.downloadRequests.first().audios.map { it.audio }
            MEDIA_TYPE_AUDIO_QUERY, MEDIA_TYPE_AUDIO_MINERVA_QUERY, MEDIA_TYPE_AUDIO_FLACS_QUERY -> {
                val params = DatmusicSearchParams(value).run {
                    when (type) {
                        MEDIA_TYPE_AUDIO_MINERVA_QUERY -> withTypes(DatmusicSearchParams.BackendType.MINERVA)
                        MEDIA_TYPE_AUDIO_FLACS_QUERY -> withTypes(DatmusicSearchParams.BackendType.FLACS)
                        else -> this
                    }
                }
                audiosDao.entries(params).first()
            }
            else -> null
        }.orEmpty()
    }

    suspend fun buildQueueTitle(source: MediaId): QueueTitle = with(source) {
        when (type) {
            MEDIA_TYPE_AUDIO -> QueueTitle(this, QueueTitle.Type.AUDIO, audiosDao.entry(value).firstOrNull()?.title)
            MEDIA_TYPE_ARTIST -> QueueTitle(this, QueueTitle.Type.ARTIST, artistsDao.entry(value).firstOrNull()?.name)
            MEDIA_TYPE_ALBUM -> QueueTitle(this, QueueTitle.Type.ALBUM, albumsDao.entry(value).firstOrNull()?.title)
            MEDIA_TYPE_PLAYLIST -> QueueTitle(
                this,
                QueueTitle.Type.PLAYLIST,
                playlistsRepo.playlistWithAudios(value.toLong()).firstOrNull()?.playlist?.name
            )
            MEDIA_TYPE_DOWNLOADS -> QueueTitle(this, QueueTitle.Type.DOWNLOADS)
            MEDIA_TYPE_AUDIO_QUERY, MEDIA_TYPE_AUDIO_MINERVA_QUERY, MEDIA_TYPE_AUDIO_FLACS_QUERY -> QueueTitle(this, QueueTitle.Type.SEARCH, value)
            else -> QueueTitle()
        }
    }
}
