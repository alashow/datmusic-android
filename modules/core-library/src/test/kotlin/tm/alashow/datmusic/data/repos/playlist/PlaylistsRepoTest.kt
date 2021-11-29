/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.playlist

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import javax.inject.Inject
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import tm.alashow.base.testing.BaseTest
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.data.db.daos.PlaylistsDao
import tm.alashow.datmusic.domain.entities.Playlist

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
class PlaylistsRepoTest : BaseTest() {

    @Inject lateinit var playlistsRepo: PlaylistsRepo
    @Inject lateinit var dao: PlaylistsDao

    @Test
    fun createPlaylistTest() = testScope.runBlockingTest {
        val playlist = Playlist(name = "Name")
        val playlistId = playlistsRepo.createPlaylist(playlist)

        assertThat(dao.getByName(playlist.name)?.id)
            .isEqualTo(playlistId)
    }
}
