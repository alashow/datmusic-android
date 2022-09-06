/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader

import tm.alashow.datmusic.downloader.DownloaderEvent.ChooseDownloadsLocation.message
import tm.alashow.i18n.UiMessage
import tm.alashow.i18n.UiMessageConvertable

typealias DownloaderEvents = List<DownloaderEvent>

data class DownloaderEventsError(val events: DownloaderEvents) : Throwable(), UiMessageConvertable {
    override fun toUiMessage() = events.first().toUiMessage()
}

sealed class DownloaderEvent : UiMessageConvertable {
    object ChooseDownloadsLocation : DownloaderEvent() {
        val message = UiMessage.Resource(R.string.downloader_enqueue_downloadsLocationNotSelected)
    }

    data class DownloaderFetchError(val error: Throwable) : DownloaderEvent()

    override fun toUiMessage() = when (this) {
        is ChooseDownloadsLocation -> message
        is DownloaderFetchError -> UiMessage.Error(this.error)
    }
}
