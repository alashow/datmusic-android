/*
 * Copyright (C) 2022, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.Analytics
import tm.alashow.base.util.IntentUtils
import tm.alashow.base.util.extensions.simpleName
import tm.alashow.base.util.toast
import tm.alashow.common.compose.LocalAnalytics
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.ui.R
import tm.alashow.datmusic.ui.downloader.AudioDownloadItemActionHandler
import tm.alashow.datmusic.ui.downloader.LocalDownloader
import tm.alashow.datmusic.ui.playback.LocalPlaybackConnection

@Composable
fun audioDownloadItemActionHandler(
    downloader: Downloader = LocalDownloader.current,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    clipboardManager: ClipboardManager = LocalClipboardManager.current,
    analytics: Analytics = LocalAnalytics.current,
): AudioDownloadItemActionHandler {
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()

    return { action ->
        analytics.event("downloads.audio.${action.simpleName}", mapOf("id" to action.audio.audio.id))
        coroutine.launch {
            when (action) {
                is AudioDownloadItemAction.Play -> playbackConnection.playAudio(action.audio.audio)
                is AudioDownloadItemAction.PlayNext -> playbackConnection.playNextAudio(action.audio.audio)
                is AudioDownloadItemAction.Pause -> downloader.pause(action.audio)
                is AudioDownloadItemAction.Resume -> downloader.resume(action.audio)
                is AudioDownloadItemAction.Cancel -> downloader.cancel(action.audio)
                is AudioDownloadItemAction.Retry -> downloader.retry(action.audio)
                is AudioDownloadItemAction.Remove -> downloader.remove(action.audio)
                is AudioDownloadItemAction.Delete -> downloader.delete(action.audio)
                is AudioDownloadItemAction.Open -> IntentUtils.openFile(context, action.audio.downloadInfo.fileUri, action.audio.audio.fileMimeType())
                is AudioDownloadItemAction.CopyLink -> {
                    clipboardManager.setText(AnnotatedString(action.audio.audio.downloadUrl ?: ""))
                    context.toast(R.string.generic_clipboard_copied)
                }
                else -> Timber.e("Unhandled action: $action")
            }
        }
    }
}
