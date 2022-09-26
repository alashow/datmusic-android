/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Status
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow
import tm.alashow.base.util.localizedMessage

internal fun Download.downloadSpeed(): String = when {
    downloadedBytesPerSecond < 0 -> ""
    else -> downloadedBytesPerSecond.humanReadableByteCount() + "/s"
}

private fun Long.humanReadableByteCount(si: Boolean = false): String {
    val unit = if (si) 1000 else 1024

    if (this < unit) return "$this B"

    val exp = (ln(this.toDouble()) / ln(unit.toDouble())).toInt()
    val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1].toString() + ""
    return String.format(Locale.ENGLISH, "%.1f %sB", this / unit.toDouble().pow(exp.toDouble()), pre)
}

@Composable
internal fun Download.fileSizeStatus() = when (status) {
    Status.DOWNLOADING, Status.PAUSED -> {
        when {
            total > 0 -> downloaded.humanReadableByteCount() + " / " + total.humanReadableByteCount()
            downloaded > 0 -> downloaded.humanReadableByteCount()
            else -> ""
        }
    }
    Status.COMPLETED -> total.humanReadableByteCount()
    else -> ""
}

@Composable
internal fun Download.statusLabel() = when (status) {
    Status.PAUSED -> stringResource(R.string.downloads_download_status_paused)
    Status.QUEUED -> stringResource(R.string.downloads_download_status_queued)
    Status.FAILED -> stringResource(R.string.downloads_download_status_failed) + ": " + stringResource(error.throwable.localizedMessage())
    Status.CANCELLED -> stringResource(R.string.downloads_download_status_cancelled)
    Status.DOWNLOADING -> stringResource(R.string.downloads_download_status_downloading)
    else -> ""
}
