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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.data.repos.downloads.DownloadManager
import tm.alashow.datmusic.data.repos.downloads.DownloadManager.*
import tm.alashow.ui.components.TextRoundedButton
import tm.alashow.ui.theme.AppTheme

val LocalDownloadManager = staticCompositionLocalOf<DownloadManager> {
    error("LocalDownloadManager not provided")
}

@Composable
fun DownloaderHost(content: @Composable () -> Unit) {
    val viewModel = hiltViewModel<DownloaderViewModel>()
    var downloadsLocationDialogShown by remember { mutableStateOf(false) }

    val permissionEvent by rememberFlowWithLifecycle(viewModel.permissionEvents).collectAsState(initial = PermissionEvent.None)
    LaunchedEffect(permissionEvent) {
        when (permissionEvent) {
            PermissionEvent.ChooseDownloadsLocation, PermissionEvent.DownloadLocationPermissionError -> {
                downloadsLocationDialogShown = true
            }
            else -> Unit
        }
    }

    CompositionLocalProvider(LocalDownloadManager provides viewModel.downloadManager) {
        DownloadsLocationDialog(downloadsLocationDialogShown, dismissDialog = { downloadsLocationDialogShown = false })
        content()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DownloadsLocationDialog(dialogShown: Boolean, dismissDialog: () -> Unit) {
    val downloadManager = LocalDownloadManager.current
    val coroutine = rememberCoroutineScope()
    val documentTreeLauncher = rememberLauncherForActivityResult(contract = WriteableOpenDocumentTree()) {
        coroutine.launch {
            try {
                downloadManager.setDownloadsLocation(it)
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
