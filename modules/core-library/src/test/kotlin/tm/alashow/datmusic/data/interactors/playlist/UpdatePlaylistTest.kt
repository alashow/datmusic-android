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
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.db.AppDatabase
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.PLAYLIST_NAME_MAX_LENGTH
import tm.alashow.i18n.ValidationErrorBlank
import tm.alashow.i18n.ValidationErrorTooLong

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
class UpdatePlaylistTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var updatePlaylist: UpdatePlaylist
    @Inject lateinit var repo: PlaylistsRepo

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `updates playlist given playlist`() = runTest {
        val originalPlaylist = SampleData.playlist()
        repo.createPlaylist(originalPlaylist)

        val updatedPlaylist = SampleData.playlist().copy(id = originalPlaylist.id)
        updatePlaylist.execute(updatedPlaylist)

        repo.playlist(originalPlaylist.id).test {
            val playlist = awaitItem()
            assertThat(playlist)
                .isEqualTo(updatedPlaylist.copy(updatedAt = playlist.updatedAt))
        }
    }

    @Test(expected = ValidationErrorBlank::class)
    fun `fails given playlist with empty name`() = runTest {
        val originalPlaylist = SampleData.playlist()
        repo.createPlaylist(originalPlaylist)

        val updatedPlaylist = originalPlaylist.copy(name = "")
        updatePlaylist.execute(updatedPlaylist)
    }

    @Test(expected = ValidationErrorTooLong::class)
    fun `fails given playlist with too long name`() = runTest {
        val originalPlaylist = SampleData.playlist()
        repo.createPlaylist(originalPlaylist)

        val updatedPlaylist = originalPlaylist.copy(name = "a".repeat(PLAYLIST_NAME_MAX_LENGTH + 1))
        updatePlaylist.execute(updatedPlaylist)
    }
}
