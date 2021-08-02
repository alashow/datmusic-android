/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import com.tonyodev.fetch2.Status
import tm.alashow.datmusic.domain.entities.AudioDownloadItem

sealed class AudioDownloadItemAction(open val audio: AudioDownloadItem) {
    data class Play(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)
    data class Resume(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)
    data class Pause(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)
    data class Cancel(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)
    data class Retry(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)
    data class Open(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)
    data class Remove(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)
    data class Delete(override val audio: AudioDownloadItem) : AudioDownloadItemAction(audio)

    companion object {
        fun from(actionLabelRes: Int, audio: AudioDownloadItem) = when (actionLabelRes) {
            R.string.downloads_download_play -> Play(audio)
            R.string.downloads_download_pause -> Pause(audio)
            R.string.downloads_download_resume -> Resume(audio)
            R.string.downloads_download_cancel -> Cancel(audio)
            R.string.downloads_download_retry -> Retry(audio)
            R.string.downloads_download_open -> Open(audio)
            R.string.downloads_download_remove -> Remove(audio)
            R.string.downloads_download_delete -> Delete(audio)
            else -> error("Unknown action: $actionLabelRes")
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
internal fun AudioDownloadDropdownMenu(
    audioDownload: AudioDownloadItem,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onDropdownSelect: (Int) -> Unit
) {
    val items = buildList {
        val downloadInfo = audioDownload.downloadInfo
        add(R.string.downloads_download_play)
        when (downloadInfo.status) {
            Status.DOWNLOADING, Status.QUEUED, Status.PAUSED -> add(R.string.downloads_download_cancel)
            Status.CANCELLED, Status.FAILED -> add(R.string.downloads_download_delete)
            else -> Unit
        }
        if (downloadInfo.status == Status.COMPLETED) {
            add(R.string.downloads_download_open)
            add(R.string.downloads_download_remove)
            add(R.string.downloads_download_delete)
        }
    }

    if (items.isNotEmpty()) {
        IconButton(
            onClick = { onExpandedChange(true) },
            modifier = modifier
        ) {
            Icon(
                painter = rememberVectorPainter(Icons.Default.MoreVert),
                contentDescription = stringResource(R.string.audio_menu_cd),
            )
        }

        Box {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .align(Alignment.Center)
            ) {
                items.forEach { item ->
                    val label = stringResource(item)
                    DropdownMenuItem(
                        onClick = {
                            onExpandedChange(false)
                            onDropdownSelect(item)
                        }
                    ) {
                        Text(text = label)
                    }
                }
            }
        }
    }
}
