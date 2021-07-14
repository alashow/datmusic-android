/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import tm.alashow.datmusic.downloader.Downloader

@HiltViewModel
class DownloaderViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    val downloader: Downloader,
) : ViewModel()
