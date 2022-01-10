/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data

import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2.Status
import com.tonyodev.fetch2.database.DownloadInfo
import io.mockk.MockKStubScope
import java.io.IOException
import tm.alashow.datmusic.downloader.manager.DownloadEnqueueFailed
import tm.alashow.datmusic.downloader.manager.DownloadEnqueueResult
import tm.alashow.datmusic.downloader.manager.DownloadEnqueueSuccessful

internal fun Int.toDownloadInfo(status: Status = Status.NONE) = DownloadInfo().also {
    it.id = this
    it.file = "file://$this"
    it.status = status
}

fun MockKStubScope<DownloadEnqueueResult<Request>, DownloadEnqueueResult<Request>>.answerSuccessfulEnqueue() {
    answers {
        val request = firstArg<Request>()
        DownloadEnqueueSuccessful(request)
    }
}

fun MockKStubScope<DownloadEnqueueResult<Request>, DownloadEnqueueResult<Request>>.answerFailedEnqueue() {
    answers {
        DownloadEnqueueFailed(IOException("Test error"))
    }
}

fun MockKStubScope<Download?, Download?>.answerGetDownloadWithStatus(status: Status) = answerGetDownload {
    it.apply { it.status = status }
}

fun MockKStubScope<Download?, Download?>.answerGetDownload(
    transform: (DownloadInfo) -> DownloadInfo? = { it }
) {
    answers {
        val downloadId = firstArg<Int>()
        transform(downloadId.toDownloadInfo())
    }
}

fun MockKStubScope<List<Download>, List<Download>>.answerGetDownloads(
    transform: (DownloadInfo) -> DownloadInfo = { it }
) {
    answers {
        val downloadId = firstArg<List<Int>>()
        downloadId.map { transform(it.toDownloadInfo()) }
    }
}

fun MockKStubScope<List<Download>, List<Download>>.answerGetDownloadsWithIdsAndStatus(
    transform: (DownloadInfo) -> DownloadInfo = { it },
    resultTransform: (List<DownloadInfo>) -> List<DownloadInfo> = { it }
) {
    answers {
        val downloadId = firstArg<Set<Int>>()
        val requestedStatuses = secondArg<List<Status>>()
        val stasuses = if (requestedStatuses.isEmpty()) Status.values().toList() else requestedStatuses
        resultTransform(downloadId.map { transform(it.toDownloadInfo(stasuses.random())) })
    }
}
