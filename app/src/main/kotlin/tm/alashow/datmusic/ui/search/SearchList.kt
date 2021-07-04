/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.ImageLoadState
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import tm.alashow.base.util.extensions.localizedMessage
import tm.alashow.base.util.extensions.localizedTitle
import tm.alashow.common.compose.LocalScaffoldState
import tm.alashow.common.compose.LogCompositions
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.R
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.ui.components.ErrorBox
import tm.alashow.datmusic.ui.components.ProgressIndicator
import tm.alashow.datmusic.ui.components.ProgressIndicatorSmall
import tm.alashow.datmusic.ui.theme.AppTheme
import tm.alashow.domain.models.errors.EmptyResultException

@Composable
internal fun SearchList(viewModel: SearchViewModel, listState: LazyListState, padding: PaddingValues) {
    SearchList(
        viewModel = viewModel,
        audiosLazyPagingItems = rememberFlowWithLifecycle(viewModel.pagedAudioList).collectAsLazyPagingItems(),
        artistsLazyPagingItems = rememberFlowWithLifecycle(viewModel.pagedArtistsList).collectAsLazyPagingItems(),
        albumsLazyPagingItems = rememberFlowWithLifecycle(viewModel.pagedAlbumsList).collectAsLazyPagingItems(),
        listState = listState,
        padding = padding
    )
}

@Composable
internal fun SearchList(
    viewModel: SearchViewModel,
    audiosLazyPagingItems: LazyPagingItems<Audio>,
    artistsLazyPagingItems: LazyPagingItems<Artist>,
    albumsLazyPagingItems: LazyPagingItems<Album>,
    listState: LazyListState,
    padding: PaddingValues,
) {
    // TODO: figure out better way of hoisting this state out without recomposing [SearchList] two levels above (in [Search] screen where viewState is originally hosted
    // which causes pagers to restart/request unnecessarily)
    val viewState by rememberFlowWithLifecycle(viewModel.state).collectAsState(initial = SearchViewState.Empty)
    val searchFilter = viewState.searchFilter

    val pagers = when (searchFilter.backends.size) {
        1 -> searchFilter.backends.map {
            when (it) {
                DatmusicSearchParams.BackendType.AUDIOS -> audiosLazyPagingItems
                DatmusicSearchParams.BackendType.ARTISTS -> artistsLazyPagingItems
                DatmusicSearchParams.BackendType.ALBUMS -> albumsLazyPagingItems
            }
        }.toSet()
        else -> setOf(audiosLazyPagingItems, artistsLazyPagingItems, albumsLazyPagingItems)
    }

    val captchaError = viewState.captchaError
    var captchaErrorShown by remember(captchaError) { mutableStateOf(true) }
    if (captchaError != null) {
        CaptchaErrorDialog(captchaErrorShown, { captchaErrorShown = it }, captchaError) { key ->
            viewModel.submitAction(SearchAction.SolveCaptcha(captchaError, key))
        }
    }

    val pagerRefreshStates = pagers.map { it.loadState.refresh }.toTypedArray()
    val pagersAreEmpty = pagers.all { it.itemCount == 0 }
    val refreshPagers = { pagers.forEach { it.refresh() } }
    val retryPagers = { pagers.forEach { it.retry() } }
    val refreshErrorState = pagerRefreshStates.firstOrNull { it is LoadState.Error }

    val scaffoldState = LocalScaffoldState.current

    val message = stringResource(viewState.error.localizedMessage())
    val retryLabel = stringResource(R.string.error_retry)

    LaunchedEffect(viewState.error) {
        viewState.error?.let {
            when (scaffoldState.snackbarHostState.showSnackbar(message, retryLabel, SnackbarDuration.Long)) {
                SnackbarResult.ActionPerformed -> refreshPagers()
                SnackbarResult.Dismissed -> viewModel.submitAction(SearchAction.ClearError)
            }
        }
    }

    // show snackbar error if there's an error state in any of the active pagers (except empty result errors)
    // and some of the pagers is not empty (in which case full screen error will be shown)
    remember(refreshErrorState, pagersAreEmpty) {
        if (refreshErrorState is LoadState.Error && refreshErrorState.error !is EmptyResultException && !pagersAreEmpty) {
            viewModel.submitAction(SearchAction.AddError(refreshErrorState.error))
        }
    }

    // scroll to top when any of active pagers refresh state change
    LaunchedEffect(*pagerRefreshStates) {
        listState.animateScrollToItem(0)
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(
            isRefreshing = pagers.all { it.itemCount == 0 } && pagerRefreshStates.any { it == LoadState.Loading }
        ),
        onRefresh = { refreshPagers() },
        indicatorPadding = padding,
        indicator = { state, trigger ->
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = trigger,
                scale = true
            )
        }
    ) {
        SearchListContent(
            audiosLazyPagingItems,
            artistsLazyPagingItems,
            albumsLazyPagingItems,
            listState,
            searchFilter,
            pagersAreEmpty,
            retryPagers,
            refreshErrorState,
            padding,
        )
    }
}

