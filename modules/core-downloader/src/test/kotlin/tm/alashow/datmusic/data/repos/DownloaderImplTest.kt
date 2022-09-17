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
import tm.alashow.base.ui.SnackbarManager
import tm.alashow.base.ui.SnackbarMessage
import tm.alashow.data.PreferencesStore
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.answerFailedEnqueue
import tm.alashow.datmusic.data.answerGetDownload
import tm.alashow.datmusic.data.answerGetDownloadWithStatus
import tm.alashow.datmusic.data.createTestDownloadsLocation
import tm.alashow.datmusic.data.db.AppDatabase
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.data.repos.audio.AudiosRepo
import tm.alashow.datmusic.domain.DownloadsSongsGrouping
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.downloader.AudioDownloadAlreadyCompleted
import tm.alashow.datmusic.downloader.AudioDownloadAlreadyQueued
import tm.alashow.datmusic.downloader.AudioDownloadErrorInvalidUrl
import tm.alashow.datmusic.downloader.AudioDownloadQueued
import tm.alashow.datmusic.downloader.AudioDownloadResumedExisting
import tm.alashow.datmusic.downloader.DownloadRequestsRepo
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.downloader.DownloaderEvent
import tm.alashow.datmusic.downloader.DownloadsFolderNotFound
import tm.alashow.datmusic.downloader.createDocumentFile
import tm.alashow.datmusic.downloader.manager.FetchDownloadManager
import tm.alashow.domain.models.None
import tm.alashow.domain.models.orNull
import tm.alashow.i18n.UiMessage

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
class DownloaderImplTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var downloader: Downloader
    @Inject lateinit var downloadRequestsRepo: DownloadRequestsRepo
    @Inject lateinit var audiosRepo: AudiosRepo
    @Inject lateinit var fetcher: Fetch
    @Inject lateinit var fetch: FetchDownloadManager
    @Inject lateinit var preferencesStore: PreferencesStore
    @Inject lateinit var snackbarManager: SnackbarManager

    private val testItems = (1..5).map { SampleData.downloadRequest() }

    @After
    fun tearDown() = runTest {
        downloader.resetDownloadsLocation()
        database.close()
    }

    private suspend fun Downloader.awaitEvents(vararg events: DownloaderEvent) = downloaderEvents.test {
        events.forEach { event ->
            assertThat(awaitItem())
                .isEqualTo(event)
        }
    }

    private suspend fun SnackbarManager.awaitMessages(vararg messages: UiMessage<*>) = this.messages.test {
        messages.forEach { message ->
            assertThat(awaitItem().message)
                .isEqualTo(message)
            onMessageDismissed(SnackbarMessage<Unit>(message))
        }
    }

    @Test
    fun setDownloadsLocation() = runTest {
        val (contentResolver, folder) = createTestDownloadsLocation()

        downloader.setDownloadsLocation(folder)

        assertThat(contentResolver.persistedUriPermissions.map { it.uri })
            .containsExactly(folder.uri)
        preferencesStore.get(Downloader.DOWNLOADS_LOCATION, "").test {
            assertThat(awaitItem())
                .isEqualTo(folder.uri.toString())
        }
    }

    @Test
    fun hasDownloadsLocation() = runTest {
        downloader.hasDownloadsLocation.test {
            assertThat(awaitItem()).isFalse()
            downloader.setDownloadsLocation(createTestDownloadsLocation().second)
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun requestNewDownloadsLocation() = runTest {
        downloader.requestNewDownloadsLocation()
        downloader.awaitEvents(DownloaderEvent.ChooseDownloadsLocation)
    }

    @Test
    fun setDownloadsSongsGrouping() = runTest {
        val grouping = DownloadsSongsGrouping.values().random()

        downloader.setDownloadsSongsGrouping(grouping)

        preferencesStore.get(Downloader.DOWNLOADS_SONGS_GROUPING, "").test {
            assertThat(DownloadsSongsGrouping.from(awaitItem()))
                .isEqualTo(grouping)
        }
    }

    @Test
    fun `enqueueAudio fails when downloads location is not set`() = runTest {
        val testItem = testItems.first().audio

        assertThat(downloader.enqueueAudio(audio = testItem)).isFalse()

        downloader.awaitEvents(DownloaderEvent.ChooseDownloadsLocation)
    }

    @Test
    fun `enqueueAudio fails if downloads location not set but then retries after setting it`() = runTest {
        val testItem = testItems.first().audio

        assertThat(downloader.enqueueAudio(audio = testItem)).isFalse()

        downloader.awaitEvents(DownloaderEvent.ChooseDownloadsLocation)
        downloader.setDownloadsLocation(createTestDownloadsLocation().second)
        snackbarManager.awaitMessages(AudioDownloadQueued)
    }

    @Test
    fun `enqueueAudio fails if downloads location folder does not exist`() = runTest {
        val testItem = testItems.first().audio
        createTestDownloadsLocation().apply {
            downloader.setDownloadsLocation(second)
            second.delete()
        }

        downloader.downloaderEvents.test {
            assertThat(downloader.enqueueAudio(audio = testItem))
                .isFalse()
            snackbarManager.awaitMessages(DownloadsFolderNotFound)
            assertThat(awaitItem())
                .isEqualTo(DownloaderEvent.ChooseDownloadsLocation)
        }
    }

    @Test
    fun `enqueueAudio fails if given audio without download url `() = runTest {
        val testItem = testItems.first().audio.copy(downloadUrl = null)
        downloader.setDownloadsLocation(createTestDownloadsLocation().second)

        assertThat(downloader.enqueueAudio(audio = testItem)).isFalse()

        snackbarManager.awaitMessages(AudioDownloadErrorInvalidUrl)
    }

    @Test
    fun `enqueueAudio fails if existing request for same audio exists`() = runTest {
        val testItem = testItems.first().audio
        downloader.setDownloadsLocation(createTestDownloadsLocation().second)

        // enqueue twice and expect second enqueue to fail
        assertThat(downloader.enqueueAudio(audio = testItem)).isTrue()
        assertThat(downloader.enqueueAudio(audio = testItem)).isFalse()
        snackbarManager.awaitMessages(AudioDownloadAlreadyQueued)
    }

    @Test
    fun `enqueueAudio succeeds and deletes existing request if it's status is Failed or Cancelled`() = runTest {
        val testItem = testItems.first().audio
        downloader.setDownloadsLocation(createTestDownloadsLocation().second)

        assertThat(downloader.enqueueAudio(audio = testItem)).isTrue()

        listOf(Status.FAILED, Status.CANCELLED).forEachIndexed { index, status ->
            coEvery { fetch.getDownload(any()) }
                .answerGetDownloadWithStatus(status)
            assertThat(downloader.enqueueAudio(audio = testItem))
                .isTrue()
            coVerify { fetch.delete(any<Int>()) }
            snackbarManager.awaitMessages(AudioDownloadQueued)
        }
    }

    @Test
    fun `enqueueAudio fails and resumes existing request with Paused status`() = runTest {
        val testItem = testItems.first().audio
        downloader.setDownloadsLocation(createTestDownloadsLocation().second)

        assertThat(downloader.enqueueAudio(audio = testItem)).isTrue()

        listOf(Status.PAUSED).forEachIndexed { index, status ->
            coEvery { fetch.getDownload(any()) }
                .answerGetDownloadWithStatus(status)
            assertThat(downloader.enqueueAudio(audio = testItem))
                .isFalse()
            coVerify { fetch.resume(any<Int>()) }
            snackbarManager.awaitMessages(AudioDownloadResumedExisting)
        }
    }

    @Test
    fun `enqueueAudio fails if existing request with Queued, Downloading, or None status exists`() = runTest {
        val testItem = testItems.first().audio
        downloader.setDownloadsLocation(createTestDownloadsLocation().second)

        assertThat(downloader.enqueueAudio(audio = testItem)).isTrue()

        listOf(Status.NONE, Status.QUEUED, Status.DOWNLOADING).forEach { status ->
            coEvery { fetch.getDownload(any()) }
                .answerGetDownloadWithStatus(status)
            assertThat(downloader.enqueueAudio(audio = testItem))
                .isFalse()
            snackbarManager.awaitMessages(AudioDownloadAlreadyQueued)
        }
    }

    @Test
    fun `enqueueAudio fails if existing request with Completed status exists and file also exists`() = runTest {
        val testItem = testItems.first().audio
        val downloadsFolder = createTestDownloadsLocation().second
        downloader.setDownloadsLocation(downloadsFolder)

        assertThat(downloader.enqueueAudio(audio = testItem)).isTrue()

        coEvery { fetch.getDownload(any()) }.answerGetDownload {
            it.apply {
                status = Status.COMPLETED
                file = testItem.createDocumentFile(downloadsFolder).uri.toString()
            }
        }

        assertThat(downloader.enqueueAudio(audio = testItem)).isFalse()

        snackbarManager.awaitMessages(AudioDownloadAlreadyCompleted)
    }

    @Test
    fun `enqueueAudio succeeds if existing request with Completed status exists but file doesn't exist`() = runTest {
        val testItem = testItems.first().audio
        downloader.setDownloadsLocation(createTestDownloadsLocation().second)

        assertThat(downloader.enqueueAudio(audio = testItem)).isTrue()

        coEvery { fetch.getDownload(any()) }
            .answerGetDownloadWithStatus(Status.COMPLETED)
        // assuming default mock download file doesn't exist
        assertThat(downloader.enqueueAudio(audio = testItem)).isTrue()
        coVerify { fetch.delete(any<Int>()) }
        snackbarManager.awaitMessages(AudioDownloadQueued)
    }

    @Test
    fun `enqueueAudio succeeds if there's existing request but there is no download info`() = runTest {
        val testItem = testItems.first().audio
        downloader.setDownloadsLocation(createTestDownloadsLocation().second)

        assertThat(downloader.enqueueAudio(audio = testItem)).isTrue()

        coEvery { fetch.getDownload(any()) }
            .answerGetDownload { null }
        // assuming default mock download file doesn't exist
        assertThat(downloader.enqueueAudio(audio = testItem)).isTrue()
        snackbarManager.awaitMessages(AudioDownloadQueued)
    }

    @Test
    fun `enqueueAudio emits error event when fetch fails enqueueing`() = runTest {
        val testItem = testItems.first().audio
        downloader.setDownloadsLocation(createTestDownloadsLocation().second)

        coEvery { fetch.enqueue(any()) }
            .answerFailedEnqueue()
        assertThat(downloader.enqueueAudio(audio = testItem)).isFalse()
        downloader.downloaderEvents.test {
            assertThat(awaitItem())
                .isInstanceOf(DownloaderEvent.DownloaderFetchError::class.java)
        }
    }

    @Test
    fun `enqueueAudio successfully saves given audio and enqueues given audio`() = runTest {
        val testItem = testItems.first().audio
        downloader.setDownloadsLocation(createTestDownloadsLocation().second)

        assertThat(audiosRepo.exists(testItem.id)).isFalse()
        assertThat(downloader.enqueueAudio(audio = testItem)).isTrue()
        assertThat(audiosRepo.exists(testItem.id)).isTrue()
        snackbarManager.awaitMessages(AudioDownloadQueued)
    }

    @Test
    fun `enqueueAudio successfully enqueues given audio id and newDownloadId emits download id`() = runTest {
        val testDownloadItem = testItems.first()
        val testItem = testDownloadItem.audio
        downloader.setDownloadsLocation(createTestDownloadsLocation().second)

        // should fail because audio id isn't in database yet
        assertThat(downloader.enqueueAudio(audioId = testItem.id))
            .isFalse()

        audiosRepo.insert(testItem)
        assertThat(downloader.enqueueAudio(audioId = testItem.id))
            .isTrue()

        downloader.newDownloadId.test {
            assertThat(awaitItem())
                .isEqualTo(testDownloadItem.id)
        }
        snackbarManager.awaitMessages(AudioDownloadQueued)
    }

    @Test
    fun `test download item actions pause, resume, cancel, retry, remove, delete`() = runTest {
        val testItem = testItems.first().audio
        downloader.setDownloadsLocation(createTestDownloadsLocation().second)

        assertThat(downloader.enqueueAudio(audio = testItem)).isTrue()

        downloadRequestsRepo.entry(testItem.id).test {
            val downloadRequest = requireNotNull(awaitItem())

            val downloadItem = AudioDownloadItem(downloadRequest, DownloadInfo(), testItem)

            downloader.pause(downloadItem)
            coVerify { fetch.pause(listOf(downloadItem.downloadInfo.id)) }

            downloader.resume(downloadItem)
            coVerify { fetch.resume(listOf(downloadItem.downloadInfo.id)) }

            downloader.cancel(downloadItem)
            coVerify { fetch.cancel(listOf(downloadItem.downloadInfo.id)) }

            downloader.retry(downloadItem)
            coVerify { fetch.retry(listOf(downloadItem.downloadInfo.id)) }

            downloader.remove(downloadItem)
            coVerify { fetch.remove(listOf(downloadItem.downloadInfo.id)) }
            assertThat(awaitItem()).isNull()

            // re-enqueue because it was just removed above
            assertThat(downloader.enqueueAudio(audio = testItem)).isTrue()
            awaitItem()

            downloader.delete(downloadItem)
            coVerify { fetch.delete(listOf(downloadItem.downloadInfo.id)) }
            assertThat(awaitItem()).isNull()
        }
    }

    @Test
    fun `getAudioDownload return audio download item given status`() = runTest {
        val testItem = testItems.first().audio
        downloader.setDownloadsLocation(createTestDownloadsLocation().second)

        assertThat(downloader.enqueueAudio(audio = testItem)).isTrue()
        // assuming default getAudioDownload allowedStatues is COMPLETED and mocked status is NONE
        assertThat(downloader.getAudioDownload(testItem.id).orNull()?.audio)
            .isNull()
        coEvery { fetch.getDownload(any()) }
            .answerGetDownloadWithStatus(Status.COMPLETED)
        assertThat(downloader.getAudioDownload(testItem.id).orNull()?.audio)
            .isEqualTo(testItem)

        coEvery { fetch.getDownload(any()) }
            .answerGetDownloadWithStatus(Status.FAILED)
        assertThat(downloader.getAudioDownload(testItem.id, Status.FAILED).orNull()?.audio)
            .isEqualTo(testItem)

        coEvery { fetch.getDownload(any()) }
            .answerGetDownload { null }
        assertThat(downloader.getAudioDownload(testItem.id, Status.NONE).orNull()?.audio)
            .isNull()
    }

    @Test
    fun `findAudioDownload returns None when audio doesn't exist`() = runTest {
        assertThat(downloader.findAudioDownload("random"))
            .isEqualTo(None)
    }

    @Test
    fun `findAudioDownload returns Audio when download status isn't Completed but without audioDownloadItem`() = runTest {
        val testItem = testItems.first().audio
        downloader.setDownloadsLocation(createTestDownloadsLocation().second)
        assertThat(downloader.enqueueAudio(audio = testItem)).isTrue()
        val result = downloader.findAudioDownload(testItem.id).value()
        assertThat(result.id)
            .isEqualTo(testItem.id)
        assertThat(result.audioDownloadItem)
            .isNull()
    }

    @Test
    fun `findAudioDownload returns Audio with audioDownloadItem when download status is Completed`() = runTest {
        val testItem = testItems.first().audio
        downloader.setDownloadsLocation(createTestDownloadsLocation().second)
        assertThat(downloader.enqueueAudio(audio = testItem)).isTrue()
        coEvery { fetch.getDownload(any()) }
            .answerGetDownloadWithStatus(Status.COMPLETED)
        val result = downloader.findAudioDownload(testItem.id).value()
        assertThat(result.id)
            .isEqualTo(testItem.id)
        assertThat(result.audioDownloadItem?.audio)
            .isEqualTo(testItem)
    }
}
