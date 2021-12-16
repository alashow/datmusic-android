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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import tm.alashow.base.testing.BaseTest
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.db.AppDatabase
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.data.repos.audio.AudiosRepo
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
class UpdatePlaylistItemsTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var updatePlaylistItems: UpdatePlaylistItems
    @Inject lateinit var repo: PlaylistsRepo
    @Inject lateinit var audiosRepo: AudiosRepo

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `updates playlist items given playlist items`() = runTest {
        val audioIds = (1..5).map { SampleData.audio() }.also { audiosRepo.insertAll(it) }.map { it.id }
        val id = repo.createPlaylist(SampleData.playlist(), audioIds)
        val shuffledPlaylistItems = repo.playlistItems(id).first()
            .shuffled()
            .mapIndexed { index, playlistItem ->
                playlistItem.copy(playlistAudio = playlistItem.playlistAudio.copy(position = index))
            }

        updatePlaylistItems.execute(shuffledPlaylistItems)

        repo.playlistItems(id).test {
            assertThat(awaitItem())
                .isEqualTo(shuffledPlaylistItems)
        }
    }
}
