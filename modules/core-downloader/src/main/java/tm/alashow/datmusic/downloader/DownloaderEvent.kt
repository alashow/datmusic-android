/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader

import tm.alashow.datmusic.downloader.DownloaderEvent.ChooseDownloadsLocation.message
import tm.alashow.i18n.UiMessage

typealias DownloaderEvents = List<DownloaderEvent>

data class DownloaderEventsError(val events: DownloaderEvents) : Throwable()

sealed class DownloaderEvent {
    object ChooseDownloadsLocation : DownloaderEvent() {
        val message = UiMessage.Resource(R.string.downloader_enqueue_downloadsLocationNotSelected)
    }

    data class DownloaderMessage(val message: UiMessage<*>) : DownloaderEvent()

    fun toUiMessage() = when (this) {
        is ChooseDownloadsLocation -> message
        is DownloaderMessage -> message
    }
}
