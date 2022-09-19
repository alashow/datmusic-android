/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.downloads

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tm.alashow.base.util.asString
import tm.alashow.base.util.toUiMessage
import tm.alashow.common.compose.LocalIsPreviewMode
import tm.alashow.common.compose.collectEvent
import tm.alashow.common.compose.previews.CombinedPreview
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.data.SampleData
import tm.alashow.datmusic.domain.entities.AudioDownloadItem
import tm.alashow.datmusic.downloader.DownloadItems
import tm.alashow.datmusic.downloader.observers.DownloadAudioItemSortOption
import tm.alashow.datmusic.downloader.observers.DownloadStatusFilter
import tm.alashow.datmusic.downloader.observers.NoResultsForDownloadsFilter
import tm.alashow.datmusic.ui.downloads.audio.AudioDownload
import tm.alashow.datmusic.ui.previews.PreviewDatmusicCore
import tm.alashow.domain.models.Fail
import tm.alashow.domain.models.Loading
import tm.alashow.domain.models.Success
import tm.alashow.domain.models.Uninitialized
import tm.alashow.ui.Delayed
import tm.alashow.ui.LifecycleRespectingBackHandler
import tm.alashow.ui.ProvideScaffoldPadding
import tm.alashow.ui.components.AppBarNavigationIcon
import tm.alashow.ui.components.AppTopBar
import tm.alashow.ui.components.EmptyErrorBox
import tm.alashow.ui.components.FullScreenLoading
import tm.alashow.ui.components.IconButton
import tm.alashow.ui.components.SearchTextField
import tm.alashow.ui.components.SelectableDropdownMenu
import tm.alashow.ui.drawVerticalScrollbar
import tm.alashow.ui.scaffoldPadding
import tm.alashow.ui.theme.AppTheme

@Composable
fun DownloadsRoute(isPreviewMode: Boolean = LocalIsPreviewMode.current) {
    when {
        isPreviewMode -> DownloadsPreview()
        else -> Downloads()
    }
}

