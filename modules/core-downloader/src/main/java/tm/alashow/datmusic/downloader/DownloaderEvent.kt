/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader

import com.tonyodev.fetch2.Error
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

    data class DownloaderMessage(val message: UiMessage<*>) : DownloaderEvent()
    data class DownloaderFetchError(val error: Error) : DownloaderEvent()

    override fun toUiMessage() = when (this) {
        is ChooseDownloadsLocation -> message
        is DownloaderMessage -> message
        is DownloaderFetchError -> UiMessage.Error(this.error.throwable ?: RuntimeException("Fetch error: ${this.error.name}"))
    }
}
