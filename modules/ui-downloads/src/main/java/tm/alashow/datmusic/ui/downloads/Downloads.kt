/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import tm.alashow.datmusic.downloader.DownloadItems
import tm.alashow.datmusic.ui.downloads.audio.AudioDownload
import tm.alashow.domain.models.Success
import tm.alashow.domain.models.Uninitialized
import tm.alashow.ui.components.AppTopBar
import tm.alashow.ui.components.EmptyErrorBox
import tm.alashow.ui.components.fullScreenLoading

@Composable
fun Downloads() {
    Downloads(viewModel = hiltViewModel())
}

@Composable
private fun Downloads(viewModel: DownloadsViewModel) {
    val listState = rememberLazyListState()
    val asyncDownloads by rememberFlowWithLifecycle(viewModel.downloadRequests).collectAsState(Uninitialized)

    Scaffold(
        topBar = {
            AppTopBar(title = stringResource(R.string.downloads_title))
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            state = listState,
            contentPadding = padding,
        ) {
            when (val dls = asyncDownloads) {
                is Success -> downloadsList(
                    downloads = dls(),
                    onAudioPlay = viewModel::playAudioDownload
                )
                else -> fullScreenLoading()
            }
        }
    }
}

fun LazyListScope.downloadsList(
    downloads: DownloadItems,
    onAudioPlay: (AudioDownloadItem) -> Unit,
) {
    val downloadsEmpty = downloads.audios.isEmpty()
    if (downloadsEmpty) {
        item {
            EmptyErrorBox(
                message = stringResource(R.string.downloads_empty),
                retryVisible = false,
                modifier = Modifier.fillParentMaxHeight()
            )
        }
    }

    itemsIndexed(downloads.audios, { _, it -> it.downloadRequest.id }) { index, it ->
        if (index != 0) Divider()
        AudioDownload(it, onAudioPlay)
    }
}
