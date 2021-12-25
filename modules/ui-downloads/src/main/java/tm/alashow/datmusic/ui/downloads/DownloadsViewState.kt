/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import tm.alashow.datmusic.downloader.DownloadItems
import tm.alashow.domain.models.Async
import tm.alashow.domain.models.Uninitialized

data class DownloadsViewState(
    val downloads: Async<DownloadItems> = Uninitialized,
    val query: String = "",
) {

    companion object {
        val Empty = DownloadsViewState()
    }
}
