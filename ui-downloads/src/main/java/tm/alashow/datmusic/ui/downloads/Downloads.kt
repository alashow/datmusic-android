/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.ui.Scaffold
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.data.repos.downloader.AudioDownloadItems
import tm.alashow.datmusic.data.repos.downloader.DownloadItems
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.domain.entities.DownloadRequest
import tm.alashow.ui.components.AppTopBar
import tm.alashow.ui.components.EmptyErrorBox
import tm.alashow.ui.theme.AppTheme

@Composable
fun Downloads() {
    Downloads(viewModel = hiltViewModel())
}

@Composable
private fun Downloads(viewModel: DownloadsViewModel) {
    val listState = rememberLazyListState()
    val downloads by rememberFlowWithLifecycle(viewModel.downloadRequests).collectAsState(initial = emptyMap())

    Scaffold(
        topBar = {
            AppTopBar(title = stringResource(R.string.downloads_title))
        }
    ) { padding ->
        DownloadsList(
            downloads = downloads,
            listState = listState,
            paddingValues = padding
        )
    }
}

@Composable
fun DownloadsList(
    downloads: DownloadItems,
    listState: LazyListState,
    paddingValues: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    BoxWithConstraints {
        LazyColumn(state = listState, contentPadding = paddingValues, modifier = modifier.fillMaxSize()) {
            if (downloads.all { (_, list) -> list.isEmpty() }) {
                item {
                    EmptyErrorBox(
                        message = stringResource(R.string.downloads_empty),
                        retryVisible = false,
                        maxHeight = maxHeight,
                        maxHeightFraction = .85f
                    )
                }
            }

            downloads.forEach { (type, items) ->
                @Suppress("UNCHECKED_CAST")
                when (type) {
                    DownloadRequest.Type.Audio -> {
                        val audioDownloads = items as AudioDownloadItems
                        items(audioDownloads, { it.downloadRequest.id }) {
                            AudioDownloadItem(it)
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudioDownloadItem(audioDownloadItem: AudioDownloadItem) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AppTheme.specs.padding),
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppTheme.specs.inputPaddings)
    ) {
        Text(audioDownloadItem.audio.buildFileDisplayName())
        Text("Status=${audioDownloadItem.downloadInfo.status}, Progress=${audioDownloadItem.downloadInfo.progress}")
    }
}
