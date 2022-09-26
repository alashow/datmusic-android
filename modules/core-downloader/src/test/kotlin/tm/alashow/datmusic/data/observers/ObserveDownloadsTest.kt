/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.observers

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.coEvery
import javax.inject.Inject
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import tm.alashow.base.testing.BaseTest
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.answerGetDownloadsWithIdsAndStatus
import tm.alashow.datmusic.data.createTestDownloadsLocation
import tm.alashow.datmusic.data.db.AppDatabase
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.downloader.DownloadItems
import tm.alashow.datmusic.downloader.DownloadRequestsRepo
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.downloader.manager.FetchDownloadManager
import tm.alashow.datmusic.downloader.observers.*
import tm.alashow.domain.models.Fail
import tm.alashow.domain.models.Loading

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
class ObserveDownloadsTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var downloader: Downloader
    @Inject lateinit var repo: DownloadRequestsRepo
    @Inject lateinit var observeDownloads: ObserveDownloads
    @Inject lateinit var fetcher: FetchDownloadManager

    private val testItems = (1..5).map { SampleData.downloadRequest() }
    private val testParams = ObserveDownloads.Params()

    private fun Audio.toTestQueries() = listOf(title, artist, album ?: "")

    private fun testAudiosSortOption(sortOption: DownloadAudioItemSortOption) = runTest {
        val params = testParams.copy(audiosSortOption = sortOption)
        val testItems = (1..5).map { SampleData.downloadRequest() }.map { it.audio }
        observeDownloads(params)
        testItems.forEach {
            assertThat(downloader.enqueueAudio(audio = it))
                .isTrue()
        }
        observeDownloads.flow.test {
            val audioDownloads = awaitItem().audios
            assertThat(audioDownloads)
                .isEqualTo(audioDownloads.sortedWith(sortOption.comparator))
            assertThat(audioDownloads.map { it.audio })
                .containsExactlyElementsIn(testItems)
        }
        repo.deleteAll()
    }

    @Before
    override fun setUp() = runTest {
        super.setUp()
        downloader.setDownloadsLocation(createTestDownloadsLocation().second)
    }

    @After
    fun tearDown() = runTest {
        downloader.resetDownloadsLocation()
        database.close()
    }

    @Test
    fun `empty list if there are no audio downloads`() = runTest {
        assertThat(observeDownloads.execute(testParams).audios)
            .isEmpty()
    }

    @Test
    fun `returns list of audio downloads`() = runTest {
        val testItem = testItems.first().audio
        observeDownloads(testParams)
        observeDownloads.flow.test {
            assertThat(awaitItem().audios)
                .isEmpty()

            assertThat(downloader.enqueueAudio(audio = testItem))
                .isTrue()

            assertThat(awaitItem().audios.first().audio)
                .isEqualTo(testItem)
        }
    }

    @Test
    fun `returns list of audio downloads sorted by given sort option`() = runTest {
        DownloadAudioItemSortOptions.ALL.forEach {
            testAudiosSortOption(it)
            testAudiosSortOption(it.toggleDescending())
        }
    }

    @Test
    fun `returns list of audio downloads filtered by status`() = runTest {
        val params = testParams.copy(statusFilters = hashSetOf(DownloadStatusFilter.Downloading))
        val testItem = testItems.first().audio
        observeDownloads(params)
        observeDownloads.flow.test {
            assertThat(awaitItem().audios)
                .isEmpty()

            assertThat(downloader.enqueueAudio(audio = testItem))
                .isTrue()

            assertThat(awaitItem().audios.first().audio)
                .isEqualTo(testItem)
        }
    }

    @Test
    fun `returns list of audio downloads filtered by search query`() = runTest {
        val testItem = testItems.first().audio
        assertThat(downloader.enqueueAudio(audio = testItem))
            .isTrue()
        testItem.toTestQueries().forEach {
            val params = testParams.copy(query = it)
            observeDownloads(params)
            observeDownloads.flow.test {
                assertThat(awaitItem().audios.first().audio)
                    .isEqualTo(testItem)
            }
        }
    }

    @Test
    fun `returns empty list if audio downloads filtered by status is empty then returns items when filters changed`() = runTest {
        val params = testParams.copy(statusFilters = hashSetOf(DownloadStatusFilter.Downloading))
        val testItem = testItems.first().audio

        assertThat(downloader.enqueueAudio(audio = testItem))
            .isTrue()
        coEvery { fetcher.getDownloadsWithIdsAndStatuses(any(), any()) }
            .answerGetDownloadsWithIdsAndStatus {
                emptyList()
            }
        observeDownloads(params)
        observeDownloads.flow.test {
            assertThat(awaitItem().audios)
                .isEmpty()
            coEvery { fetcher.getDownloadsWithIdsAndStatuses(any(), any()) }
                .answerGetDownloadsWithIdsAndStatus()
            observeDownloads(params.copy(statusFilters = hashSetOf(DownloadStatusFilter.Queued)))
            assertThat(awaitItem().audios.first().audio)
                .isEqualTo(testItem)
        }
    }

    @Test
    fun `fails with NoResults if failWithNoResultsIfEmpty is applied to flow and status filters are used`() = runTest {
        val params = testParams.copy(statusFilters = hashSetOf(DownloadStatusFilter.Paused))
        val testItem = testItems.first().audio

        assertThat(downloader.enqueueAudio(audio = testItem))
            .isTrue()
        coEvery { fetcher.getDownloadsWithIdsAndStatuses(any(), any()) }
            .answerGetDownloadsWithIdsAndStatus {
                emptyList()
            }
        observeDownloads(params)
        observeDownloads.asyncFlow.test {
            assertThat(awaitItem())
                .isEqualTo(Loading<DownloadItems>())
            assertThat(awaitItem().failWithNoResultsIfEmpty(params))
                .isEqualTo(Fail<DownloadItems>(NoResultsForDownloadsFilter(params)))
        }
    }

    @Test
    fun `fails with NoResults if failWithNoResultsIfEmpty is applied to flow and non matching search query is used`() = runTest {
        val params = testParams.copy(query = "random string")
        val testItem = testItems.first().audio

        assertThat(downloader.enqueueAudio(audio = testItem))
            .isTrue()
        observeDownloads(params)
        observeDownloads.asyncFlow.test {
            assertThat(awaitItem())
                .isEqualTo(Loading<DownloadItems>())
            assertThat(awaitItem().failWithNoResultsIfEmpty(params))
                .isEqualTo(Fail<DownloadItems>(NoResultsForDownloadsFilter(params)))
        }
    }
}