@Composable
private fun SearchListContent(
    audiosLazyPagingItems: LazyPagingItems<Audio>,
    artistsLazyPagingItems: LazyPagingItems<Artist>,
    albumsLazyPagingItems: LazyPagingItems<Album>,
    listState: LazyListState,
    searchFilter: SearchFilter,
    pagersAreEmpty: Boolean,
    retryPagers: () -> Unit,
    refreshErrorState: LoadState?,
    padding: PaddingValues
) {
    BoxWithConstraints {
        LazyColumn(
            state = listState,
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            if (refreshErrorState is LoadState.Error) {
                if (pagersAreEmpty)
                    item {
                        ErrorBox(
                            title = stringResource(refreshErrorState.error.localizedTitle()),
                            message = stringResource(refreshErrorState.error.localizedMessage()),
                            onRetryClick = { retryPagers() },
                            maxHeight = this@BoxWithConstraints.maxHeight
                        )
                    }
            }

            item {
                if (searchFilter.backends.contains(DatmusicSearchParams.BackendType.ARTISTS))
                    ArtistList(artistsLazyPagingItems)
                if (searchFilter.backends.contains(DatmusicSearchParams.BackendType.ALBUMS))
                    AlbumList(albumsLazyPagingItems)
            }

            if (searchFilter.backends.contains(DatmusicSearchParams.BackendType.AUDIOS))
                audioList(audiosLazyPagingItems)
        }
    }
}

@Composable
internal fun ArtistList(pagingItems: LazyPagingItems<Artist>) {
    LogCompositions(tag = "ArtistList")
    if (pagingItems.itemCount > 0)
        SearchListLabel(stringResource(R.string.search_artists), pagingItems.loadState)

    LazyRow(Modifier.fillMaxWidth()) {
        items(pagingItems) {
            val artist = it ?: return@items

            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.specs.padding)
            ) {
                val image = rememberCoilPainter(artist.photo, fadeIn = true)
                Image(
                    painter = image,
                    contentDescription = null,
                    Modifier
                        .size(70.dp)
                        .clip(MaterialTheme.shapes.small)
                        .placeholder(
                            visible = image.loadState is ImageLoadState.Loading,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
                Text(artist.name)
            }
        }

        loadingMoreRow(pagingItems)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun AlbumList(pagingItems: LazyPagingItems<Album>) {
    LogCompositions(tag = "AlbumList")

    if (pagingItems.itemCount > 0)
        SearchListLabel(stringResource(R.string.search_albums), pagingItems.loadState)

    LazyRow(Modifier.fillMaxWidth()) {
        items(pagingItems) {
            val album = it ?: return@items

            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.specs.padding)
            ) {
                val image = rememberCoilPainter(album.photo.smallUrl, fadeIn = true)
                Image(
                    painter = image,
                    contentDescription = null,
                    Modifier
                        .size(70.dp)
                        .clip(MaterialTheme.shapes.small)
                        .placeholder(
                            visible = image.loadState is ImageLoadState.Loading,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
                Text(album.title)
            }
        }

        loadingMoreRow(pagingItems)
    }
}

internal fun LazyListScope.audioList(pagingItems: LazyPagingItems<Audio>) {
    if (pagingItems.itemCount > 0)
        item {
            SearchListLabel(stringResource(R.string.search_audios), pagingItems.loadState)
        }

    items(lazyPagingItems = pagingItems) {
        val audio = it ?: return@items
        Row(
            Modifier
                .fillMaxWidth()
                .padding(AppTheme.specs.padding)
        ) {
            val image = rememberCoilPainter(audio.coverUrlSmall, fadeIn = true)
            Image(
                painter = image,
                contentDescription = null,
                Modifier
                    .size(70.dp)
                    .clip(MaterialTheme.shapes.small)
                    .placeholder(
                        visible = image.loadState is ImageLoadState.Loading,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
            Spacer(Modifier.width(AppTheme.specs.padding))
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall)) {
                Text(audio.title)
                Text(audio.artist)
            }
        }
    }

    loadingMore(pagingItems)
}

private fun <T : Any> LazyListScope.loadingMoreRow(pagingItems: LazyPagingItems<T>, modifier: Modifier = Modifier) {
    loadingMore(pagingItems, modifier.height(105.dp))
}

private fun <T : Any> LazyListScope.loadingMore(pagingItems: LazyPagingItems<T>, modifier: Modifier = Modifier) {
    if (pagingItems.loadState.append == LoadState.Loading) {
        item {
            Box(
                modifier
                    .fillMaxWidth()
                    .padding(AppTheme.specs.padding)
            ) {
                ProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun SearchListLabel(label: String, loadState: CombinedLoadStates) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppTheme.specs.padding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label, style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
        )

        AnimatedVisibility(
            visible = loadState.refresh == LoadState.Loading || loadState.source.refresh == LoadState.Loading,
            enter = expandIn(Alignment.Center),
            exit = shrinkOut(Alignment.Center)
        ) {
            ProgressIndicatorSmall()
        }
    }
}
