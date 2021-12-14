/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.interactors.playlist

import android.net.Uri
import app.cash.turbine.test
import coil.ImageLoader
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
import tm.alashow.datmusic.data.repos.playlist.ArtworkImageFileType
import tm.alashow.datmusic.data.repos.playlist.PlaylistsRepo

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
class SetCustomPlaylistArtworkTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var setCustomPlaylistArtwork: SetCustomPlaylistArtwork
    @Inject lateinit var repo: PlaylistsRepo
    @Inject lateinit var imageLoader: ImageLoader

    private val testParams = SetCustomPlaylistArtwork.Params(uri = Uri.parse("test"), playlistId = -1)

    override fun tearDown() {
        super.tearDown()
        database.close()
        imageLoader.shutdown()
    }

    @Test
    fun `sets custom playlist artwork given uri`() = testScope.runBlockingTest {
        val playlistId = repo.createPlaylist(SampleData.playlist())
        val params = testParams.copy(playlistId = playlistId)

        setCustomPlaylistArtwork.execute(params)

        repo.playlist(playlistId).test {
            val playlist = awaitItem()
            assertThat(playlist.artworkPath)
                .isNotEmpty()
            assertThat(playlist.artworkPath)
                .contains(ArtworkImageFileType.PLAYLIST_USER_SET.prefix)
        }
    }
}
