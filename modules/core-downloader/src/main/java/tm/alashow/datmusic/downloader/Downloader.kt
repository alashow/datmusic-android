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
import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2.Status
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.internal.toImmutableList
import timber.log.Timber
import tm.alashow.base.util.CoroutineDispatchers
import tm.alashow.base.util.event
import tm.alashow.data.PreferencesStore
import tm.alashow.data.db.RoomRepo
import tm.alashow.datmusic.data.db.daos.DownloadRequestsDao
import tm.alashow.datmusic.data.repos.audio.AudioSaveType
import tm.alashow.datmusic.data.repos.audio.AudiosRepo
import tm.alashow.datmusic.domain.DownloadsSongsGrouping
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.domain.entities.DownloadItem
import tm.alashow.datmusic.domain.entities.DownloadRequest
import tm.alashow.datmusic.downloader.manager.DownloadEnqueueFailed
import tm.alashow.datmusic.downloader.manager.DownloadEnqueueResult
import tm.alashow.datmusic.downloader.manager.DownloadEnqueueSuccessful
import tm.alashow.datmusic.downloader.manager.FetchDownloadManager
import tm.alashow.domain.models.None
import tm.alashow.domain.models.Optional
import tm.alashow.domain.models.orNone
import tm.alashow.domain.models.orNull
import tm.alashow.domain.models.some
import tm.alashow.i18n.UiMessage

typealias AudioDownloadItems = List<AudioDownloadItem>

data class DownloadItems(val audios: AudioDownloadItems = emptyList())

const val INTENT_READ_WRITE_FLAG = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

