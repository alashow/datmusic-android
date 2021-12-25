/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader

import com.tonyodev.fetch2.AbstractFetchListener
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2.Status
import com.tonyodev.fetch2.database.DownloadInfo
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun Download.isRetriable() = status in listOf(Status.FAILED, Status.CANCELLED)
fun Download.isResumable() = status in listOf(Status.PAUSED)
fun Download.isPausable() = status in listOf(Status.DOWNLOADING, Status.QUEUED)
fun Download.isCancelable() = status in listOf(Status.DOWNLOADING, Status.QUEUED, Status.PAUSED)
fun Download.isComplete() = status in listOf(Status.COMPLETED)
fun Download.isIncomplete() = status in listOf(Status.CANCELLED, Status.FAILED)
fun Download.progressVisible() = status in listOf(Status.DOWNLOADING, Status.PAUSED, Status.FAILED, Status.CANCELLED, Status.QUEUED)

sealed class FetchEnqueueResult
data class FetchEnqueueSuccessful(val updatedRequest: Request) : FetchEnqueueResult()
data class FetchEnqueueFailed(val error: Error) : FetchEnqueueResult()

sealed class Downloadable(
    val download: Download
)

object DownloadUninitialized : Downloadable(DownloadInfo())

class DownloadCompleted(
    download: Download
) : Downloadable(download)

class DownloadAdded(
    download: Download
) : Downloadable(download)

class DownloadResumed(
    download: Download
) : Downloadable(download)

class DownloadPaused(
    download: Download
) : Downloadable(download)

class DownloadCanceled(
    download: Download
) : Downloadable(download)

class DownloadRemoved(
    download: Download
) : Downloadable(download)

class DownloadDeleted(
    download: Download
) : Downloadable(download)

class DownloadQueued(
    download: Download,
    val waitingOnNetwork: Boolean
) : Downloadable(download)

class DownloadError(
    download: Download,
    val error: Error,
    val throwable: Throwable?
) : Downloadable(download)

class DownloadProgress(
    download: Download,
    val etaInMilliSeconds: Long,
    val downloadedBytesPerSecond: Long
) : Downloadable(download)

fun createFetchListener(fetch: Fetch): Flow<Downloadable?> = callbackFlow {
    val fetchListener = object : AbstractFetchListener() {
        override fun onAdded(download: Download) {
            super.onAdded(download)
            trySend(DownloadAdded(download))
        }

        override fun onCancelled(download: Download) {
            super.onCancelled(download)
            trySend(DownloadCanceled(download))
        }

        override fun onCompleted(download: Download) {
            super.onCompleted(download)
            trySend(DownloadCompleted(download))
        }

        override fun onDeleted(download: Download) {
            super.onDeleted(download)
            trySend(DownloadDeleted(download))
        }

        override fun onError(download: Download, error: Error, throwable: Throwable?) {
            super.onError(download, error, throwable)
            trySend(DownloadError(download, error, throwable))
        }

        override fun onPaused(download: Download) {
            super.onPaused(download)
            trySend(DownloadPaused(download))
        }

        override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
            super.onProgress(download, etaInMilliSeconds, downloadedBytesPerSecond)
            trySend(DownloadProgress(download, etaInMilliSeconds, downloadedBytesPerSecond))
        }

        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
            super.onQueued(download, waitingOnNetwork)
            trySend(DownloadQueued(download, waitingOnNetwork))
        }

        override fun onRemoved(download: Download) {
            super.onRemoved(download)
            trySend(DownloadRemoved(download))
        }

        override fun onResumed(download: Download) {
            super.onResumed(download)
            trySend(DownloadResumed(download))
        }
    }
    fetch.addListener(fetchListener)
    awaitClose {
        fetch.removeListener(fetchListener)
    }
}

suspend fun Fetch.downloads(statuses: List<Status> = emptyList()): List<Download> = suspendCoroutine { continuation ->
    when (statuses.isEmpty()) {
        true -> getDownloads {
            continuation.resume(it)
        }
        else -> getDownloadsWithStatus(statuses) {
            continuation.resume(it)
        }
    }
}

suspend fun Fetch.downloadInfo(id: Int): Download? = suspendCoroutine { continuation ->
    getDownload(id) {
        continuation.resume(it)
    }
}
