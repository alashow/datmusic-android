/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader

import android.net.Uri
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.documentfile.provider.DocumentFile
import com.tonyodev.fetch2.Status
import java.io.File
import kotlinx.coroutines.flow.Flow
import tm.alashow.datmusic.domain.DownloadsSongsGrouping
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.domain.entities.DownloadItem
import tm.alashow.domain.models.Optional

typealias AudioDownloadItems = List<AudioDownloadItem>

data class DownloadItems(val audios: AudioDownloadItems = emptyList())

interface Downloader {
    companion object {
        const val DOWNLOADS_STATUS_REFRESH_INTERVAL = 1500L

        internal val DOWNLOADS_LOCATION = stringPreferencesKey("downloads_location")
        internal val DOWNLOADS_SONGS_GROUPING = stringPreferencesKey("downloads_songs_grouping")
    }

    val newDownloadId: Flow<String>
    val downloaderEvents: Flow<DownloaderEvent>
    fun clearDownloaderEvents()
    fun getDownloaderEvents(): List<DownloaderEvent>

    suspend fun enqueueAudio(audioId: String): Boolean
    suspend fun enqueueAudio(audio: Audio): Boolean

    suspend fun pause(vararg downloadItems: DownloadItem)
    suspend fun resume(vararg downloadItems: DownloadItem)
    suspend fun cancel(vararg downloadItems: DownloadItem)
    suspend fun retry(vararg downloadItems: DownloadItem)
    suspend fun remove(vararg downloadItems: DownloadItem)
    suspend fun delete(vararg downloadItems: DownloadItem)

    suspend fun findAudioDownload(audioId: String): Optional<Audio>
    suspend fun getAudioDownload(audioId: String, vararg allowedStatuses: Status = arrayOf(Status.COMPLETED)): Optional<AudioDownloadItem>

    val hasDownloadsLocation: Flow<Boolean>

    fun requestNewDownloadsLocation()
    suspend fun setDownloadsLocation(folder: File)

    @Throws(IllegalArgumentException::class)
    suspend fun setDownloadsLocation(documentFile: DocumentFile)

    suspend fun setDownloadsLocation(uri: Uri?)
    suspend fun resetDownloadsLocation()

    val downloadsSongsGrouping: Flow<DownloadsSongsGrouping>
    suspend fun setDownloadsSongsGrouping(songsGrouping: DownloadsSongsGrouping)
}
