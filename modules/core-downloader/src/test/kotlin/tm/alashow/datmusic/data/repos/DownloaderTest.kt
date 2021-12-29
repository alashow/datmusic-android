/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tonyodev.fetch2.Fetch
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import tm.alashow.base.testing.BaseTest
import tm.alashow.base.util.extensions.simpleName
import tm.alashow.data.PreferencesStore
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.db.AppDatabase
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.data.repos.audio.AudiosRepo
import tm.alashow.datmusic.domain.DownloadsSongsGrouping
import tm.alashow.datmusic.domain.entities.DownloadRequest
import tm.alashow.datmusic.downloader.AudioDownloadAlreadyQueued
import tm.alashow.datmusic.downloader.AudioDownloadQueued
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.downloader.DownloaderEvent

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
class DownloaderTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var repo: Downloader
    @Inject lateinit var audiosRepo: AudiosRepo
    @Inject lateinit var fetcher: Fetch
    @Inject lateinit var preferencesStore: PreferencesStore

    private val testItems = (1..5).map { SampleData.downloadRequest() }
    private val entriesComparator = compareByDescending(DownloadRequest::createdAt).thenBy(DownloadRequest::id)

    @After
    fun tearDown() {
        database.close()
    }

    private fun createTestDownloadsLocation(): Pair<ContentResolver, Uri> {
        val context: Context = ApplicationProvider.getApplicationContext()
        val folder = File(context.filesDir, "Downloads")
        folder.mkdir()
        return context.contentResolver to folder.toUri()
    }

    @Test
    fun `given folder uri saves downloads location and persists uri`() = runTest {
        val (contentResolver, folderUri) = createTestDownloadsLocation()

        repo.setDownloadsLocation(folderUri)

        assertThat(contentResolver.persistedUriPermissions.map { it.uri })
            .containsExactly(folderUri)
        preferencesStore.get(Downloader.DOWNLOADS_LOCATION, "").test {
            assertThat(awaitItem())
                .isEqualTo(folderUri.toString())
        }
    }

    @Test
    fun hasDownloadsLocation() = runTest {
        repo.hasDownloadsLocation.test {
            assertThat(awaitItem()).isFalse()

            repo.setDownloadsLocation(createTestDownloadsLocation().second)

            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun setDownloadsSongsGrouping() = runTest {
        val grouping = DownloadsSongsGrouping.values().random()

        repo.setDownloadsSongsGrouping(grouping)

        preferencesStore.get(Downloader.DOWNLOADS_SONGS_GROUPING, "").test {
            assertThat(DownloadsSongsGrouping.from(awaitItem()))
                .isEqualTo(grouping)
        }
    }

    @Test
    fun `enqueueAudio doesn't enqueue when downloads location not set`() = runTest {
        val testItem = testItems.first().audio

        val isEnqueued = repo.enqueueAudio(audio = testItem)

        assertThat(isEnqueued).isFalse()
        repo.downloaderEvents.test {
            assertThat(awaitItem())
                .isEqualTo(DownloaderEvent.ChooseDownloadsLocation)
        }
    }

    @Test
    fun `enqueueAudio successfully enqueues audio`() = runTest {
        val testItem = testItems.first().audio
        repo.setDownloadsLocation(createTestDownloadsLocation().second)

        val isEnqueued = repo.enqueueAudio(audio = testItem)

        assertThat(isEnqueued).isFalse()
        repo.downloaderEvents.test {
            assertThat(awaitItem().simpleName)
                .isEqualTo(AudioDownloadQueued)
        }
    }

    @Test
    fun `enqueueAudio doesn't allow enqueuing already enqueued audio`() = runTest {
        val testItem = testItems.first().audio

        assertThat(repo.enqueueAudio(audio = testItem)).isTrue()
        assertThat(repo.enqueueAudio(audio = testItem)).isFalse()

        repo.downloaderEvents.test {
            assertThat(awaitItem())
                .isEqualTo(AudioDownloadAlreadyQueued)
        }
    }

    // region RoomRepo tests

    @Test
    fun entry() = runTest {
        val item = testItems.first()
        repo.insert(item)

        repo.entry(item.id).test {
            assertThat(awaitItem()).isEqualTo(item)
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

        val updated = SampleData.downloadRequest().copy(id = item.id)
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
        repo.insert(item)

        repo.delete(item.id)

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
