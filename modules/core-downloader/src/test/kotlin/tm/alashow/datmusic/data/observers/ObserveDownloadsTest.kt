/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.observers

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import javax.inject.Inject
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import tm.alashow.base.testing.BaseTest
import tm.alashow.data.PreferencesStore
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.createTestDownloadsLocation
import tm.alashow.datmusic.data.db.AppDatabase
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.data.repos.artist.DatmusicArtistDetailsStoreModule
import tm.alashow.datmusic.data.repos.audio.AudiosRepo
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.downloader.observers.DownloadAudioItemSortOption
import tm.alashow.datmusic.downloader.observers.DownloadAudioItemSortOptions
import tm.alashow.datmusic.downloader.observers.DownloadStatusFilter
import tm.alashow.datmusic.downloader.observers.ObserveDownloads

@HiltAndroidTest
@UninstallModules(DatabaseModule::class, DatmusicArtistDetailsStoreModule::class)
class ObserveDownloadsTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var repo: Downloader
    @Inject lateinit var audiosRepo: AudiosRepo
    @Inject lateinit var preferencesStore: PreferencesStore
    @Inject lateinit var observeDownloads: ObserveDownloads

    private val testItems = (1..5).map { SampleData.downloadRequest() }
    private val testParams = ObserveDownloads.Params()

    private fun testAudiosSortOption(sortOption: DownloadAudioItemSortOption) = runTest {
        val params = testParams.copy(audiosSortOption = sortOption)
        val testItems = (1..5).map { SampleData.downloadRequest() }.map { it.audio }
        observeDownloads(params)
        testItems.forEach {
            assertThat(repo.enqueueAudio(audio = it))
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
        repo.setDownloadsLocation(createTestDownloadsLocation().second)
    }

    @After
    fun tearDown() = runTest {
        repo.resetDownloadsLocation()
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

            assertThat(repo.enqueueAudio(audio = testItem))
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
        val params = testParams.copy(statusFilters = setOf(DownloadStatusFilter.Downloading))
        val testItem = testItems.first().audio
        observeDownloads(params)
        observeDownloads.flow.test {
            assertThat(awaitItem().audios)
                .isEmpty()

            assertThat(repo.enqueueAudio(audio = testItem))
                .isTrue()

            assertThat(awaitItem().audios.first().audio)
                .isEqualTo(testItem)
        }
    }
}
