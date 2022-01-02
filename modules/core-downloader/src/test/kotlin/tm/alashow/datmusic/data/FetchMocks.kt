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
import io.mockk.MockKStubScope
import org.mockito.invocation.InvocationOnMock
import org.mockito.kotlin.doAnswer
import org.mockito.stubbing.OngoingStubbing

private fun Int.toDownloadInfo(status: Status = Status.NONE) = DownloadInfo().also {
    it.id = this
    it.file = "file://$this"
    it.status = status
}

fun MockKStubScope<Fetch, Fetch>.answerSuccessfulEnqueue() {
    answers {
        val request = firstArg<Request>()
        val callback = secondArg<Func<Request>>()
        callback.call(request)
        self as Fetch
    }
}

fun MockKStubScope<Fetch, Fetch>.answerFailedEnqueue() {
    answers {
        val callback = thirdArg<Func<Error>>()
        callback.call(Error.FETCH_DATABASE_ERROR)
        self as Fetch
    }
}

fun MockKStubScope<Fetch, Fetch>.answerGetDownloadWithStatus(status: Status) = answerGetDownload {
    it.apply { it.status = status }
}

fun MockKStubScope<Fetch, Fetch>.answerGetDownload(
    transform: (DownloadInfo) -> DownloadInfo? = { it }
) {
    answers {
        val downloadId = firstArg<Int>()
        val callback = invocation.args[1] as Func2<Download?>?
        callback?.call(transform(downloadId.toDownloadInfo()))
        self as Fetch
    }
}

fun MockKStubScope<Fetch, Fetch>.answerGetDownloads() {
    answers {
        val downloadId = firstArg<List<Int>>()
        val callback = secondArg<Func<List<Download>>>()
        callback.call(downloadId.map { it.toDownloadInfo() })
        self as Fetch
    }
}

fun MockKStubScope<Fetch, Fetch>.answerGetDownloadsWithStatus() {
    answers {
        val downloadId = firstArg<List<Int>>()
        val callback = secondArg<Func<List<Download>>>()
        callback.call(downloadId.map { it.toDownloadInfo() })
        self as Fetch
    }
}

fun InvocationOnMock.onGetDownload(transform: (DownloadInfo) -> DownloadInfo? = { it }): Fetch {
    val downloadId = arguments[0] as Int
    val callback = arguments[1] as Func2<Download?>
    callback.call(transform(downloadId.toDownloadInfo()))
    return this.mock as Fetch
}

fun InvocationOnMock.onGetDownloads(
    transform: (DownloadInfo) -> DownloadInfo = { it }
): Fetch {
    val downloadIds = arguments[0] as List<Int>
    val callback = arguments[1] as Func<List<Download>>
    callback.call(downloadIds.map { transform(it.toDownloadInfo()) })
    return this.mock as Fetch
}

fun InvocationOnMock.onGetDownloadsWithStatus(
    transform: (DownloadInfo) -> DownloadInfo = { it }
): Fetch {
    val ids = arguments[0] as Set<Int>
    val statuses = arguments[1] as List<Status>
    val callback = arguments[2] as Func<List<Download>>
    val downloads = ids.map { transform(it.toDownloadInfo(statuses.shuffled().first())) }
    callback.call(downloads)
    return this.mock as Fetch
}

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

fun OngoingStubbing<Fetch>.thenAnswerDownloadInfoWithStatus(status: Status) = thenAnswerDownloadInfo {
    it.apply { this.status = status }
}

fun OngoingStubbing<Fetch>.thenAnswerDownloadInfo(
    transform: (DownloadInfo) -> DownloadInfo? = { it }
): OngoingStubbing<Fetch> {
    return doAnswer {
        it.onGetDownload(transform)
    }
}

fun OngoingStubbing<Fetch>.thenAnswerDownloadInfosWithStatus(status: Status) = thenAnswerDownloadInfos {
    it.apply { this.status = status }
}

fun OngoingStubbing<Fetch>.thenAnswerDownloadInfos(
    transform: (DownloadInfo) -> DownloadInfo = { it }
): OngoingStubbing<Fetch> {
    return doAnswer {
        it.onGetDownloads(transform)
    }
}

fun OngoingStubbing<Fetch>.thenAnswerDownloadInfosWithStatus(
    transform: (DownloadInfo) -> DownloadInfo = { it }
): OngoingStubbing<Fetch> {
    return doAnswer {
        it.onGetDownloadsWithStatus(transform)
    }
}
