/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.interactors.backup

import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.db.daos.AlbumsDao
import tm.alashow.datmusic.data.db.daos.ArtistsDao
import tm.alashow.datmusic.data.db.daos.DownloadRequestsDao
import tm.alashow.datmusic.data.repos.audio.AudiosRepo
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.DownloadRequest

internal suspend fun createBackupData(
    playlistsRepo: PlaylistsRepo,
    audiosRepo: AudiosRepo,
    artistsDao: ArtistsDao,
    albumsDao: AlbumsDao,
    downloadRequestsDao: DownloadRequestsDao,
): Pair<List<Audio>, List<DownloadRequest>> {
    val audioItems = (1..10).map { SampleData.audio() }
        .also { audiosRepo.insertAll(it) }
    val artistItems = (1..10).map { SampleData.artist() }
        .also { artistsDao.insertAll(it) }
    val albumItems = (1..10).map { SampleData.album() }
        .also { albumsDao.insertAll(it) }
    val playlistItemAudios = audioItems.shuffled().take(5)
    val playlist = SampleData.playlist()
        .also { playlistsRepo.createPlaylist(it, playlistItemAudios.map { it.id }) }
    val downloadRequests = audioItems.map { SampleData.downloadRequest(it) }
        .also { audiosRepo.insertAll(it.map { it.audio }) }
        .also { downloadRequestsDao.insertAll(it) }

    return Pair(playlistItemAudios, downloadRequests)
}
