/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.ui.Scaffold
import tm.alashow.base.util.asString
import tm.alashow.base.util.toUiMessage
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.downloader.DownloadItems
import tm.alashow.datmusic.downloader.observers.NoResultsForDownloadsFilter
import tm.alashow.datmusic.ui.downloads.audio.AudioDownload
import tm.alashow.domain.models.Fail
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success
import tm.alashow.domain.models.Uninitialized
import tm.alashow.ui.components.AppTopBar
import tm.alashow.ui.components.EmptyErrorBox
import tm.alashow.ui.components.IconButton
import tm.alashow.ui.components.SearchTextField
import tm.alashow.ui.components.fullScreenLoading
import tm.alashow.ui.theme.AppTheme

@Composable
fun Downloads() {
    Downloads(viewModel = hiltViewModel())
}

@Composable
private fun Downloads(viewModel: DownloadsViewModel) {
    val listState = rememberLazyListState()
    val viewState by rememberFlowWithLifecycle(viewModel.state).collectAsState(DownloadsViewState.Empty)

    Scaffold(
        topBar = {
            DownloadsAppBar(onSearchQueryChange = viewModel::onSearchQueryChange)
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            state = listState,
            contentPadding = padding,
        ) {
            when (val asyncDownloads = viewState.downloads) {
                is Fail -> downloadsError(asyncDownloads)
                is Success -> downloadsList(asyncDownloads(), viewModel::playAudioDownload)
                is Uninitialized, is Loading -> fullScreenLoading()
            }
        }
    }
}

@Composable
private fun DownloadsAppBar(
    onSearchQueryChange: (String) -> Unit = {},
) {
    var filterActive by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    val onQueryChange = { query: TextFieldValue ->
        searchQuery = query
        onSearchQueryChange(query.text)
    }
    AppTopBar(
        modifier = Modifier.animateContentSize(),
        title = stringResource(R.string.downloads_title),
        // padding to match the height of search content, could be removed when we have more filter
        titleModifier = Modifier.padding(bottom = 12.dp),
        filterActive = searchQuery.text.isNotBlank() || filterActive,
        filterContent = {
            SearchTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                hint = stringResource(R.string.downloads_filter_search_hint),
                autoFocus = true,
            )
        },
        onCloseFilter = {
            filterActive = false
            onQueryChange(TextFieldValue())
        },
        actions = {
            IconButton(onClick = { filterActive = true }) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = null, // TODO:
                    modifier = Modifier.size(AppTheme.specs.iconSizeSmall)
                )
            }
        }
    )
}

private fun LazyListScope.downloadsError(asyncDownloads: Fail<DownloadItems>) {
    item {
        val error = asyncDownloads.error
        val errorMessage = asyncDownloads.error.toUiMessage().asString(LocalContext.current)
        when (error) {
            is NoResultsForDownloadsFilter -> Text(
                text = errorMessage,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.specs.padding)
            )
            else -> EmptyErrorBox(
                message = errorMessage,
                retryVisible = false,
                modifier = Modifier.fillParentMaxHeight()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.downloadsList(
    downloads: DownloadItems,
    onAudioPlay: (AudioDownloadItem) -> Unit
) {
    if (downloads.audios.isEmpty()) {
        item {
            EmptyErrorBox(
                message = stringResource(R.string.downloads_empty),
                retryVisible = false,
                modifier = Modifier.fillParentMaxHeight()
            )
        }
    }

    itemsIndexed(downloads.audios, { _, it -> it.downloadRequest.id }) { index, it ->
        Column(modifier = Modifier.animateItemPlacement()) {
            if (index != 0) Divider()
            AudioDownload(
                audioDownloadItem = it,
                onAudioPlay = onAudioPlay,
            )
        }
    }
}
