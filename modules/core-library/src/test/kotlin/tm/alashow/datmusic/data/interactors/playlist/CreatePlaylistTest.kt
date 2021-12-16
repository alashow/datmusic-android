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
import tm.alashow.base.testing.awaitSingle
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.db.AppDatabase
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.data.repos.audio.AudiosRepo
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo
import tm.alashow.datmusic.domain.entities.PLAYLIST_NAME_MAX_LENGTH
import tm.alashow.i18n.ValidationErrorBlank
import tm.alashow.i18n.ValidationErrorTooLong

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
class CreatePlaylistTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var createPlaylist: CreatePlaylist
    @Inject lateinit var repo: PlaylistsRepo
    @Inject lateinit var audiosRepo: AudiosRepo

    private val testParams = CreatePlaylist.Params(name = "Test Name")

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `creates playlist given valid Playlist`() = runTest {
        val params = testParams

        createPlaylist(params).test {
            val playlist = awaitSingle()

            assertThat(playlist.name)
                .isEqualTo(params.name)
            assertThat(repo.getByName(params.name))
                .isEqualTo(playlist)
        }
    }

    @Test
    fun `creates playlist with generated name given empty name`() = runTest {
        val params = testParams.copy(name = "", generateNameIfEmpty = true)

        createPlaylist(params).test {
            val playlist = awaitSingle()

            assertThat(playlist.name)
                .isEqualTo("My Playlist #1")
            assertThat(repo.getByName(playlist.name))
                .isEqualTo(playlist)
        }
    }

    @Test
    fun `creates playlist with trimmed named given too long name`() = runTest {
        val longName = "a".repeat(300)
        val params = testParams.copy(name = "a".repeat(300), trimIfTooLong = true)

        createPlaylist(params).test {
            val playlist = awaitSingle()

            assertThat(playlist.name)
                .isEqualTo(longName.take(PLAYLIST_NAME_MAX_LENGTH))

            repo.playlist(playlist.id).test {
                assertThat(awaitItem()).isNotNull()
            }
        }
    }

    @Test(expected = ValidationErrorBlank::class)
    fun `fails with empty name given empty name and generateNameIfEmpty false`() = runTest {
        val params = testParams.copy(name = "", generateNameIfEmpty = false)

        createPlaylist.execute(params)
    }

    @Test(expected = ValidationErrorTooLong::class)
    fun `fails with too long name given long name and trimIfTooLong false`() = runTest {
        val params = testParams.copy(name = "a".repeat(300), trimIfTooLong = false)

        createPlaylist.execute(params)
    }

    @Test
    fun `creates playlist with given audios & ids`() = runTest {
        val audiosCount = 10
        val audioItems = (1..audiosCount).map { SampleData.audio() }.apply { audiosRepo.insertAll(this) }
        val audioIds = audioItems.map { it.id }
        val params = testParams.copy(audios = audioItems.take(audiosCount / 5), audioIds = audioIds.drop(audiosCount / 5))

        val playlist = createPlaylist.execute(params)

        assertThat(repo.getByName(params.name))
            .isEqualTo(playlist)

        repo.playlistItems(playlist.id).test {
            val playlistItems = awaitItem()

            assertThat(playlistItems.map { it.audio.id })
                .isEqualTo(audioIds)

            // Check that playlist item positions are correct
            assertThat(playlistItems.map { it.playlistAudio.position })
                .isEqualTo((0 until audiosCount).toList())
        }
    }
}
