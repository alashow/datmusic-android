/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.ui.Scaffold
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.domain.entities.DownloadRequest
import tm.alashow.datmusic.downloader.AudioDownloadItems
import tm.alashow.datmusic.downloader.DownloadItems
import tm.alashow.datmusic.ui.downloads.audio.AudioDownload
import tm.alashow.domain.models.Incomplete
import tm.alashow.domain.models.Success
import tm.alashow.domain.models.Uninitialized
import tm.alashow.ui.components.AppTopBar
import tm.alashow.ui.components.EmptyErrorBox
import tm.alashow.ui.components.FullScreenLoading

@Composable
fun Downloads() {
    Downloads(viewModel = hiltViewModel())
}

@Composable
private fun Downloads(viewModel: DownloadsViewModel) {
    val listState = rememberLazyListState()
    val asyncDownloads by rememberFlowWithLifecycle(viewModel.downloadRequests).collectAsState(initial = Uninitialized)

    Scaffold(
        topBar = {
            AppTopBar(title = stringResource(R.string.downloads_title))
        }
    ) { padding ->
        BoxWithConstraints {
            when (val downloads = asyncDownloads) {
                is Success -> {
                    DownloadsList(
                        downloads = downloads(),
                        listState = listState,
                        paddingValues = padding,
                        onAudioPlay = viewModel::playAudioDownload
                    )
                }
                is Incomplete -> {
                    FullScreenLoading()
                }
            }
        }
    }
}

@Composable
fun DownloadsList(
    downloads: DownloadItems,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(),
    onAudioPlay: (AudioDownloadItem) -> Unit,
) {
    BoxWithConstraints {
        LazyColumn(
            state = listState,
            contentPadding = paddingValues,
            modifier = modifier.fillMaxSize()
        ) {
            val allDownloadsEmpty = downloads.all { (_, list) -> list.isEmpty() }
            if (allDownloadsEmpty) {
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
                        itemsIndexed(audioDownloads, { _, it -> it.downloadRequest.id }) { index, it ->
                            if (index != 0) Divider()
                            AudioDownload(it, onAudioPlay)
                        }
                    }
                }
            }
        }
    }
}
