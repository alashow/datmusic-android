/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.data.repos.downloader

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.documentfile.provider.DocumentFile
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.Request
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.data.PreferencesStore
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.data.db.daos.DownloadRequestsDao
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.domain.entities.DownloadItem
import tm.alashow.datmusic.domain.entities.DownloadRequest
import tm.alashow.domain.FetchEnqueueFailed
import tm.alashow.domain.FetchEnqueueResult
import tm.alashow.domain.FetchEnqueueSuccessful
import tm.alashow.domain.downloads
import tm.alashow.domain.models.None
import tm.alashow.domain.models.Optional
import tm.alashow.domain.models.some

typealias DownloadItems = Map<DownloadRequest.Type, List<DownloadItem>>
typealias AudioDownloadItems = List<AudioDownloadItem>

class Downloader @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val dispatchers: CoroutineDispatchers,
    private val preferences: PreferencesStore,
    private val dao: DownloadRequestsDao,
    private val audiosDao: AudiosDao,
    private val fetcher: Fetch
) {

    private val fetcherDownloadsRefreshInterval = 1200L
    private val fetcherDownloads = flow {
        while (true) {
            emit(fetcher.downloads())
            delay(fetcherDownloadsRefreshInterval)
        }
    }.distinctUntilChanged()

    val downloadRequests: Flow<DownloadItems> = combine(dao.entriesObservable(), fetcherDownloads) { downloadsRequests, downloads ->
        val audioRequests = downloadsRequests.filter { it.entityType == DownloadRequest.Type.Audio }
        val audios = audiosDao.entriesById(audioRequests.map { it.entityId }).first()
        val audioDownloads = audios.map { audio ->
            val request = audioRequests.first { it.entityId == audio.id }
            val downloadInfo = downloads.firstOrNull { dl -> dl.id == request.requestId }
            AudioDownloadItem.from(request, audio, downloadInfo)
        }

        mapOf(DownloadRequest.Type.Audio to audioDownloads)
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
        ChooseDownloadsLocation, DownloadLocationPermissionError
    }

    private val permissionEventsQueue = Channel<PermissionEvent>(Channel.CONFLATED)
    val permissionEvents = permissionEventsQueue.receiveAsFlow()

    private suspend fun verifyAndGetDownloadsLocationUri(): Uri? {
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
