/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import tm.alashow.datmusic.data.repos.downloads.DownloadManager

@HiltViewModel
class DownloaderViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    val downloadManager: DownloadManager,
) : ViewModel() {

    val permissionEvents = downloadManager.permissionEvents
}
