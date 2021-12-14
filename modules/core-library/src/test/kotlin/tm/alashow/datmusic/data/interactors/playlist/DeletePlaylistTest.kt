/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.interactors.playlist

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import javax.inject.Inject
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import tm.alashow.base.testing.BaseTest
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.db.AppDatabase
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
class DeletePlaylistTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var deletePlaylist: DeletePlaylist
    @Inject lateinit var repo: PlaylistsRepo

    override fun tearDown() {
        super.tearDown()
        database.close()
    }

    @Test
    fun `deletes playlist given playlist id`() = testScope.runBlockingTest {
        val playlistId = repo.createPlaylist(SampleData.playlist())

        deletePlaylist.execute(playlistId)

        assertThat(repo.exists(playlistId))
            .isFalse()
    }
}
