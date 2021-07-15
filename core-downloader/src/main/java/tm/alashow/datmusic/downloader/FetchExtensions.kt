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
import com.tonyodev.fetch2.database.DownloadInfo
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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

suspend fun Fetch.downloads(): List<Download> = suspendCoroutine { continuation ->
    getDownloads {
        continuation.resume(it)
    }
}
