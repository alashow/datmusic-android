/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.backup

import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.ResultInteractor
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.data.db.daos.PlaylistsDao
import tm.alashow.datmusic.data.db.daos.PlaylistsWithAudiosDao

class DatmusicBackup @Inject constructor(
    private val audiosDao: AudiosDao,
    private val playlistsDao: PlaylistsDao,
    private val playlistWithAudiosDao: PlaylistsWithAudiosDao,
    private val dispatchers: CoroutineDispatchers,
    private val clearUnusedEntities: ClearUnusedEntities,
) : ResultInteractor<Unit, String>() {
    override suspend fun doWork(params: Unit) = withContext(dispatchers.io) {
        clearUnusedEntities()

        val audios = audiosDao.entries().first()
        val playlists = playlistsDao.entries().first()
        val playlistAudios = playlistWithAudiosDao.playlistAudios().first()
        val backupData = DatmusicBackupData(audios, playlists, playlistAudios)

        return@withContext backupData.toJson()
    }
}
