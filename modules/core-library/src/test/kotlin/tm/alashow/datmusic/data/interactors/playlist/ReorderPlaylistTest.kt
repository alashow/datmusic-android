/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.interactors.playlist

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import javax.inject.Inject
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import tm.alashow.base.testing.BaseTest
import tm.alashow.base.util.extensions.swap
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.db.AppDatabase
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.data.repos.audio.AudiosRepo
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
class ReorderPlaylistTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var reorderPlaylist: ReorderPlaylist
    @Inject lateinit var repo: PlaylistsRepo
    @Inject lateinit var audiosRepo: AudiosRepo

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `swaps position of playlist items given playlist id and from, to`() = runTest {
        val playlist = SampleData.playlist()
        val audioIds = (1..5).map { SampleData.audio() }
            .apply { audiosRepo.insertAll(this) }
            .map { it.id }
        repo.createPlaylist(playlist, audioIds = audioIds)

        val params = ReorderPlaylist.Params(playlist.id, 0, 4)
        reorderPlaylist.execute(params)

        repo.playlistItems(playlist.id).test {
            assertThat(awaitItem().map { it.audio.id })
                .isEqualTo(audioIds.swap(0, 4))
        }
    }
}
