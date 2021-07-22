/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.domain.entities

import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.database.DownloadInfo

sealed class DownloadItem(open val downloadRequest: DownloadRequest, open val downloadInfo: Download)
data class AudioDownloadItem(
    override val downloadRequest: DownloadRequest = DownloadRequest(),
    override val downloadInfo: Download = DownloadInfo(),
    val audio: Audio = Audio(),
) : DownloadItem(downloadRequest, downloadInfo) {
    companion object {
        fun from(downloadRequest: DownloadRequest, audio: Audio, downloadInfo: Download? = null) =
            AudioDownloadItem(downloadRequest, downloadInfo ?: DownloadInfo(), audio)
    }
}
