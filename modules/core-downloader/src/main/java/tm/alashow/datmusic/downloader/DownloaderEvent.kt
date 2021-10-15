/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader

import tm.alashow.i18n.UiMessage

sealed class DownloaderEvent {
    object ChooseDownloadsLocation : DownloaderEvent()
    data class DownloaderMessage(val message: UiMessage<*>) : DownloaderEvent()
}
