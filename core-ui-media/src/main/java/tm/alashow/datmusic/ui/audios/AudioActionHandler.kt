/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.audios

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import tm.alashow.base.util.toast
import tm.alashow.datmusic.ui.downloader.LocalDownloader

typealias AudioActionHandler = (AudioItemAction) -> Unit

@Composable
internal fun AudioActionHandler(): AudioActionHandler {
    val downloader = LocalDownloader.current
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()

    return { action ->
        when (action) {
            is AudioItemAction.Download -> {
                coroutine.launch {
                    downloader.queueAudio(action.audio)
                }
            }
            is AudioItemAction.CopyLink -> {
                context.toast("Not implemented: Copy link")
            }
            is AudioItemAction.Share -> {
                context.toast("Not implemented: Share")
            }
        }
    }
}
