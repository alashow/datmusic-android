/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
@file:Suppress("FunctionName")

package tm.alashow.datmusic.downloader

import com.tonyodev.fetch2.Status
import tm.alashow.i18n.UiMessage

val DownloadsUnknownError = UiMessage.Resource(R.string.error_unknown)
val DownloadsFolderNotFound = UiMessage.Resource(R.string.downloader_enqueue_downloadsNotFound)
val AudioDownloadErrorFileCreate = UiMessage.Resource(R.string.downloader_enqueue_audio_error_fileCreate)
val AudioDownloadErrorInvalidUrl = UiMessage.Resource(R.string.downloader_enqueue_audio_error_invalidUrl)

val AudioDownloadQueued = UiMessage.Resource(R.string.downloader_enqueue_audio_queued)
val AudioDownloadResumedExisting = UiMessage.Resource(R.string.downloader_enqueue_audio_existing_resuming)
val AudioDownloadAlreadyQueued = UiMessage.Resource(R.string.downloader_enqueue_audio_existing_alreadyQueued)
val AudioDownloadAlreadyCompleted = UiMessage.Resource(R.string.downloader_enqueue_audio_existing_completed)
fun AudioDownloadExistingUnknownStatus(status: Status) = UiMessage.Resource(R.string.downloader_enqueue_audio_existing_unknown, listOf(status))
