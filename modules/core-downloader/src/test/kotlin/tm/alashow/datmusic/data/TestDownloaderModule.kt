/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data

import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import io.mockk.coEvery
import io.mockk.mockk
import javax.inject.Singleton
import tm.alashow.datmusic.downloader.manager.FetchDownloadManager

@Module
@DisableInstallInCheck
class TestDownloaderModule {

    @Provides
    @Singleton
    fun fetch(): FetchDownloadManager = mockk {
        coEvery { enqueue(any()) }.answerSuccessfulEnqueue()
        coEvery { getDownload(any()) }.answerGetDownload()
        coEvery { getDownloads() }.answerGetDownloads()
        coEvery { getDownloadsWithIdsAndStatuses(any(), any()) }.answerGetDownloadsWithIdsAndStatus()

        coEvery { resume(id = any()) } returns Unit
        coEvery { pause(id = any()) } returns Unit
        coEvery { cancel(id = any()) } returns Unit
        coEvery { delete(id = any()) } returns Unit
        coEvery { retry(id = any()) } returns Unit
        coEvery { remove(id = any()) } returns Unit

        coEvery { resume(ids = any()) } returns Unit
        coEvery { pause(ids = any()) } returns Unit
        coEvery { cancel(ids = any()) } returns Unit
        coEvery { delete(ids = any()) } returns Unit
        coEvery { retry(ids = any()) } returns Unit
        coEvery { remove(ids = any()) } returns Unit
    }
}
