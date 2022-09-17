/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloader

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.WriteableOpenDocumentTree
import tm.alashow.common.compose.collectEvent
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.downloader.DownloaderEvent
import tm.alashow.ui.components.TextRoundedButton

@Composable
fun DownloaderHost(content: @Composable () -> Unit) {
    DownloaderHost(
        downloader = hiltViewModel<DownloaderViewModel>().downloader,
        content = content,
    )
}

@Composable
private fun DownloaderHost(
    downloader: Downloader,
    content: @Composable () -> Unit
) {
    var downloadsLocationDialogShown by remember { mutableStateOf(false) }
    collectEvent(downloader.downloaderEvents) { event ->
        when (event) {
            DownloaderEvent.ChooseDownloadsLocation -> {
                downloadsLocationDialogShown = true
            }
            else -> Unit
        }
    }

    CompositionLocalProvider(LocalDownloader provides downloader) {
        DownloadsLocationDialog(
            dialogShown = downloadsLocationDialogShown,
            onDismiss = { downloadsLocationDialogShown = false }
        )
        content()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DownloadsLocationDialog(
    dialogShown: Boolean,
    downloader: Downloader = LocalDownloader.current,
    onDismiss: () -> Unit,
) {
    val coroutine = rememberCoroutineScope()
    val documentTreeLauncher = rememberLauncherForActivityResult(contract = WriteableOpenDocumentTree()) {
        coroutine.launch {
            try {
                downloader.setDownloadsLocation(it)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    if (dialogShown) {
        // [ColorPalettePreference.Black] theme needs at least 1.dp dialog surfaces
        CompositionLocalProvider(LocalAbsoluteTonalElevation provides 1.dp) {
            AlertDialog(
                properties = DialogProperties(usePlatformDefaultWidth = true),
                onDismissRequest = { onDismiss() },
                title = { Text(stringResource(R.string.downloader_downloadsLocationSelect_title)) },
                text = { Text(stringResource(R.string.downloader_downloadsLocationSelect_text)) },
                dismissButton = {
                    TextRoundedButton(
                        onClick = {
                            onDismiss()
                            documentTreeLauncher.launch(null)
                        },
                        text = stringResource(R.string.downloader_downloadsLocationSelect_next)
                    )
                },
                confirmButton = {},
            )
        }
    }
}
