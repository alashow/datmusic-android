/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads.audio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.downloader.isCancelable
import tm.alashow.datmusic.downloader.isComplete
import tm.alashow.datmusic.downloader.isIncomplete
import tm.alashow.datmusic.downloader.isResumable
import tm.alashow.datmusic.downloader.isRetriable
import tm.alashow.datmusic.ui.downloads.R
import tm.alashow.ui.components.MoreVerticalIcon

@Composable
internal fun AudioDownloadDropdownMenu(
    audioDownload: AudioDownloadItem,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onDropdownSelect: (actionLabelRes: Int) -> Unit
) {
    val items = buildList {
        val downloadInfo = audioDownload.downloadInfo

        if (downloadInfo.isResumable()) {
            add(R.string.downloads_download_resume)
        }

        if (downloadInfo.isRetriable()) {
            add(R.string.downloads_download_retry)
        }

        if (downloadInfo.isCancelable()) {
            add(R.string.downloads_download_cancel)
        }

        add(R.string.downloads_download_play)
        add(R.string.downloads_download_playNext)
        add(R.string.audio_menu_copyLink)
        add(R.string.playlist_addTo)

        if (downloadInfo.isIncomplete()) {
            add(R.string.downloads_download_delete)
        }

        if (downloadInfo.isComplete()) {
            add(R.string.downloads_download_open)
            add(R.string.downloads_download_remove)
            add(R.string.downloads_download_delete)
        }
    }

    if (items.isNotEmpty()) {
        MoreVerticalIcon(onClick = { onExpandedChange(true) }, modifier = modifier)

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
                        },
                        text = { Text(text = label) }
                    )
                }
            }
        }
    }
}
