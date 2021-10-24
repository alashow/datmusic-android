/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.backup

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.base.util.extensions.writeToFile
import tm.alashow.data.ResultInteractor
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.data.db.daos.PlaylistsDao
import tm.alashow.datmusic.data.db.daos.PlaylistsWithAudiosDao

class CreateDatmusicBackup @Inject constructor(
    private val audiosDao: AudiosDao,
    private val playlistsDao: PlaylistsDao,
    private val playlistWithAudiosDao: PlaylistsWithAudiosDao,
    private val dispatchers: CoroutineDispatchers,
    private val clearUnusedEntities: ClearUnusedEntities,
) : ResultInteractor<CreateDatmusicBackup.Params, DatmusicBackupData>() {

    data class Params(val clearEntities: Boolean = true)

    override suspend fun doWork(params: Params) = withContext(dispatchers.io) {
        clearUnusedEntities()

        val audios = audiosDao.entries().first()
        val playlists = playlistsDao.entries().first().map { it.copyForBackup() }
        val playlistAudios = playlistWithAudiosDao.playlistAudios().first()

        return@withContext DatmusicBackupData(audios, playlists, playlistAudios)
    }
}

class DatmusicBackupToFile @Inject constructor(
    @ApplicationContext private val context: Context,
    private val createDatmusicBackup: CreateDatmusicBackup,
    private val dispatchers: CoroutineDispatchers,
) : ResultInteractor<Uri, Unit>() {
    override suspend fun doWork(params: Uri) = withContext(dispatchers.io) {
        val backup = createDatmusicBackup.execute(CreateDatmusicBackup.Params())
        val backupJsonBytes = backup.toJson().toByteArray()
        context.writeToFile(backupJsonBytes, params)
    }
}
