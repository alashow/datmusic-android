/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.audios

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.launch
import tm.alashow.base.util.event
import tm.alashow.base.util.extensions.simpleName
import tm.alashow.base.util.toast
import tm.alashow.common.compose.LocalAnalytics
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.ui.downloader.LocalDownloader
import tm.alashow.datmusic.ui.media.R

typealias AudioActionHandler = (AudioItemAction) -> Unit

@Composable
internal fun AudioActionHandler(
    downloader: Downloader = LocalDownloader.current,
    clipboardManager: ClipboardManager = LocalClipboardManager.current,
    analytics: FirebaseAnalytics = LocalAnalytics.current
): AudioActionHandler {
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()

    return { action ->
        analytics.event("audio.${action.simpleName}", mapOf("id" to action.audio.id))
        when (action) {
            is AudioItemAction.Download -> coroutine.launch { downloader.enqueueAudio(action.audio) }
            is AudioItemAction.CopyLink -> {
                clipboardManager.setText(AnnotatedString(action.audio.downloadUrl ?: ""))
                context.toast(R.string.generic_clipboard_copied)
            }
        }
    }
}
