/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.launch
import tm.alashow.base.util.IntentUtils
import tm.alashow.base.util.event
import tm.alashow.base.util.extensions.simpleName
import tm.alashow.common.compose.LocalAnalytics
import tm.alashow.common.compose.LocalPlaybackConnection
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.playback.PlaybackConnection
import tm.alashow.datmusic.ui.downloader.LocalDownloader

val LocalAudioDownloadItemActionHandler = staticCompositionLocalOf<AudioDownloadItemActionHandler> {
    error("No AudioDownloadItemActionHandler provided")
}

typealias AudioDownloadItemActionHandler = (AudioDownloadItemAction) -> Unit

@Composable
fun audioDownloadItemActionHandler(
    downloader: Downloader = LocalDownloader.current,
    playbackConnection: PlaybackConnection = LocalPlaybackConnection.current,
    analytics: FirebaseAnalytics = LocalAnalytics.current
): AudioDownloadItemActionHandler {
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()

    return { action ->
        analytics.event("downloads.audio.${action.simpleName}", mapOf("id" to action.audio.audio.id))
        when (action) {
            is AudioDownloadItemAction.Play -> playbackConnection.playAudio(action.audio.audio)
            is AudioDownloadItemAction.PlayNext -> playbackConnection.playNextAudio(action.audio.audio)
            is AudioDownloadItemAction.Pause -> downloader.pause(action.audio)
            is AudioDownloadItemAction.Resume -> downloader.resume(action.audio)
            is AudioDownloadItemAction.Cancel -> downloader.cancel(action.audio)
            is AudioDownloadItemAction.Retry -> downloader.retry(action.audio)
            is AudioDownloadItemAction.Remove -> coroutine.launch { downloader.remove(action.audio) }
            is AudioDownloadItemAction.Delete -> coroutine.launch { downloader.delete(action.audio) }
            is AudioDownloadItemAction.Open -> IntentUtils.openFile(context, action.audio.downloadInfo.fileUri, action.audio.audio.fileMimeType())
        }
    }
}
