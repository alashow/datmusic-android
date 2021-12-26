/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader.observers

import androidx.annotation.StringRes
import com.tonyodev.fetch2.Status
import tm.alashow.datmusic.downloader.R
import tm.alashow.i18n.UiMessage
import tm.alashow.i18n.UiMessageConvertable

enum class DownloadStatusFilter(
    @StringRes private val labelRes: Int,
    val statuses: List<Status> = emptyList()
) : UiMessageConvertable {

    All(R.string.downloads_filter_status_all),
    Completed(R.string.downloads_download_status_completed, listOf(Status.COMPLETED)),
    Downloading(R.string.downloads_download_status_downloading, listOf(Status.DOWNLOADING)),
    Queued(R.string.downloads_download_status_queued, listOf(Status.QUEUED)),
    Paused(R.string.downloads_download_status_paused, listOf(Status.PAUSED)),
    Failed(R.string.downloads_download_status_failed, listOf(Status.FAILED)),
    Cancelled(R.string.downloads_download_status_cancelled, listOf(Status.CANCELLED));

    val isDefault = statuses.isEmpty()
    override fun toUiMessage() = UiMessage.Resource(labelRes)
}
