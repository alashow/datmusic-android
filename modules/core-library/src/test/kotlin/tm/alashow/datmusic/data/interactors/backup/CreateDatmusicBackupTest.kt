/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.interactors.backup

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import tm.alashow.base.testing.BaseTest
import tm.alashow.datmusic.data.db.AppDatabase
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.data.db.daos.AlbumsDao
import tm.alashow.datmusic.data.db.daos.ArtistsDao
import tm.alashow.datmusic.data.db.daos.DownloadRequestsDao
import tm.alashow.datmusic.data.repos.audio.AudiosRepo
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
class CreateDatmusicBackupTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var playlistsRepo: PlaylistsRepo
    @Inject lateinit var audiosRepo: AudiosRepo
    @Inject lateinit var artistsDao: ArtistsDao
    @Inject lateinit var albumsDao: AlbumsDao
    @Inject lateinit var downloadRequestsDao: DownloadRequestsDao
    @Inject lateinit var createDatmusicBackup: CreateDatmusicBackup

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `creates backup with audios & playlists`() = runTest {
        val (playlistItemAudios, downloadRequests) = createBackupData(playlistsRepo, audiosRepo, artistsDao, albumsDao, downloadRequestsDao)

        val backup = createDatmusicBackup.execute(Unit)

        val playlistAudiosAndDownloads = playlistItemAudios.map { it.id } + downloadRequests.map { it.id }
        assertThat(backup.audios.map { it.id }.toSet())
            .containsExactlyElementsIn(playlistAudiosAndDownloads.toSet())

        assertThat(artistsDao.entries().first())
            .isEmpty()
        assertThat(albumsDao.entries().first())
            .isEmpty()
    }
}
