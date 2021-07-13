/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.ui.Scaffold
import tm.alashow.common.compose.rememberFlowWithLifecycle
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
    val downloads by rememberFlowWithLifecycle(viewModel.downloadRequests).collectAsState(initial = listOf())

    Scaffold(
        topBar = {
            AppTopBar(title = stringResource(R.string.downloads_title))
        }
    ) { padding ->
        DownloadsList(
            list = downloads,
            listState = listState,
            paddingValues = padding
        )
    }
}

@Composable
fun DownloadsList(
    list: List<DownloadRequest>,
    listState: LazyListState,
    paddingValues: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    BoxWithConstraints {
        LazyColumn(state = listState, contentPadding = paddingValues, modifier = modifier.fillMaxSize()) {
            if (list.isEmpty()) {
                item {
                    EmptyErrorBox(
                        message = stringResource(R.string.downloads_empty),
                        onRetryClick = {},
                        maxHeight = maxHeight,
                        maxHeightFraction = .85f
                    )
                }
            }
            items(list, key = { it.id }) {
                DownloadRequestItem(it)
            }
        }
    }
}

@Composable
fun DownloadRequestItem(downloadRequest: DownloadRequest) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.padding),
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppTheme.specs.inputPaddings)
    ) {
        Text("Download request:")
        Text("id=${downloadRequest.id}")
        Text("entity=${downloadRequest.entityType}#${downloadRequest.entityId}")
        Text("reqId=${downloadRequest.requestId}")
        Text("download=${downloadRequest.download}")
    }
}
