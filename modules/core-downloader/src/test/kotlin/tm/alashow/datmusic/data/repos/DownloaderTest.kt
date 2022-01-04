/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.Status
import com.tonyodev.fetch2.database.DownloadInfo
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.coEvery
import io.mockk.coVerify
import javax.inject.Inject
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import tm.alashow.base.testing.BaseTest
import tm.alashow.data.PreferencesStore
import tm.alashow.datmusic.data.*
import tm.alashow.datmusic.data.db.AppDatabase
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.data.repos.audio.AudiosRepo
import tm.alashow.datmusic.domain.DownloadsSongsGrouping
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.domain.entities.DownloadRequest
import tm.alashow.datmusic.downloader.*
import tm.alashow.datmusic.downloader.manager.FetchDownloadManager
import tm.alashow.domain.models.None
import tm.alashow.domain.models.orNull
import tm.alashow.i18n.UiMessage

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
class DownloaderTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var repo: Downloader
    @Inject lateinit var audiosRepo: AudiosRepo
    @Inject lateinit var fetcher: Fetch
    @Inject lateinit var fetch: FetchDownloadManager
    @Inject lateinit var preferencesStore: PreferencesStore

    private val testItems = (1..5).map { SampleData.downloadRequest() }
    private val entriesComparator = compareByDescending(DownloadRequest::createdAt).thenBy(DownloadRequest::id)

    @After
    fun tearDown() = runTest {
        repo.resetDownloadsLocation()
        database.close()
    }

    private suspend fun Downloader.awaitEvents(vararg events: DownloaderEvent) = downloaderEvents.test {
        events.forEach { event ->
            assertThat(awaitItem())
                .isEqualTo(event)
        }
    }

    private suspend fun Downloader.awaitMessages(vararg events: UiMessage<*>) = downloaderEvents.test {
        events.forEach { event ->
            assertThat(awaitItem().toUiMessage())
                .isEqualTo(event)
        }
    }

    @Test
    fun setDownloadsLocation() = runTest {
        val (contentResolver, folder) = createTestDownloadsLocation()

        repo.setDownloadsLocation(folder)

        assertThat(contentResolver.persistedUriPermissions.map { it.uri })
            .containsExactly(folder.uri)
        preferencesStore.get(Downloader.DOWNLOADS_LOCATION, "").test {
            assertThat(awaitItem())
                .isEqualTo(folder.uri.toString())
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
    fun requestNewDownloadsLocation() = runTest {
        repo.requestNewDownloadsLocation()
        repo.awaitEvents(DownloaderEvent.ChooseDownloadsLocation)
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
    fun `enqueueAudio fails when downloads location is not set`() = runTest {
        val testItem = testItems.first().audio

        assertThat(repo.enqueueAudio(audio = testItem)).isFalse()

        repo.awaitEvents(DownloaderEvent.ChooseDownloadsLocation)
    }

    @Test
    fun `enqueueAudio fails if downloads location not set but then retries after setting it`() = runTest {
        val testItem = testItems.first().audio

        assertThat(repo.enqueueAudio(audio = testItem)).isFalse()

        repo.awaitEvents(DownloaderEvent.ChooseDownloadsLocation)
        repo.setDownloadsLocation(createTestDownloadsLocation().second)
        repo.awaitMessages(AudioDownloadQueued)
    }

    @Test
    fun `enqueueAudio fails if downloads location folder does not exist`() = runTest {
        val testItem = testItems.first().audio
        createTestDownloadsLocation().apply {
            repo.setDownloadsLocation(second)
            second.delete()
        }

        repo.downloaderEvents.test {
            assertThat(repo.enqueueAudio(audio = testItem))
                .isFalse()
            assertThat(awaitItem().toUiMessage())
                .isEqualTo(DownloadsFolderNotFound)
            assertThat(awaitItem())
                .isEqualTo(DownloaderEvent.ChooseDownloadsLocation)
        }
    }

    @Test
    fun `enqueueAudio fails if given audio without download url `() = runTest {
        val testItem = testItems.first().audio.copy(downloadUrl = null)
        repo.setDownloadsLocation(createTestDownloadsLocation().second)

        assertThat(repo.enqueueAudio(audio = testItem)).isFalse()

        repo.awaitMessages(AudioDownloadErrorInvalidUrl)
    }

    @Test
    fun `enqueueAudio fails if existing request for same audio exists`() = runTest {
        val testItem = testItems.first().audio
        repo.setDownloadsLocation(createTestDownloadsLocation().second)

        // enqueue twice and expect second enqueue to fail
        assertThat(repo.enqueueAudio(audio = testItem)).isTrue()
        assertThat(repo.enqueueAudio(audio = testItem)).isFalse()
        repo.awaitMessages(AudioDownloadAlreadyQueued)
    }

    @Test
    fun `enqueueAudio succeeds and deletes existing request if it's status is Failed or Cancelled`() = runTest {
        val testItem = testItems.first().audio
        repo.setDownloadsLocation(createTestDownloadsLocation().second)

        assertThat(repo.enqueueAudio(audio = testItem)).isTrue()

        listOf(Status.FAILED, Status.CANCELLED).forEachIndexed { index, status ->
            coEvery { fetch.getDownload(any()) }
                .answerGetDownloadWithStatus(status)
            assertThat(repo.enqueueAudio(audio = testItem)).isTrue()
            coVerify { fetch.delete(any<Int>()) }
            repo.awaitMessages(AudioDownloadQueued)
        }
    }

    @Test
    fun `enqueueAudio fails and resumes existing request with Paused status`() = runTest {
        val testItem = testItems.first().audio
        repo.setDownloadsLocation(createTestDownloadsLocation().second)

        assertThat(repo.enqueueAudio(audio = testItem)).isTrue()

        listOf(Status.PAUSED).forEachIndexed { index, status ->
            coEvery { fetch.getDownload(any()) }
                .answerGetDownloadWithStatus(status)
            assertThat(repo.enqueueAudio(audio = testItem)).isFalse()
            coVerify { fetch.resume(any<Int>()) }
            repo.awaitMessages(AudioDownloadResumedExisting)
        }
    }

    @Test
    fun `enqueueAudio fails if existing request with Queued, Downloading, or None status exists`() = runTest {
        val testItem = testItems.first().audio
        repo.setDownloadsLocation(createTestDownloadsLocation().second)

        assertThat(repo.enqueueAudio(audio = testItem)).isTrue()

        listOf(Status.NONE, Status.QUEUED, Status.DOWNLOADING).forEach { status ->
            coEvery { fetch.getDownload(any()) }
                .answerGetDownloadWithStatus(status)
            assertThat(repo.enqueueAudio(audio = testItem)).isFalse()
            repo.awaitMessages(AudioDownloadAlreadyQueued)
        }
    }

    @Test
    fun `enqueueAudio fails if existing request with Completed status exists and file also exists`() = runTest {
        val testItem = testItems.first().audio
        val downloadsFolder = createTestDownloadsLocation().second
        repo.setDownloadsLocation(downloadsFolder)

        assertThat(repo.enqueueAudio(audio = testItem)).isTrue()

        coEvery { fetch.getDownload(any()) }.answerGetDownload {
            it.apply {
                status = Status.COMPLETED
                file = testItem.createDocumentFile(downloadsFolder).uri.toString()
            }
        }

        assertThat(repo.enqueueAudio(audio = testItem)).isFalse()

        repo.awaitMessages(AudioDownloadAlreadyCompleted)
    }

    @Test
    fun `enqueueAudio succeeds if existing request with Completed status exists but file doesn't exist`() = runTest {
        val testItem = testItems.first().audio
        repo.setDownloadsLocation(createTestDownloadsLocation().second)

        assertThat(repo.enqueueAudio(audio = testItem)).isTrue()

        coEvery { fetch.getDownload(any()) }
            .answerGetDownloadWithStatus(Status.COMPLETED)
        // assuming default mock download file doesn't exist
        assertThat(repo.enqueueAudio(audio = testItem)).isTrue()
        coVerify { fetch.delete(any<Int>()) }
        repo.awaitMessages(AudioDownloadQueued)
    }

    @Test
    fun `enqueueAudio succeeds if there's existing request but there is no download info`() = runTest {
        val testItem = testItems.first().audio
        repo.setDownloadsLocation(createTestDownloadsLocation().second)

        assertThat(repo.enqueueAudio(audio = testItem)).isTrue()

        coEvery { fetch.getDownload(any()) }
            .answerGetDownload { null }
        // assuming default mock download file doesn't exist
        assertThat(repo.enqueueAudio(audio = testItem)).isTrue()
        repo.awaitMessages(AudioDownloadQueued)
    }

    @Test
    fun `enqueueAudio emits error event when fetch fails enqueueing`() = runTest {
        val testItem = testItems.first().audio
        repo.setDownloadsLocation(createTestDownloadsLocation().second)

        coEvery { fetch.enqueue(any()) }
            .answerFailedEnqueue()
        assertThat(repo.enqueueAudio(audio = testItem)).isFalse()
        repo.downloaderEvents.test {
            assertThat(awaitItem())
                .isInstanceOf(DownloaderEvent.DownloaderFetchError::class.java)
        }
    }

    @Test
    fun `enqueueAudio successfully saves given audio and enqueues given audio`() = runTest {
        val testItem = testItems.first().audio
        repo.setDownloadsLocation(createTestDownloadsLocation().second)

        assertThat(audiosRepo.exists(testItem.id)).isFalse()
        assertThat(repo.enqueueAudio(audio = testItem)).isTrue()
        assertThat(audiosRepo.exists(testItem.id)).isTrue()
        repo.awaitMessages(AudioDownloadQueued)
    }

    @Test
    fun `enqueueAudio successfully enqueues given audio id`() = runTest {
        val testItem = testItems.first().audio
        repo.setDownloadsLocation(createTestDownloadsLocation().second)

        // should fail because audio id isn't in database yet
        assertThat(repo.enqueueAudio(audioId = testItem.id)).isFalse()

        audiosRepo.insert(testItem)
        assertThat(repo.enqueueAudio(audioId = testItem.id)).isTrue()
        repo.awaitMessages(AudioDownloadQueued)
    }

    @Test
    fun `test download item actions pause, resume, cancel, retry, remove, delete`() = runTest {
        val testItem = testItems.first().audio
        repo.setDownloadsLocation(createTestDownloadsLocation().second)

        assertThat(repo.enqueueAudio(audio = testItem)).isTrue()

        repo.entry(testItem.id).test {
            val downloadRequest = awaitItem()
            val downloadItem = AudioDownloadItem(downloadRequest, DownloadInfo(), testItem)

            repo.pause(downloadItem)
            coVerify { fetch.pause(listOf(downloadItem.downloadInfo.id)) }

            repo.resume(downloadItem)
            coVerify { fetch.resume(listOf(downloadItem.downloadInfo.id)) }

            repo.cancel(downloadItem)
            coVerify { fetch.cancel(listOf(downloadItem.downloadInfo.id)) }

            repo.retry(downloadItem)
            coVerify { fetch.retry(listOf(downloadItem.downloadInfo.id)) }

            repo.remove(downloadItem)
            coVerify { fetch.remove(listOf(downloadItem.downloadInfo.id)) }
            assertThat(awaitItem()).isNull()

            // re-enqueue because it was just removed above
            assertThat(repo.enqueueAudio(audio = testItem)).isTrue()
            awaitItem()

            repo.delete(downloadItem)
            coVerify { fetch.delete(listOf(downloadItem.downloadInfo.id)) }
            assertThat(awaitItem()).isNull()
        }
    }

    @Test
    fun `getAudioDownload return audio download item given status`() = runTest {
        val testItem = testItems.first().audio
        repo.setDownloadsLocation(createTestDownloadsLocation().second)

        assertThat(repo.enqueueAudio(audio = testItem)).isTrue()
        // assuming default getAudioDownload allowedStatues is COMPLETED and mocked status is NONE
        assertThat(repo.getAudioDownload(testItem.id).orNull()?.audio)
            .isNull()
        coEvery { fetch.getDownload(any()) }
            .answerGetDownloadWithStatus(Status.COMPLETED)
        assertThat(repo.getAudioDownload(testItem.id).orNull()?.audio)
            .isEqualTo(testItem)

        coEvery { fetch.getDownload(any()) }
            .answerGetDownloadWithStatus(Status.FAILED)
        assertThat(repo.getAudioDownload(testItem.id, Status.FAILED).orNull()?.audio)
            .isEqualTo(testItem)

        coEvery { fetch.getDownload(any()) }
            .answerGetDownload { null }
        assertThat(repo.getAudioDownload(testItem.id, Status.NONE).orNull()?.audio)
            .isNull()
    }

    @Test
    fun `findAudioDownload returns None when audio doesn't exist`() = runTest {
        assertThat(repo.findAudioDownload("random"))
            .isEqualTo(None)
    }

    @Test
    fun `findAudioDownload returns Audio when download status isn't Completed but without audioDownloadItem`() = runTest {
        val testItem = testItems.first().audio
        repo.setDownloadsLocation(createTestDownloadsLocation().second)
        assertThat(repo.enqueueAudio(audio = testItem)).isTrue()
        val result = repo.findAudioDownload(testItem.id).value()
        assertThat(result.id)
            .isEqualTo(testItem.id)
        assertThat(result.audioDownloadItem)
            .isNull()
    }

    @Test
    fun `findAudioDownload returns Audio with audioDownloadItem when download status is Completed`() = runTest {
        val testItem = testItems.first().audio
        repo.setDownloadsLocation(createTestDownloadsLocation().second)
        assertThat(repo.enqueueAudio(audio = testItem)).isTrue()
        coEvery { fetch.getDownload(any()) }
            .answerGetDownloadWithStatus(Status.COMPLETED)
        val result = repo.findAudioDownload(testItem.id).value()
        assertThat(result.id)
            .isEqualTo(testItem.id)
        assertThat(result.audioDownloadItem?.audio)
            .isEqualTo(testItem)
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
