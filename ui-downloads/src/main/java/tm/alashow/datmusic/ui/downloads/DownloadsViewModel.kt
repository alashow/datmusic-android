/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import tm.alashow.datmusic.downloader.Downloader

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val downloader: tm.alashow.datmusic.downloader.Downloader,
) : ViewModel() {

    val downloadRequests = downloader.downloadRequests
}
