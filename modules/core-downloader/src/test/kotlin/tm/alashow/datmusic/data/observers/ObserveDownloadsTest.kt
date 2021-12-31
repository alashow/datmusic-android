/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.observers

import com.tonyodev.fetch2.Fetch
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import javax.inject.Inject
import kotlinx.coroutines.test.runTest
import org.junit.After
import tm.alashow.base.testing.BaseTest
import tm.alashow.data.PreferencesStore
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.data.db.AppDatabase
import tm.alashow.datmusic.data.db.DatabaseModule
import tm.alashow.datmusic.data.repos.audio.AudiosRepo
import tm.alashow.datmusic.domain.entities.DownloadRequest
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.downloader.DownloaderModule
import tm.alashow.datmusic.downloader.observers.ObserveDownloads

@HiltAndroidTest
@UninstallModules(DatabaseModule::class, DownloaderModule::class)
class ObserveDownloadsTest : BaseTest() {

    @Inject lateinit var database: AppDatabase
    @Inject lateinit var repo: Downloader
    @Inject lateinit var audiosRepo: AudiosRepo
    @Inject lateinit var fetcher: Fetch
    @Inject lateinit var preferencesStore: PreferencesStore
    @Inject lateinit var observeDownloads: ObserveDownloads

    private val testItems = (1..5).map { SampleData.downloadRequest() }
    private val entriesComparator = compareByDescending(DownloadRequest::createdAt).thenBy(DownloadRequest::id)
    private val testParams = ObserveDownloads.Params()

    @After
    fun tearDown() = runTest {
        repo.resetDownloadsLocation()
        database.close()
    }

//    @Test
//    fun `empty list if there are no audio downloads`() = runTest {
//        val params = testParams
//
//        assertThat(observeDownloads.execute(params).audios)
//            .isEmpty()
//    }
}