@Composable
private fun Downloads(viewModel: DownloadsViewModel = hiltViewModel()) {
    val listState = rememberLazyListState()
    val viewState by rememberFlowWithLifecycle(viewModel.state)

    collectEvent(viewModel.newDownloadPositionEvent) {
        listState.animateScrollToItem(it)
    }

    Downloads(
        viewState = viewState,
        listState = listState,
        onClearFilter = viewModel::onClearFilter,
        onAudiosSortOptionSelect = viewModel::onAudiosSortOptionSelect,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onStatusFilterSelect = viewModel::onStatusFilterSelect,
        onAudioPlay = viewModel::playAudioDownload,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Downloads(
    viewState: DownloadsViewState,
    onClearFilter: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAudiosSortOptionSelect: (DownloadAudioItemSortOption) -> Unit,
    onStatusFilterSelect: (DownloadStatusFilter) -> Unit,
    onAudioPlay: (AudioDownloadItem) -> Unit,
    listState: LazyListState = rememberLazyListState(),
) {
    Scaffold(
        topBar = {
            DownloadsAppBar(
                viewState = viewState,
                onClearFilter = onClearFilter,
                onSearchQueryChange = onSearchQueryChange,
                onAudiosSortOptionSelect = onAudiosSortOptionSelect,
                onStatusFilterSelect = onStatusFilterSelect,
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddings ->
        ProvideScaffoldPadding(paddings) {
            when (val asyncDownloads = viewState.downloads) {
                is Uninitialized, is Loading -> FullScreenLoading()
                is Fail -> DownloadsError(asyncDownloads, Modifier.padding(scaffoldPadding()))
                is Success -> LazyColumn(
                    state = listState,
                    modifier = Modifier.drawVerticalScrollbar(listState),
                    contentPadding = scaffoldPadding(),
                ) {
                    downloadsList(
                        downloads = asyncDownloads(),
                        onAudioPlay = onAudioPlay,
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadsAppBar(
    viewState: DownloadsViewState,
    onClearFilter: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAudiosSortOptionSelect: (DownloadAudioItemSortOption) -> Unit,
    onStatusFilterSelect: (DownloadStatusFilter) -> Unit,
) {
    val downloadsIsEmpty = viewState.downloads is Success && viewState.downloads()!!.audios.isEmpty()
    var filterVisible by remember { mutableStateOf(false) }

    AppTopBar(
        title = stringResource(R.string.downloads_title),
        filterVisible = viewState.params.query.isNotBlank() || filterVisible,
        filterContent = {
            DownloadsFilters(
                searchQuery = viewState.params.query,
                onQueryChange = onSearchQueryChange,
                hasSortingOption = viewState.params.hasSortingOption,
                audiosSortOptions = viewState.params.audiosSortOptions,
                audiosSortOption = viewState.params.audiosSortOption,
                onAudiosSortOptionSelect = onAudiosSortOptionSelect,
                hasStatusFilter = viewState.params.hasStatusFilter,
                statusFilters = viewState.params.statusFilters,
                onStatusFilterSelect = onStatusFilterSelect,
                onClose = { filterVisible = false; onSearchQueryChange("") },
            )
        },
        actions = {
            if (!downloadsIsEmpty)
                IconButton(
                    onClick = { filterVisible = true },
                    onLongClick = { filterVisible = false; onClearFilter() },
                    onLongClickLabel = stringResource(R.string.downloads_filter_clear),
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(AppTheme.specs.iconSizeSmall),
                        tint = if (viewState.params.hasNoFilters) LocalContentColor.current else MaterialTheme.colorScheme.secondary
                    )
                }
        },
    )
}

@Composable
private fun DownloadsFilters(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    hasSortingOption: Boolean,
    hasStatusFilter: Boolean,
    audiosSortOptions: List<DownloadAudioItemSortOption>,
    audiosSortOption: DownloadAudioItemSortOption,
    onAudiosSortOptionSelect: (DownloadAudioItemSortOption) -> Unit,
    statusFilters: Set<DownloadStatusFilter>,
    onStatusFilterSelect: (DownloadStatusFilter) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
) {
    LifecycleRespectingBackHandler(onBack = onClose)
    var iconSize by remember { mutableStateOf(IntSize.Zero) }
    Column(modifier) {
        Row(Modifier.padding(end = AppTheme.specs.padding)) {
            AppBarNavigationIcon(onClick = onClose, modifier = Modifier.onGloballyPositioned { iconSize = it.size })
            SearchTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                hint = stringResource(R.string.downloads_filter_search_hint),
                autoFocus = true,
            )
        }
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                top = AppTheme.specs.paddingTiny,
                start = with(LocalDensity.current) { iconSize.width.toDp() }
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall),
        ) {
            item {
                SelectableDropdownMenu(
                    items = audiosSortOptions,
                    selectedItem = audiosSortOption,
                    onItemSelect = onAudiosSortOptionSelect,
                    border = ButtonDefaults.outlinedButtonBorder,
                    leadingIcon = Icons.Default.Sort,
                    leadingIconColor = if (hasSortingOption) MaterialTheme.colorScheme.secondary else LocalContentColor.current,
                    itemLabelMapper = { it.asString(context) },
                    itemSuffixMapper = {
                        if (it == audiosSortOption) {
                            Spacer(Modifier.width(AppTheme.specs.paddingLarge))
                            Icon(
                                rememberVectorPainter(
                                    if (it.isDescending) Icons.Default.ArrowDownward
                                    else Icons.Default.ArrowUpward
                                ),
                                modifier = Modifier.size(14.dp),
                                contentDescription = null,
                            )
                        }
                    }
                )
            }
            item {
                SelectableDropdownMenu(
                    items = DownloadStatusFilter.values().toList(),
                    selectedItem = statusFilters.first(),
                    selectedItems = statusFilters,
                    onItemSelect = onStatusFilterSelect,
                    leadingIconColor = if (hasStatusFilter) MaterialTheme.colorScheme.secondary else LocalContentColor.current,
                    border = ButtonDefaults.outlinedButtonBorder,
                    leadingIcon = Icons.Default.FilterAlt,
                )
            }
        }
    }
}

@Composable
private fun DownloadsError(asyncDownloads: Fail<DownloadItems>, modifier: Modifier = Modifier) {
    Box(modifier) {
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
                modifier = Modifier.fillMaxSize()
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
            Delayed {
                EmptyErrorBox(
                    message = stringResource(R.string.downloads_empty),
                    retryVisible = false,
                    modifier = Modifier.fillParentMaxHeight()
                )
            }
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

@CombinedPreview
@Composable
fun DownloadsPreview() = PreviewDatmusicCore {
    val sampleDownloads = remember(Unit) { DownloadItems(SampleData.list { audioDownloadItem() }) }
    var viewState by remember { mutableStateOf(DownloadsViewState(downloads = Success(value = sampleDownloads))) }
    Downloads(
        viewState = viewState,
        onClearFilter = {
            viewState = DownloadsViewState(downloads = Success(value = sampleDownloads))
        },
        onSearchQueryChange = { query ->
            viewState = viewState.copy(
                params = viewState.params.copy(query = query),
                downloads = Success(
                    value = sampleDownloads.copy(sampleDownloads.audios.filter { query in it.toString() })
                )
            )
        },
        onAudiosSortOptionSelect = {
            viewState = viewState.copy(
                params = viewState.params.copy(audiosSortOption = it),
                downloads = Success(
                    value = sampleDownloads.copy(sampleDownloads.audios.sortedWith(it.comparator))
                )
            )
        },
        onStatusFilterSelect = { filter ->
            val newFilter = (viewState.params.statusFilters + filter).toHashSet()
            val newFilterStatuses = newFilter.flatMap { it.statuses }
            viewState = viewState.copy(
                params = viewState.params.copy(statusFilters = newFilter),
                downloads = Success(
                    value = sampleDownloads.copy(
                        sampleDownloads.audios.let { audios ->
                            if (filter.isDefault) audios
                            else audios.filter { it.downloadInfo.status in newFilterStatuses }
                        }
                    )
                )
            )
        },
        onAudioPlay = {}
    )
}
