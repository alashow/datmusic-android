/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.backup

import javax.inject.Inject
import kotlinx.coroutines.withContext
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.ResultInteractor
import tm.alashow.datmusic.data.db.AppDatabaseNuke
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.data.db.daos.PlaylistsDao
import tm.alashow.datmusic.data.db.daos.PlaylistsWithAudiosDao

class DatmusicRestore @Inject constructor(
    private val audiosDao: AudiosDao,
    private val playlistsDao: PlaylistsDao,
    private val playlistWithAudiosDao: PlaylistsWithAudiosDao,
    private val dispatchers: CoroutineDispatchers,
    private val databaseNuke: AppDatabaseNuke,
) : ResultInteractor<String, Int>() {
    override suspend fun doWork(params: String) = withContext(dispatchers.io) {
        databaseNuke.nuke()

        val backupData = DatmusicBackupData.fromJson(params)
        var insertedCount = 0
        insertedCount += audiosDao.insertAll(backupData.audios).size
        insertedCount += playlistsDao.insertAll(backupData.playlists).size
        insertedCount += playlistWithAudiosDao.insertAll(backupData.playlistAudios).size

        return@withContext insertedCount
    }
}
