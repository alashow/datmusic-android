/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import tm.alashow.base.util.toast
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.ui.downloader.LocalDownloader

typealias AudioDownloadItemActionHandler = (AudioDownloadItemAction) -> Unit

@Composable
internal fun AudioDownloadItemActionHandler(
    downloader: Downloader = LocalDownloader.current,
): AudioDownloadItemActionHandler {
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()

    return { action ->
        when (action) {
            is AudioDownloadItemAction.Pause -> downloader.pause(action.audio)
            is AudioDownloadItemAction.Resume -> downloader.resume(action.audio)
            is AudioDownloadItemAction.Cancel -> downloader.cancel(action.audio)
            is AudioDownloadItemAction.Retry -> downloader.retry(action.audio)
            is AudioDownloadItemAction.Remove -> coroutine.launch { downloader.remove(action.audio) }
            is AudioDownloadItemAction.Delete -> coroutine.launch { downloader.delete(action.audio) }
            else -> context.toast("Not implemented: $action")
        }
    }
}
