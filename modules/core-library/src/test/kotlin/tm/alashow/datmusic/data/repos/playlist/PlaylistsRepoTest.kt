/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.playlist

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
import tm.alashow.base.util.extensions.swap
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.db.AppDatabase
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.domain.entities.PLAYLIST_NAME_MAX_LENGTH
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.i18n.DatabaseNotFoundError
import tm.alashow.i18n.ValidationErrorBlank
import tm.alashow.i18n.ValidationErrorTooLong

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
class PlaylistsRepoTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var repo: PlaylistsRepo
    @Inject lateinit var audiosDao: AudiosDao

    private val testItems = (1..5).map { SampleData.playlist() }
    private val entriesComparator = compareByDescending(Playlist::id)

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getByName() = runTest {
        val item = testItems.first()
        repo.insert(item)

        assertThat(repo.getByName(item.name)).isEqualTo(item)
    }

    @Test
    fun playlistItems() = runTest {
        val item = testItems.first()
        val audioIds = (1..5).map { SampleData.audio() }.also { audiosDao.insertAll(it) }.map { it.id }
        val id = repo.createPlaylist(item, audioIds)

        repo.playlistItems(id).test {
            assertThat(awaitItem().map { it.audio.id })
                .isEqualTo(audioIds)
        }
    }

    @Test
    fun playlistWithAudios() = runTest {
        val item = testItems.first()
        val audioIds = (1..5).map { SampleData.audio() }.also { audiosDao.insertAll(it) }.map { it.id }
        val id = repo.createPlaylist(item, audioIds)

        repo.playlistWithItems(id).test {
            assertThat(awaitItem().items.map { it.audio.id })
                .isEqualTo(audioIds)
        }
    }

    @Test
    fun playlists() = runTest {
        repo.insertAll(testItems)

        repo.playlists().test {
            assertThat(awaitItem())
                .isEqualTo(testItems.sortedWith(entriesComparator))
        }
    }

    @Test
    fun validatePlaylistId() = runTest {
        val item = testItems.first()
        val id = repo.createPlaylist(item)

        repo.validatePlaylistId(id)
    }

    @Test(expected = DatabaseNotFoundError::class)
    fun validatePlaylistId_notFound() = runTest {
        val item = testItems.first()

        repo.validatePlaylistId(item.id)
    }

    @Test
    fun createPlaylist() = runTest {
        val item = testItems.first()
        val id = repo.createPlaylist(item)

        assertThat(repo.getByName(item.name)?.id)
            .isEqualTo(id)
    }

    @Test(expected = ValidationErrorBlank::class)
    fun `createPlaylist fails with empty playlist name`() = runTest {
        val item = testItems.first().copy(name = "")
        repo.createPlaylist(item)
    }

    @Test(expected = ValidationErrorTooLong::class)
    fun `createPlaylist fails with too long playlist name`() = runTest {
        val item = testItems.first().copy(name = "a".repeat(PLAYLIST_NAME_MAX_LENGTH + 1))
        repo.createPlaylist(item)
    }

    @Test
    fun getOrCreatePlaylist() = runTest {
        val item = testItems.first()
        repo.createPlaylist(item)
        repo.getOrCreatePlaylist(item.name)

        assertThat(repo.getByName(item.name))
            .isEqualTo(item)
    }

    @Test
    fun getOrCreatePlaylist_inexisting() = runTest {
        val item = testItems.first()
        val id = repo.getOrCreatePlaylist(item.name)

        assertThat(repo.getByName(item.name)?.id)
            .isEqualTo(id)
    }

    @Test
    fun updatePlaylist() = runTest {
        val item = testItems.first()
        repo.createPlaylist(item)

        val updatedItem = item.copy(name = "Updated name")
        repo.updatePlaylist(updatedItem)

        val updated = repo.getByName(updatedItem.name)
        assertThat(updated)
            .isEqualTo(updatedItem.copy(updatedAt = (updated ?: updatedItem).updatedAt))
    }

    @Test(expected = DatabaseNotFoundError::class)
    fun `updatePlaylist fails with inexisting playlist id`() = runTest {
        val item = testItems.first()
        repo.updatePlaylist(item)
    }

    @Test(expected = ValidationErrorBlank::class)
    fun `updatePlaylist fails with empty playlist name`() = runTest {
        val item = testItems.first()
        repo.createPlaylist(item)
        repo.updatePlaylist(item.copy(name = ""))
    }

    @Test(expected = ValidationErrorTooLong::class)
    fun `updatePlaylist fails with too long playlist name`() = runTest {
        val item = testItems.first()
        repo.createPlaylist(item)
        repo.updatePlaylist(item.copy(name = "a".repeat(PLAYLIST_NAME_MAX_LENGTH + 1)))
    }

    @Test
    fun updatePlaylistById() = runTest {
        val item = testItems.first()
        val id = repo.createPlaylist(item)

        val updatedItem = repo.updatePlaylist(id) { it.copy(name = "Updated name") }

        val updated = repo.getByName(updatedItem.name)
        assertThat(updated)
            .isEqualTo(updatedItem.copy(updatedAt = (updated ?: updatedItem).updatedAt))
    }

    @Test(expected = DatabaseNotFoundError::class)
    fun `updatePlaylistById fails with inexisting playlist id`() = runTest {
        val item = testItems.first()

        repo.updatePlaylist(item.id) { it.copy(name = "") }
    }

    @Test(expected = ValidationErrorBlank::class)
    fun `updatePlaylistById with empty playlist name`() = runTest {
        val item = testItems.first()
        val id = repo.createPlaylist(item)

        repo.updatePlaylist(id) { it.copy(name = "") }
    }

    @Test(expected = ValidationErrorTooLong::class)
    fun `updatePlaylistById with too long playlist name`() = runTest {
        val item = testItems.first()
        val id = repo.createPlaylist(item)

        repo.updatePlaylist(id) { it.copy(name = "a".repeat(PLAYLIST_NAME_MAX_LENGTH + 1)) }
    }

    @Test
    fun addAudiosToPlaylist() = runTest {
        val item = testItems.first()
        val id = repo.createPlaylist(item)

        val audioIds = (1..5).map { SampleData.audio() }.also { audiosDao.insertAll(it) }.map { it.id }
        repo.addAudiosToPlaylist(id, audioIds)

        // Check playlist audios are added have the correct positions
        repo.playlistItems(id).test {
            val playlistItems = awaitItem()
            assertThat(playlistItems.map { it.audio.id })
                .isEqualTo(audioIds)
            assertThat(playlistItems.map { it.playlistAudio.position })
                .isEqualTo(audioIds.indices.toList())
        }

        // Check playlist items are not duplicated when ignoringExisting
        repo.addAudiosToPlaylist(id, audioIds, ignoreExisting = true)
        repo.playlistItems(id).test {
            assertThat(awaitItem().map { it.audio.id })
                .isEqualTo(audioIds)
        }

        // Check more ids are added and have correct positions
        val moreAudioIds = (1..5).map { SampleData.audio() }.also { audiosDao.insertAll(it) }.map { it.id }
        repo.addAudiosToPlaylist(id, moreAudioIds)
        repo.playlistItems(id).test {
            val playlistItems = awaitItem()
            assertThat(playlistItems.map { it.audio.id })
                .isEqualTo(audioIds + moreAudioIds)
            assertThat(playlistItems.map { it.playlistAudio.position })
                .isEqualTo((audioIds + moreAudioIds).indices.toList())
        }
    }

    @Test
    fun swapPositions() = runTest {
        val item = testItems.first()
        val audioIds = (1..5).map { SampleData.audio() }.also { audiosDao.insertAll(it) }.map { it.id }
        val repositionedAudioIds = audioIds.swap(0, audioIds.size - 1)
        val id = repo.createPlaylist(item, audioIds)

        repo.swapPositions(id, 0, audioIds.size - 1)
        repo.playlistItems(id).test {
            assertThat(awaitItem().map { it.audio.id })
                .isEqualTo(repositionedAudioIds)
        }
    }

    @Test
    fun updatePlaylistItems() = runTest {
        val item = testItems.first()
        val audioIds = (1..5).map { SampleData.audio() }.also { audiosDao.insertAll(it) }.map { it.id }
        val id = repo.createPlaylist(item, audioIds)
        val shuffledPlaylistItems = repo.playlistItems(id).first()
            .shuffled()
            .mapIndexed { index, playlistItem ->
                playlistItem.copy(playlistAudio = playlistItem.playlistAudio.copy(position = index))
            }

        repo.updatePlaylistItems(shuffledPlaylistItems)

        repo.playlistItems(id).test {
            assertThat(awaitItem())
                .isEqualTo(shuffledPlaylistItems)
        }
    }

    @Test
    fun removePlaylistItems() = runTest {
        val item = testItems.first()
        val audioIds = (1..5).map { SampleData.audio() }.also { audiosDao.insertAll(it) }.map { it.id }
        val id = repo.createPlaylist(item, audioIds)

        val playlistItems = repo.playlistItems(id).first().shuffled()
        val playlistItemsToRemove = playlistItems.take(2)
        val removedItemsCount = repo.removePlaylistItems(playlistItemsToRemove.map { it.playlistAudio.id })

        assertThat(removedItemsCount)
            .isEqualTo(playlistItemsToRemove.size)

        repo.playlistItems(id).test {
            assertThat(awaitItem())
                .containsExactlyElementsIn(playlistItems.subtract(playlistItemsToRemove))
        }
    }

    @Test
    fun clearPlaylistArtwork() = runTest {
        val item = testItems.first().copy(artworkPath = "some/path")
        val id = repo.createPlaylist(item)

        repo.clearPlaylistArtwork(id)

        repo.playlist(id).test {
            assertThat(awaitItem().artworkPath).isNull()
        }
    }

    // region RoomRepo tests

    @Test
    fun entry() = runTest {
        val item = testItems.first()
        repo.insert(item)

        repo.entry(item.id).test {
            assertThat(awaitItem())
                .isEqualTo(item)
        }
    }

    @Test
    fun `entry returns null if item doesn't exist`() = runTest {
        val item = testItems.first()

        repo.entry(item.id).test {
            assertThat(awaitItem())
                .isNull()
            repo.insert(item)
            assertThat(awaitItem())
                .isEqualTo(item)
        }
    }

    @Test
    fun `entryNotNull doesn't return anything it's available`() = runTest {
        val item = testItems.first()

        repo.entryNotNull(item.id).test {
            // awaiting here would get stuck since it won't return null items
            repo.insert(item)
            assertThat(awaitItem())
                .isEqualTo(item)
        }
    }

    @Test
    fun entries() = runTest {
        repo.insertAll(testItems)

        repo.entries().test {
            assertThat(awaitItem())
                .isEqualTo(testItems.sortedWith(entriesComparator))
        }
    }

    @Test
    fun entries_byId() = runTest {
        repo.insertAll(testItems)

        repo.entries(testItems.map { it.id }).test {
            assertThat(awaitItem())
                .containsExactlyElementsIn(testItems)
        }
    }

    @Test
    fun update() = runTest {
        val item = testItems.first()
        repo.insert(item)

        val updated = item.copy(
            name = "Updated Name",
            artworkSource = "Updated Source",
            artworkPath = "Updated Path",
            params = "Updated Params",
        ).updatedCopy()
        repo.update(updated)

        repo.entry(item.id).test {
            assertThat(awaitItem()).isEqualTo(updated)
        }
    }

    @Test
    fun isEmpty() = runTest {
        repo.isEmpty().test {
            assertThat(awaitItem()).isTrue()
        }

        repo.insertAll(testItems)

        repo.isEmpty().test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun count() = runTest {
        repo.count().test {
            assertThat(awaitItem()).isEqualTo(0)
        }

        repo.insertAll(testItems)

        repo.count().test {
            assertThat(awaitItem()).isEqualTo(testItems.size)
        }
    }

    @Test
    fun has() = runTest {
        val item = testItems.first()

        repo.has(item.id).test {
            assertThat(awaitItem()).isFalse()
            repo.insert(item)
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun exists() = runTest {
        val item = testItems.first()

        assertThat(repo.exists(item.id)).isFalse()
        repo.insert(item)
        assertThat(repo.exists(item.id)).isTrue()
    }

    @Test
    fun delete() = runTest {
        val item = testItems.first()
        val id = repo.createPlaylist(item)

        repo.delete(id)

        assertThat(repo.exists(id)).isFalse()
    }

    @Test
    fun deleteEntity() = runTest {
        val item = testItems.first()
        repo.insert(item)

        repo.delete(item)

        assertThat(repo.exists(item.id)).isFalse()
    }

    @Test
    fun deleteAll() = runTest {
        repo.insertAll(testItems)

        repo.deleteAll()

        repo.isEmpty().test {
            assertThat(awaitItem()).isTrue()
        }
    }

    // endregion
}
