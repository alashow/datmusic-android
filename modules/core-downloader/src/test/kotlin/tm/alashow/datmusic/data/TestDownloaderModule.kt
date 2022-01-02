/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data

import com.tonyodev.fetch2.Fetch
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import io.mockk.every
import io.mockk.spyk
import javax.inject.Singleton
import org.mockito.kotlin.any

@Module
@DisableInstallInCheck
class TestDownloaderModule {
//
//    @Provides
//    @Singleton
//    fun fetch(): Fetch = spy {
//        on { enqueue(any(), any(), any()) }.thenAnswerSuccessfulEnqueueResult()
//        on { getDownload(any(), any()) }.thenAnswerDownloadInfo()
//        on { getDownloads(any(), any()) }.thenAnswerDownloadInfos()
// //        on { getDownloadsWithIdsAndStatus(anyOrNull(), anyOrNull(), anyOrNull()) }.thenAnswerDownloadInfosWithStatus()
//    }

    @Provides
    @Singleton
    fun fetch(): Fetch = spyk {
        every { enqueue(any(), any(), any()) }.answerSuccessfulEnqueue()
        every { getDownload(any(), any()) }.answerGetDownload()
        every { getDownloads(any(), any()) }.answerGetDownloads()
//        on { getDownloadsWithIdsAndStatus(anyOrNull(), anyOrNull(), anyOrNull()) }.thenAnswerDownloadInfosWithStatus()
    }
}
