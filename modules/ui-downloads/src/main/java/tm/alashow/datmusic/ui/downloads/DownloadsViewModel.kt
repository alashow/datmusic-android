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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.extensions.stateInDefault
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.domain.models.Success
import tm.alashow.domain.models.Uninitialized

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val downloader: Downloader,
    private val playbackConnection: PlaybackConnection,
) : ViewModel() {

    val downloadRequests = downloader.downloadRequests
        .map { Success(it) }
        .stateInDefault(viewModelScope, Uninitialized)

    fun playAudioDownload(audioDownloadItem: AudioDownloadItem) = viewModelScope.launch {
        val downloads = downloader.downloadRequests.first().audios

        val downloadIndex = downloads.indexOfFirst { it.audio.id == audioDownloadItem.audio.id }
        if (downloadIndex < 0) {
            Timber.d("Audio not found in downloads: ${audioDownloadItem.audio.id}")
            Timber.d(downloads.map { it.audio.id }.joinToString())
            return@launch
        } else {
            Timber.d("Audio download index found: $downloadIndex")
        }
        playbackConnection.playFromDownloads(downloadIndex)
    }
}
