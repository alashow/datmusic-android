/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data

import com.tonyodev.fetch2.Fetch
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton
import org.mockito.kotlin.any
import org.mockito.kotlin.spy

@Module
@DisableInstallInCheck
class TestDownloaderModule {

    @Provides
    @Singleton
    fun fetch(): Fetch = spy {
        on { enqueue(any(), any(), any()) }.thenAnswerSuccessfulEnqueueResult()
        on { getDownload(any(), any()) }.thenAnswerDownloadInfo()
    }
}
