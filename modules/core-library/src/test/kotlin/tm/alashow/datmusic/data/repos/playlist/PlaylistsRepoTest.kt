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
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.domain.entities.Playlist

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
class PlaylistsRepoTest : BaseTest() {

    @Inject lateinit var repo: PlaylistsRepo

    private val testItems = (1..5).map { SampleData.playlist() }
    private val entriesComparator = compareByDescending(Playlist::id)

    @Test
    fun getByName() = testScope.runBlockingTest {
        val item = testItems.first()
        repo.insert(item)

        assertThat(repo.getByName(item.name)).isEqualTo(item)
    }

    @Test
    fun createPlaylistTest() = testScope.runBlockingTest {
        val playlist = testItems.first()
        val playlistId = repo.createPlaylist(playlist)

        assertThat(repo.getByName(playlist.name)?.id)
            .isEqualTo(playlistId)
    }
}
