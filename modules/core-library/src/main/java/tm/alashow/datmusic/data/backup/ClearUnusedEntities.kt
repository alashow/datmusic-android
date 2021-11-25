/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.backup

import javax.inject.Inject
import timber.log.Timber
import tm.alashow.datmusic.data.db.daos.AlbumsDao
import tm.alashow.datmusic.data.db.daos.ArtistsDao
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.data.db.daos.DownloadRequestsDao
import tm.alashow.datmusic.data.db.daos.PlaylistsWithAudiosDao
import tm.alashow.datmusic.domain.entities.DownloadRequest

class ClearUnusedEntities @Inject constructor(
    private val audiosDao: AudiosDao,
    private val artistsDao: ArtistsDao,
    private val albumsDao: AlbumsDao,
    private val downloadsRequestsDao: DownloadRequestsDao,
    private val playlistWithAudios: PlaylistsWithAudiosDao,
) {
    /**
     * Delete all audios except the ones are in downloads and delete all artists/albums.
     * To be modified to not delete downloaded audios artists/albums in the future.
     */
    suspend operator fun invoke() {
        val downloadRequestAudios = downloadsRequestsDao.getByType(DownloadRequest.Type.Audio)
        val downloadedAudioIds = downloadRequestAudios.map { it.id }
        val audioIdsInPlaylists = playlistWithAudios.distinctAudios().first()

        val audioIds = downloadedAudioIds + audioIdsInPlaylists

        val deletedAudios = audiosDao.deleteExcept(audioIds)
        val deletedArtists = artistsDao.deleteAll()
        val deletedAlbums = albumsDao.deleteAll()
        Timber.d("deletedAudios: $deletedAudios, deletedArtists: $deletedArtists, deletedAlbums: $deletedAlbums")
    }
}
