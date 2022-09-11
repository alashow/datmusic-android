/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader.manager

import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2.Status
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import tm.alashow.base.util.extensions.simpleName
import tm.alashow.datmusic.data.db.SQLITE_MAX_VARIABLES

class FetchDownloadManager @Inject constructor(
    private val fetch: Fetch,
) : DownloadManager<Int, Request, Status, Download> {

    private suspend fun Fetch.getDownloadsByIds(ids: List<Int>): List<Download> = suspendCoroutine { continuation ->
        getDownloads(ids) { continuation.resume(it) }
    }

    private suspend fun Fetch.getDownloadsByIdsChunked(
        ids: List<Int>,
        // some sqlite versions have SQLITE_MAX_VARIABLE_NUMBER set to 999, so chunk size should be less than that
        chunkSize: Int = SQLITE_MAX_VARIABLES,
    ): List<Download> {
        return ids.chunked(chunkSize).map { idsChunk ->
            getDownloadsByIds(idsChunk)
        }.flatten()
    }

    private suspend fun Fetch.getDownloadsByIdsAndStatus(ids: Set<Int>, statuses: List<Status>): List<Download> = suspendCoroutine { continuation ->
        getDownloadsWithStatus(statuses) {
            continuation.resume(it.filter { dl -> dl.id in ids })
        }
    }

    override suspend fun enqueue(request: Request): DownloadEnqueueResult<Request> = suspendCoroutine { continuation ->
        fetch.enqueue(
            request,
            { request ->
                continuation.resume(DownloadEnqueueSuccessful(request))
            },
            { error ->
                continuation.resume(DownloadEnqueueFailed(error.throwable ?: IOException("Download error: ${error.simpleName}, code=${error.value}")))
            }
        )
    }

    override suspend fun getDownload(id: Int): Download? = suspendCoroutine { continuation ->
        fetch.getDownload(id) { continuation.resume(it) }
    }

    override suspend fun getDownloads(): List<Download> = suspendCoroutine { continuation ->
        fetch.getDownloads { continuation.resume(it) }
    }

    override suspend fun getDownloadsWithIdsAndStatuses(ids: Set<Int>, statuses: List<Status>): List<Download> {
        return if (ids.isEmpty())
            emptyList()
        else when (statuses.isEmpty()) {
            true -> fetch.getDownloadsByIdsChunked(ids.toList())
            else -> fetch.getDownloadsByIdsAndStatus(ids, statuses)
        }
    }

    override suspend fun getDownloadsWithStatuses(statuses: List<Status>): List<Download> =
        suspendCoroutine { continuation ->
            when (statuses.isEmpty()) {
                true -> fetch.getDownloads { continuation.resume(it) }
                else -> fetch.getDownloadsWithStatus(statuses) { continuation.resume(it) }
            }
        }

    override suspend fun pause(id: Int) {
        fetch.pause(id)
    }

    override suspend fun resume(id: Int) {
        fetch.resume(id)
    }

    override suspend fun cancel(id: Int) {
        fetch.cancel(id)
    }

    override suspend fun retry(id: Int) {
        fetch.retry(id)
    }

    override suspend fun remove(id: Int) {
        fetch.remove(id)
    }

    override suspend fun delete(id: Int) {
        fetch.delete(id)
    }

    override suspend fun pause(ids: List<Int>) {
        fetch.pause(ids)
    }

    override suspend fun resume(ids: List<Int>) {
        fetch.resume(ids)
    }

    override suspend fun cancel(ids: List<Int>) {
        fetch.cancel(ids)
    }

    override suspend fun remove(ids: List<Int>) {
        fetch.remove(ids)
    }

    override suspend fun retry(ids: List<Int>) {
        fetch.retry(ids)
    }

    override suspend fun delete(ids: List<Int>) {
        fetch.delete(ids)
    }
}

// Note: Currently not used
// sealed class Downloadable(
//    val download: Download
// )
//
// object DownloadUninitialized : Downloadable(DownloadInfo())
//
// class DownloadCompleted(
//    download: Download
// ) : Downloadable(download)
//
// class DownloadAdded(
//    download: Download
// ) : Downloadable(download)
//
// class DownloadResumed(
//    download: Download
// ) : Downloadable(download)
//
// class DownloadPaused(
//    download: Download
// ) : Downloadable(download)
//
// class DownloadCanceled(
//    download: Download
// ) : Downloadable(download)
//
// class DownloadRemoved(
//    download: Download
// ) : Downloadable(download)
//
// class DownloadDeleted(
//    download: Download
// ) : Downloadable(download)
//
// class DownloadQueued(
//    download: Download,
//    val waitingOnNetwork: Boolean
// ) : Downloadable(download)
//
// class DownloadError(
//    download: Download,
//    val error: Error,
//    val throwable: Throwable?
// ) : Downloadable(download)
//
// class DownloadProgress(
//    download: Download,
//    val etaInMilliSeconds: Long,
//    val downloadedBytesPerSecond: Long
// ) : Downloadable(download)
//
// fun createFetchListener(fetch: Fetch): Flow<Downloadable?> = callbackFlow {
//    val fetchListener = object : AbstractFetchListener() {
//        override fun onAdded(download: Download) {
//            super.onAdded(download)
//            trySend(DownloadAdded(download))
//        }
//
//        override fun onCancelled(download: Download) {
//            super.onCancelled(download)
//            trySend(DownloadCanceled(download))
//        }
//
//        override fun onCompleted(download: Download) {
//            super.onCompleted(download)
//            trySend(DownloadCompleted(download))
//        }
//
//        override fun onDeleted(download: Download) {
//            super.onDeleted(download)
//            trySend(DownloadDeleted(download))
//        }
//
//        override fun onError(download: Download, error: Error, throwable: Throwable?) {
//            super.onError(download, error, throwable)
//            trySend(DownloadError(download, error, throwable))
//        }
//
//        override fun onPaused(download: Download) {
//            super.onPaused(download)
//            trySend(DownloadPaused(download))
//        }
//
//        override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
//            super.onProgress(download, etaInMilliSeconds, downloadedBytesPerSecond)
//            trySend(DownloadProgress(download, etaInMilliSeconds, downloadedBytesPerSecond))
//        }
//
//        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
//            super.onQueued(download, waitingOnNetwork)
//            trySend(DownloadQueued(download, waitingOnNetwork))
//        }
//
//        override fun onRemoved(download: Download) {
//            super.onRemoved(download)
//            trySend(DownloadRemoved(download))
//        }
//
//        override fun onResumed(download: Download) {
//            super.onResumed(download)
//            trySend(DownloadResumed(download))
//        }
//    }
//    fetch.addListener(fetchListener)
//    awaitClose {
//        fetch.removeListener(fetchListener)
//    }
// }
