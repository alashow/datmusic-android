/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.downloader

import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Status

fun Download.isRetriable() = status in listOf(Status.FAILED, Status.CANCELLED)
fun Download.isResumable() = status in listOf(Status.PAUSED)
fun Download.isQueued() = status in listOf(Status.QUEUED)
fun Download.isPausable() = status in listOf(Status.DOWNLOADING, Status.QUEUED)
fun Download.isCancelable() = status in listOf(Status.DOWNLOADING, Status.QUEUED, Status.PAUSED)
fun Download.isComplete() = status in listOf(Status.COMPLETED)
fun Download.isIncomplete() = status in listOf(Status.CANCELLED, Status.FAILED)
fun Download.progressVisible() = status in listOf(Status.DOWNLOADING, Status.PAUSED, Status.FAILED, Status.CANCELLED, Status.QUEUED)
