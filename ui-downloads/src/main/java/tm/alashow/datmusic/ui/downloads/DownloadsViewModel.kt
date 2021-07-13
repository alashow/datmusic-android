/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import tm.alashow.datmusic.data.repos.downloads.DownloadManager

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val downloadManager: DownloadManager,
) : ViewModel() {

    val downloadRequests = downloadManager.downloadRequests
}
