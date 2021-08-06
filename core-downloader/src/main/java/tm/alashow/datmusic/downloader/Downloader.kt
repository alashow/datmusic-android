/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.documentfile.provider.DocumentFile
import com.google.firebase.analytics.FirebaseAnalytics
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2.Status
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileNotFoundException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.base.util.UiMessage
import tm.alashow.base.util.event
import tm.alashow.data.PreferencesStore
import tm.alashow.datmusic.data.db.daos.AudiosDao
import tm.alashow.datmusic.data.db.daos.DownloadRequestsDao
import tm.alashow.datmusic.domain.DownloadsSongsGrouping
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.domain.entities.DownloadItem
import tm.alashow.datmusic.domain.entities.DownloadRequest
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
    private val fetcher: Fetch,
    private val analytics: FirebaseAnalytics,
) {

    companion object {
        const val DOWNLOADS_STATUS_REFRESH_INTERVAL = 1500L
        val DOWNLOADS_LOCATION = stringPreferencesKey("downloads_location")
        val DOWNLOADS_SONGS_GROUPING = stringPreferencesKey("downloads_songs_grouping")
    }

    private val downloaderEventsChannel = Channel<DownloaderEvent>(Channel.CONFLATED)

    private fun downloaderEvent(event: DownloaderEvent) = downloaderEventsChannel.trySend(event)
    private fun downloaderMessage(message: UiMessage<*>) = downloaderEventsChannel.trySend(DownloaderEvent.DownloaderMessage(message))

    val downloaderEvents = downloaderEventsChannel.receiveAsFlow()

    private val fetcherDownloads = flow {
        while (true) {
            emit(fetcher.downloads())
            delay(DOWNLOADS_STATUS_REFRESH_INTERVAL)
        }
    }.distinctUntilChanged()

    /**
     * Emits download requests mapped with download info from [fetcher].
     */
    val downloadRequests: Flow<DownloadItems> = combine(dao.entriesObservable(), fetcherDownloads) { downloadRequests, downloads ->
        val audioRequests = downloadRequests.filter { it.entityType == DownloadRequest.Type.Audio }

        val audioDownloads = audioRequests.map { request ->
            val downloadInfo = downloads.firstOrNull { dl -> dl.id == request.requestId }
            AudioDownloadItem.from(request, request.audio, downloadInfo)
        }.sortedByDescending { it.downloadRequest.createdAt }

        mapOf(DownloadRequest.Type.Audio to audioDownloads)
    }

    /**
     * Audio item pending for download. Used when waiting for download location.
     */
    private var pendingEnqueableAudio: Audio? = null

    suspend fun enqueueAudio(audioId: String) {
        Timber.d("Enqueue requested for: $audioId")
        audiosDao.entry(audioId).firstOrNull()?.apply {
            enqueueAudio(this)
        }
    }

    /**
     * Tries to enqueue given audio or issues error events in case of failure.
     */
    suspend fun enqueueAudio(audio: Audio) {
        Timber.d("Enqueue audio: $audio")
        val downloadsLocation = verifyAndGetDownloadsLocationUri()
        if (downloadsLocation == null) {
            pendingEnqueableAudio = audio
            return
        }

        val downloadRequest = DownloadRequest.fromAudio(audio)
        if (!validateNewAudioRequest(downloadRequest)) {
            return
        }

        val songsGrouping = downloadsSongsGrouping.first()

        val file = try {
            val documents = DocumentFile.fromTreeUri(appContext, downloadsLocation) ?: error("Couldn't resolve downloads location folder")
            audio.documentFile(documents, songsGrouping)
        } catch (e: Exception) {
            Timber.e(e, "Error while creating new audio file")
            if (e is FileNotFoundException) {
                pendingEnqueableAudio = audio
                downloaderMessage(DownloadsFolderNotFound)
                downloaderEvent(DownloaderEvent.ChooseDownloadsLocation)
            } else {
                downloaderMessage(AudioDownloadErrorFileCreate)
            }
            return
        }

        val downloadUrl = audio.downloadUrl
        if (downloadUrl == null) {
            downloaderMessage(AudioDownloadErrorInvalidUrl)
            return
        }

        val request = Request(downloadUrl, file.uri)
        when (val enqueueResult = enqueue(downloadRequest, request)) {
            is FetchEnqueueSuccessful -> {
                Timber.i("Successfully enqueued audio to download")
                downloaderMessage(AudioDownloadQueued)
            }
            is FetchEnqueueFailed -> {
                val error = enqueueResult.error.throwable ?: UnknownError("error while enqueuing")
                Timber.e(error, "Failed to enqueue audio to download")
                downloaderMessage(UiMessage.Error(error))
            }
        }
    }

    /**
     * Validates new audio download request for existence.
     *
     * @return false if not allowed to enqueue again, true otherwise
     */
    private suspend fun validateNewAudioRequest(downloadRequest: DownloadRequest): Boolean {
        val existingRequest = dao.has(downloadRequest.id) > 0

        if (existingRequest) {
            val oldRequest = dao.entry(downloadRequest.id).first()
            val downloadInfo = fetcher.downloadInfo(oldRequest.requestId)
            if (downloadInfo != null) {
                when (downloadInfo.status) {
                    Status.FAILED, Status.CANCELLED -> {
                        fetcher.delete(downloadInfo.id)
                        dao.delete(oldRequest)
                        Timber.i("Retriable download exists, cancelling the old one and allowing enqueue.")
                        return true
                    }
                    Status.PAUSED -> {
                        Timber.i("Resuming paused download because of new request")
                        fetcher.resume(oldRequest.requestId)
                        downloaderMessage(AudioDownloadResumingExisting)
                        return false
                    }
                    Status.NONE, Status.QUEUED -> {
                        Timber.i("File already queued, doing nothing")
                        downloaderMessage(AudioDownloadAlreadyQueued)
                        return false
                    }
                    Status.COMPLETED -> {
                        val fileExists = DocumentFile.fromTreeUri(appContext, downloadInfo.fileUri)?.exists() == true
                        return if (!fileExists) {
                            fetcher.delete(downloadInfo.id)
                            dao.delete(oldRequest)
                            Timber.i("Completed status but file doesn't exist, allowing enqueue.")
                            true
                        } else {
                            Timber.i("Completed status and file exists=$fileExists, doing nothing.")
                            downloaderMessage(AudioDownloadAlreadyCompleted)
                            false
                        }
                    }
                    else -> {
                        Timber.d("Existing download was requested with unhandled status, doing nothing: Status: ${downloadInfo.status}")
                        downloaderMessage(audioDownloadExistingUnknownStatus(downloadInfo.status))
                        return false
                    }
                }
            } else {
                Timber.d("Download request exists but there's no download info, deleting old request and allowing enqueue.")
                dao.delete(oldRequest)
                return true
            }
        }
        return true
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
            try {
                dao.insert(downloadRequest.copy(requestId = newRequest.id))
            } catch (e: Exception) {
                Timber.e(e, "Failed to insert audio request")
                downloaderMessage(UiMessage.Error(e))
            }
        }
        return enqueueResult
    }

    fun pause(vararg downloadItems: DownloadItem) {
        fetcher.pause(downloadItems.map { it.downloadInfo.id })
    }

    fun resume(vararg downloadItems: DownloadItem) {
        fetcher.resume(downloadItems.map { it.downloadInfo.id })
    }

    fun cancel(vararg downloadItems: DownloadItem) {
        fetcher.cancel(downloadItems.map { it.downloadInfo.id })
    }

    fun retry(vararg downloadItems: DownloadItem) {
        fetcher.retry(downloadItems.map { it.downloadInfo.id })
    }

    suspend fun remove(vararg downloadItems: DownloadItem) {
        fetcher.remove(downloadItems.map { it.downloadInfo.id })
        downloadItems.forEach {
            dao.delete(it.downloadRequest)
        }
    }

    suspend fun delete(vararg downloadItems: DownloadItem) {
        fetcher.delete(downloadItems.map { it.downloadInfo.id })
        downloadItems.forEach {
            dao.delete(it.downloadRequest)
        }
    }

    /**
     * Builds [AudioDownloadItem] from given audio id if it exists and satisfies [allowedStatuses].
     */
    suspend fun getAudioDownload(audioId: String, vararg allowedStatuses: Status): Optional<AudioDownloadItem> {
        val existingRequest = dao.has(audioId) > 0
        if (existingRequest) {
            val request = dao.entry(audioId).first()
            val downloadInfo = fetcher.downloadInfo(request.requestId)
            if (downloadInfo != null) {
                if (downloadInfo.status in allowedStatuses)
                    return some(AudioDownloadItem.from(request, request.audio, downloadInfo))
            }
        }
        return None
    }

    private val downloadsLocationUri = preferences.get(DOWNLOADS_LOCATION, "").map {
        when {
            it.isEmpty() -> None
            else -> some(it.toUri())
        }
    }

    val hasDownloadsLocation = downloadsLocationUri.map { it.isSome() }

    val downloadsSongsGrouping = preferences.get(DOWNLOADS_SONGS_GROUPING, "").map { DownloadsSongsGrouping.from(it) }

    suspend fun setDownloadsSongsGrouping(songsGrouping: DownloadsSongsGrouping) {
        analytics.event("downloads.setSongsGrouping", mapOf("type" to songsGrouping.name))
        preferences.save(DOWNLOADS_SONGS_GROUPING, songsGrouping.name)
    }

    fun requestNewDownloadsLocations() = downloaderEvent(DownloaderEvent.ChooseDownloadsLocation)

    suspend fun setDownloadsLocation(uri: Uri) {
        analytics.event("downloads.setDownloadsLocation", mapOf("uri" to uri))
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        appContext.contentResolver.takePersistableUriPermission(uri, takeFlags)
        preferences.save(DOWNLOADS_LOCATION, uri.toString())

        pendingEnqueableAudio?.apply {
            Timber.d("Consuming pending enqueuable audio download")
            enqueueAudio(this)
            pendingEnqueableAudio = null
        }
    }

    private suspend fun verifyAndGetDownloadsLocationUri(): Uri? {
        when (val downloadLocation = downloadsLocationUri.first()) {
            is None -> requestNewDownloadsLocations()
            is Optional.Some -> {
                val uri = downloadLocation()
                val writeableAndReadable =
                    appContext.contentResolver.persistedUriPermissions.firstOrNull { it.uri == uri && it.isWritePermission && it.isReadPermission } != null
                if (!writeableAndReadable) {
                    requestNewDownloadsLocations()
                } else return uri
            }
        }
        return null // we don't have the uri, someone gotta listen for [permissionEvents] to recover from the error
    }
}
