/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloader

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.common.compose.collectEvent
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.downloader.Downloader.PermissionEvent
import tm.alashow.ui.components.TextRoundedButton
import tm.alashow.ui.theme.AppTheme

val LocalDownloader = staticCompositionLocalOf<Downloader> {
    error("LocalDownloader not provided")
}

@Composable
fun DownloaderHost(content: @Composable () -> Unit) {
    val viewModel = hiltViewModel<DownloaderViewModel>()
    var downloadsLocationDialogShown by remember { mutableStateOf(false) }

    collectEvent(viewModel.downloader.permissionEvents) { event ->
        when (event) {
            PermissionEvent.ChooseDownloadsLocation, PermissionEvent.DownloadLocationPermissionError -> {
                downloadsLocationDialogShown = true
            }
            else -> Unit
        }
    }

    CompositionLocalProvider(LocalDownloader provides viewModel.downloader) {
        DownloadsLocationDialog(downloadsLocationDialogShown, dismissDialog = { downloadsLocationDialogShown = false })
        content()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DownloadsLocationDialog(dialogShown: Boolean, dismissDialog: () -> Unit) {
    val downloader = LocalDownloader.current
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
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = true),
            onDismissRequest = { dismissDialog() },
            title = { Text(stringResource(R.string.downloader_downloadsLocationSelect_title)) },
            text = { Text(stringResource(R.string.downloader_downloadsLocationSelect_text)) },
            buttons = {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppTheme.specs.padding)
                ) {
                    TextRoundedButton(
                        onClick = {
                            dismissDialog()
                            documentTreeLauncher.launch(null)
                        },
                        text = stringResource(R.string.downloader_downloadsLocationSelect_next)
                    )
                }
            },
        )
    }
}
