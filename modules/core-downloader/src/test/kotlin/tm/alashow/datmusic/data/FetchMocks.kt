/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data

import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2.Status
import com.tonyodev.fetch2.database.DownloadInfo
import com.tonyodev.fetch2core.Func
import com.tonyodev.fetch2core.Func2
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.doAnswer
import org.mockito.stubbing.OngoingStubbing

fun OngoingStubbing<Fetch>.thenAnswerSuccessfulEnqueueResult(): OngoingStubbing<Fetch> {
    return doAnswer {
        val request = it.arguments[0] as Request
        (it.arguments[1] as Func<Request>).call(request)
        this.getMock()
    }
}

fun OngoingStubbing<Fetch>.thenAnswerFailedEnqueueResult(): OngoingStubbing<Fetch> {
    return doAnswer {
        (it.arguments[2] as Func<Error>).call(Error.FETCH_DATABASE_ERROR)
        this.getMock()
    }
}

fun InvocationOnMock.onGetDownload(downloadInfo: (DownloadInfo) -> DownloadInfo? = { it }): Fetch {
    val downloadId = arguments[0] as Int
    val func = arguments[1] as Func2<Download?>
    func.call(
        downloadInfo(
            DownloadInfo().apply {
                id = downloadId
                file = "file://$downloadId"
            }
        )
    )
    return this.mock as Fetch
}

fun OngoingStubbing<Fetch>.thenAnswerDownloadInfoWithStatus(status: Status) = thenAnswerDownloadInfo { it.apply { this.status = status } }

fun OngoingStubbing<Fetch>.thenAnswerDownloadInfo(
    downloadInfo: (DownloadInfo) -> DownloadInfo? = { it }
): OngoingStubbing<Fetch> {
    return doAnswer {
        it.onGetDownload(downloadInfo)
    }
}
