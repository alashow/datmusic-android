/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.domain.entities.DownloadRequest
import tm.alashow.datmusic.downloader.AudioDownloadItems
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.playback.models.QueueTitle
import tm.alashow.domain.models.Success
import tm.alashow.domain.models.Uninitialized

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val downloader: Downloader,
    private val playbackConnection: PlaybackConnection,
) : ViewModel() {

    val downloadRequests = flow {
        emit(Uninitialized)
        downloader.downloadRequests
            .collect {
                emit(Success(it))
            }
    }

    fun playAudioDownload(audioDownloadItem: AudioDownloadItem) = viewModelScope.launch {
        val downloads = try {
            downloader.downloadRequests.first()[DownloadRequest.Type.Audio] as AudioDownloadItems
        } catch (e: Exception) {
            Timber.e(e)
            return@launch
        }

        val downloadIndex = downloads.indexOfFirst { it.audio.id == audioDownloadItem.audio.id }
        if (downloadIndex < 0) {
            Timber.d("Audio not found in downloads: ${audioDownloadItem.audio.id}")
            Timber.d(downloads.map { it.audio.id }.joinToString())
            return@launch
        } else {
            Timber.d("Audio download index found: $downloadIndex")
        }
        playbackConnection.playAudio(
            audios = downloads.map { it.audio }.toTypedArray(),
            index = downloadIndex,
            title = QueueTitle(QueueTitle.Type.DOWNLOADS)
        )
    }
}