class Downloader @Inject constructor(
    @ApplicationContext private val appContext: Context,
    dispatchers: CoroutineDispatchers,
    private val fetcher: FetchDownloadManager,
    private val preferences: PreferencesStore,
    private val dao: DownloadRequestsDao,
    private val audiosRepo: AudiosRepo,
    private val analytics: FirebaseAnalytics,
) : RoomRepo<String, DownloadRequest>(dao, dispatchers) {

    companion object {
        const val DOWNLOADS_STATUS_REFRESH_INTERVAL = 1500L
        val DOWNLOADS_LOCATION = stringPreferencesKey("downloads_location")
        val DOWNLOADS_SONGS_GROUPING = stringPreferencesKey("downloads_songs_grouping")
    }

    private val downloaderEventsChannel = Channel<DownloaderEvent>(Channel.CONFLATED)
    val downloaderEvents = downloaderEventsChannel.receiveAsFlow()

    private val downloaderEventsHistory = mutableListOf<DownloaderEvent>()
    fun clearDownloaderEvents() = downloaderEventsHistory.clear()
    fun getDownloaderEvents() = downloaderEventsHistory.toImmutableList()

    private fun downloaderEvent(event: DownloaderEvent) {
        downloaderEventsChannel.trySend(event)
        downloaderEventsHistory.add(event)
    }

    private fun downloaderMessage(message: UiMessage<*>) = downloaderEvent(DownloaderEvent.DownloaderMessage(message))

    /**
     * Audio item pending for download. Used when waiting for download location.
     */
    private var pendingEnqueableAudio: Audio? = null

    suspend fun enqueueAudio(audioId: String): Boolean {
        Timber.d("Enqueue requested for: $audioId")
        audiosRepo.entry(audioId).firstOrNull()?.apply {
            return enqueueAudio(this)
        }
        return false
    }

    /**
     * Tries to enqueue given audio or issues error events in case of failure.
     */
    suspend fun enqueueAudio(audio: Audio): Boolean {
        Timber.d("Enqueue audio: $audio")
        val downloadRequest = DownloadRequest.fromAudio(audio)
        if (!validateNewAudioRequest(downloadRequest))
            return false

        // save audio to db so Downloads won't depend on given audios existence in audios table
        audiosRepo.saveAudios(AudioSaveType.Download, audio)

        val fileDestination = getAudioDownloadFileDestination(audio)
        if (fileDestination == null) {
            pendingEnqueableAudio = audio
            return false
        }

        if (audio.downloadUrl == null) {
            downloaderMessage(AudioDownloadErrorInvalidUrl)
            return false
        }

        val downloadUrl = Uri.parse(audio.downloadUrl).buildUpon()
            .appendQueryParameter("redirect", "")
            .build()
            .toString()
        val fetchRequest = Request(downloadUrl, fileDestination.uri)

        return when (val enqueueResult = enqueueDownloadRequest(downloadRequest, fetchRequest)) {
            is DownloadEnqueueSuccessful -> {
                downloaderMessage(AudioDownloadQueued)
                true
            }
            is DownloadEnqueueFailed -> {
                Timber.e(enqueueResult.toString())
                downloaderEvent(DownloaderEvent.DownloaderFetchError(enqueueResult.error))
                false
            }
        }
    }

    /**
     * Validates new audio download request for existence.
     *
     * @return false if not allowed to enqueue again, true otherwise
     */
    private suspend fun validateNewAudioRequest(downloadRequest: DownloadRequest): Boolean {
        val existingRequest = dao.exists(downloadRequest.id) > 0

        if (existingRequest) {
            val oldRequest = dao.entry(downloadRequest.id).first()
            val downloadInfo = fetcher.getDownload(oldRequest.requestId)
            if (downloadInfo != null) {
                when (downloadInfo.status) {
                    Status.FAILED, Status.CANCELLED -> {
                        fetcher.delete(downloadInfo.id)
                        delete(oldRequest.id)
                        Timber.i("Retriable download exists, cancelling the old one and allowing enqueue.")
                        return true
                    }
                    Status.PAUSED -> {
                        Timber.i("Resuming paused download because of new request")
                        fetcher.resume(oldRequest.requestId)
                        downloaderMessage(AudioDownloadResumedExisting)
                        return false
                    }
                    Status.NONE, Status.QUEUED, Status.DOWNLOADING -> {
                        Timber.i("File already queued, doing nothing")
                        downloaderMessage(AudioDownloadAlreadyQueued)
                        return false
                    }
                    Status.COMPLETED -> {
                        val fileExists = downloadInfo.fileUri.toDocumentFile(appContext).exists()
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

    private suspend fun enqueueDownloadRequest(downloadRequest: DownloadRequest, request: Request): DownloadEnqueueResult<Request> {
        val enqueueResult = fetcher.enqueue(request)

        if (enqueueResult is DownloadEnqueueSuccessful) {
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

    suspend fun pause(vararg downloadItems: DownloadItem) {
        fetcher.pause(downloadItems.map { it.downloadInfo.id })
    }

    suspend fun resume(vararg downloadItems: DownloadItem) {
        fetcher.resume(downloadItems.map { it.downloadInfo.id })
    }

    suspend fun cancel(vararg downloadItems: DownloadItem) {
        fetcher.cancel(downloadItems.map { it.downloadInfo.id })
    }

    suspend fun retry(vararg downloadItems: DownloadItem) {
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

    suspend fun findAudioDownload(audioId: String): Optional<Audio> = audiosRepo.find(audioId)
        ?.apply { audioDownloadItem = getAudioDownload(id).orNull() }
        .orNone()

    /**
     * Builds [AudioDownloadItem] from given audio id if it exists and satisfies [allowedStatuses].
     */
    suspend fun getAudioDownload(audioId: String, vararg allowedStatuses: Status = arrayOf(Status.COMPLETED)): Optional<AudioDownloadItem> {
        if (dao.exists(audioId) > 0) {
            val request = dao.entry(audioId).first()
            val downloadInfo = fetcher.getDownload(request.requestId)
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

    val downloadsSongsGrouping = preferences.get(DOWNLOADS_SONGS_GROUPING, "")
        .map { DownloadsSongsGrouping.from(it) }

    suspend fun setDownloadsSongsGrouping(songsGrouping: DownloadsSongsGrouping) {
        analytics.event("downloads.setSongsGrouping", mapOf("type" to songsGrouping.name))
        preferences.save(DOWNLOADS_SONGS_GROUPING, songsGrouping.name)
    }

    suspend fun setDownloadsLocation(folder: File) = setDownloadsLocation(DocumentFile.fromFile(folder))

    suspend fun setDownloadsLocation(documentFile: DocumentFile) {
        require(documentFile.exists()) { "Downloads location must be existing" }
        require(documentFile.isDirectory) { "Downloads location must be a directory" }

        setDownloadsLocation(documentFile.uri)
    }

    suspend fun setDownloadsLocation(uri: Uri?) {
        if (uri == null) {
            Timber.e("Downloads URI is null")
            downloaderMessage(DownloadsUnknownError)
            return
        }
        analytics.event("downloads.setDownloadsLocation", mapOf("uri" to uri))
        appContext.contentResolver.takePersistableUriPermission(uri, INTENT_READ_WRITE_FLAG)
        preferences.save(DOWNLOADS_LOCATION, uri.toString())

        pendingEnqueableAudio?.apply {
            Timber.d("Consuming pending enqueuable audio download")
            enqueueAudio(this)
            pendingEnqueableAudio = null
        }
    }

    suspend fun resetDownloadsLocation() {
        analytics.event("downloads.resetDownloadsLocation")
        val current = downloadsLocationUri.first()
        if (current is Optional.Some) {
            appContext.contentResolver.releasePersistableUriPermission(current.value, INTENT_READ_WRITE_FLAG)
        }
        preferences.save(DOWNLOADS_LOCATION, "")
    }

    fun requestNewDownloadsLocation() = downloaderEvent(DownloaderEvent.ChooseDownloadsLocation)

    private suspend fun verifyAndGetDownloadsLocationUri(): Uri? {
        when (val downloadLocation = downloadsLocationUri.first()) {
            is None -> requestNewDownloadsLocation()
            is Optional.Some -> {
                val uri = downloadLocation()
                val writeableAndReadable =
                    appContext.contentResolver.persistedUriPermissions.firstOrNull { it.uri == uri && it.isWritePermission && it.isReadPermission } != null
                if (!writeableAndReadable) {
                    requestNewDownloadsLocation()
                } else return uri
            }
        }
        return null
    }

    private suspend fun getAudioDownloadFileDestination(audio: Audio): DocumentFile? {
        val downloadsLocationUri = verifyAndGetDownloadsLocationUri() ?: return null
        val songsGrouping = downloadsSongsGrouping.first()

        val file = try {
            val downloadsLocationFolder = downloadsLocationUri.toDocumentFile(appContext)
            audio.documentFile(downloadsLocationFolder, songsGrouping)
        } catch (e: Exception) {
            Timber.e(e, "Error while creating new audio file")
            when (e) {
                is FileNotFoundException -> {
                    downloaderMessage(DownloadsFolderNotFound)
                    downloaderEvent(DownloaderEvent.ChooseDownloadsLocation)
                }
                else -> downloaderMessage(AudioDownloadErrorFileCreate)
            }
            return null
        }
        return file
    }
}
