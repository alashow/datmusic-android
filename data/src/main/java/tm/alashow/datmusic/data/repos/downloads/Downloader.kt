/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.downloads

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.documentfile.provider.DocumentFile
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.Request
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.PreferencesStore
import tm.alashow.datmusic.data.db.daos.DownloadRequestsDao
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.DownloadRequest
import tm.alashow.domain.FetchEnqueueFailed
import tm.alashow.domain.FetchEnqueueResult
import tm.alashow.domain.FetchEnqueueSuccessful
import tm.alashow.domain.createFetchListener
import tm.alashow.domain.downloads
import tm.alashow.domain.models.None
import tm.alashow.domain.models.Optional
import tm.alashow.domain.models.some

class Downloader @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val dispatchers: CoroutineDispatchers,
    private val preferences: PreferencesStore,
    private val dao: DownloadRequestsDao,
    private val fetcher: Fetch
) {

    private val fetchListenerFlow = createFetchListener(fetcher)

    val downlaodsPager = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { dao.entriesPagingSource() }
    ).flow.map { pagingData ->
        val downloads = fetcher.downloads()
        pagingData.map { downloadRequest ->
            if (downloadRequest.hasRequest()) {
                downloadRequest.download = downloads.firstOrNull { it.id == downloadRequest.requestId }
                downloadRequest
            } else downloadRequest
        }
    }

    val downloadRequests = combine(dao.entriesObservable(), fetchListenerFlow) { downloadsRequests, downloadableEvent ->
        val downloads = fetcher.downloads()
        downloadsRequests.map {
            if (it.hasRequest()) {
                it.download = when (it.requestId) {
                    downloadableEvent.download.id -> downloadableEvent.download
                    else -> downloads.firstOrNull { dl -> dl.id == it.requestId }
                }
                it
            } else it
        }
    }

    suspend fun queueAudio(audio: Audio) {
        val downloadsLocation = verifyAndGetDownloadsLocationUri() ?: return

        val documents = DocumentFile.fromTreeUri(appContext, downloadsLocation) ?: error("Documents is null")
        val downloadUrl = audio.downloadUrl ?: error("Audio doesn't have download url")

        val file = documents.createFile(audio.buildFileMimeType(), audio.buildFileDisplayName())
            ?: error("Failed to create download file")

        val request = Request(downloadUrl, file.uri)
        val downloadRequest = DownloadRequest(entityId = audio.id)

        when (val enqueueResult = enqueue(downloadRequest, request)) {
            is FetchEnqueueSuccessful -> {
                Timber.i("Successfully enqueued audio to download")
            }
            is FetchEnqueueFailed -> {
                val error = enqueueResult.error.throwable ?: UnknownError("error while enqueuing")
                Timber.e(error, "Failed to enqueue audio to download")
                throw error
            }
        }
    }

    private suspend fun enqueue(downloadRequest: DownloadRequest, request: Request): FetchEnqueueResult {
        val enqueueResult = suspendCoroutine<FetchEnqueueResult> { continuation ->
            fetcher.enqueue(
                request,
                { request ->
                    continuation.resume(FetchEnqueueSuccessful(request))
                },
                { error ->
                    continuation.resume(FetchEnqueueFailed(error))
                }
            )
        }

        if (enqueueResult is FetchEnqueueSuccessful) {
            val newRequest = enqueueResult.updatedRequest
            dao.insert(downloadRequest.copy(requestId = newRequest.id))
        }
        return enqueueResult
    }

    suspend fun setDownloadsLocation(uri: Uri) {
        Timber.i("Setting new downloads location: $uri")
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        appContext.contentResolver.takePersistableUriPermission(uri, takeFlags)
        preferences.save(downloadsLocationKey, uri.toString())
    }

    private suspend fun getDownloadsLocationUri(): Optional<Uri> {
        val downloadLocation = preferences.get(downloadsLocationKey, "").first()
        if (downloadLocation.isEmpty()) {
            return None
        }
        return some(Uri.parse(downloadLocation))
    }

    enum class PermissionEvent {
        None, ChooseDownloadsLocation, DownloadLocationPermissionError
    }

    private val permissionEventsQueue = Channel<PermissionEvent>(Channel.CONFLATED)
    val permissionEvents = permissionEventsQueue.receiveAsFlow()

    suspend fun verifyAndGetDownloadsLocationUri(): Uri? {
        when (val downloadLocation = getDownloadsLocationUri()) {
            is None -> permissionEventsQueue.trySend(PermissionEvent.ChooseDownloadsLocation)
            is Optional.Some -> {
                val uri = downloadLocation()
                val writeableAndReadable =
                    appContext.contentResolver.persistedUriPermissions.firstOrNull { it.uri == uri && it.isWritePermission && it.isReadPermission } != null
                if (!writeableAndReadable) {
                    permissionEventsQueue.trySend(PermissionEvent.DownloadLocationPermissionError)
                } else return uri
            }
        }
        return null // we don't have the uri, someone gotta listen for [permissionEvents] to recover from the error
    }

    companion object {
        private val downloadsLocationKey = stringPreferencesKey("downloads_location")
    }
}
