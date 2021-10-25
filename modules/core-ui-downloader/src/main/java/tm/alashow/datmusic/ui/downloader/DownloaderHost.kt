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
import androidx.compose.material.LocalAbsoluteElevation
import androidx.compose.material.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.base.util.WriteableOpenDocumentTree
import tm.alashow.base.util.asString
import tm.alashow.common.compose.LocalScaffoldState
import tm.alashow.common.compose.collectEvent
import tm.alashow.datmusic.downloader.Downloader
import tm.alashow.datmusic.downloader.DownloaderEvent
import tm.alashow.ui.components.TextRoundedButton
import tm.alashow.ui.theme.AppTheme

val LocalDownloader = staticCompositionLocalOf<Downloader> {
    error("LocalDownloader not provided")
}

@Composable
fun DownloaderHost(
    viewModel: DownloaderViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = LocalScaffoldState.current.snackbarHostState,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()

    var downloadsLocationDialogShown by remember { mutableStateOf(false) }
    collectEvent(viewModel.downloader.downloaderEvents) { event ->
        when (event) {
            DownloaderEvent.ChooseDownloadsLocation -> {
                downloadsLocationDialogShown = true
            }
            is DownloaderEvent.DownloaderMessage -> {
                val message = event.message.asString(context)
                coroutine.launch { snackbarHostState.showSnackbar(message) }
            }
            else -> Unit
        }
    }

    CompositionLocalProvider(LocalDownloader provides viewModel.downloader) {
        DownloadsLocationDialog(dialogShown = downloadsLocationDialogShown, onDismiss = { downloadsLocationDialogShown = false })
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
        CompositionLocalProvider(LocalAbsoluteElevation provides 1.dp) {
            AlertDialog(
                properties = DialogProperties(usePlatformDefaultWidth = true),
                onDismissRequest = { onDismiss() },
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
                                onDismiss()
                                documentTreeLauncher.launch(null)
                            },
                            text = stringResource(R.string.downloader_downloadsLocationSelect_next)
                        )
                    }
                },
            )
        }
    }
}
