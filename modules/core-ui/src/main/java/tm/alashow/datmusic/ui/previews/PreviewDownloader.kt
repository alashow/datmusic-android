/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.previews

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.tonyodev.fetch2.Status
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import tm.alashow.datmusic.domain.DownloadsSongsGrouping
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.domain.entities.DownloadItem
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.downloader.DownloaderEvent
import tm.alashow.domain.models.Optional

internal object PreviewDownloader : Downloader {
    override val newDownloadId: Flow<String> = flowOf()
    override val downloaderEvents: Flow<DownloaderEvent> = flowOf()
    override fun clearDownloaderEvents() {}
    override fun getDownloaderEvents(): List<DownloaderEvent> = emptyList()

    override suspend fun enqueueAudio(audioId: String): Boolean = true
    override suspend fun enqueueAudio(audio: Audio): Boolean = true

    override suspend fun pause(vararg downloadItems: DownloadItem) {}
    override suspend fun resume(vararg downloadItems: DownloadItem) {}
    override suspend fun cancel(vararg downloadItems: DownloadItem) {}
    override suspend fun retry(vararg downloadItems: DownloadItem) {}
    override suspend fun remove(vararg downloadItems: DownloadItem) {}
    override suspend fun delete(vararg downloadItems: DownloadItem) {}

    override suspend fun findAudioDownload(audioId: String): Optional<Audio> = Optional.None
    override suspend fun getAudioDownload(audioId: String, vararg allowedStatuses: Status): Optional<AudioDownloadItem> = Optional.None

    override val hasDownloadsLocation: Flow<Boolean> = flowOf(true)
    override val downloadsSongsGrouping: Flow<DownloadsSongsGrouping> = flowOf(DownloadsSongsGrouping.ByAlbum)

    override suspend fun setDownloadsSongsGrouping(songsGrouping: DownloadsSongsGrouping) {}
    override suspend fun setDownloadsLocation(folder: File) {}
    override suspend fun setDownloadsLocation(documentFile: DocumentFile) {}
    override suspend fun setDownloadsLocation(uri: Uri?) {}
    override suspend fun resetDownloadsLocation() {}

    override fun requestNewDownloadsLocation() {}
}
