/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import javax.annotation.concurrent.Immutable
import tm.alashow.datmusic.downloader.DownloadItems
import tm.alashow.datmusic.downloader.observers.ObserveDownloads
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Uninitialized

@Immutable
internal data class DownloadsViewState(
    val downloads: Async<DownloadItems> = Uninitialized,
    val params: ObserveDownloads.Params = ObserveDownloads.Params()
) {

    companion object {
        val Empty = DownloadsViewState()
    }
}
