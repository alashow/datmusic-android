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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import tm.alashow.base.util.extensions.localizedMessage
import tm.alashow.base.util.extensions.localizedTitle
import tm.alashow.common.compose.LocalScaffoldState
import tm.alashow.common.compose.LogCompositions
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.ui.items
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.ui.components.ErrorBox
import tm.alashow.ui.components.ImageWithPlaceholder
import tm.alashow.ui.components.ProgressIndicator
import tm.alashow.ui.components.ProgressIndicatorSmall
import tm.alashow.ui.theme.AppTheme
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

    val hasMultiplePagers = pagers.size > 1
    val hasLoadingPager = pagerRefreshStates.any { it == LoadState.Loading }

    val scaffoldState = LocalScaffoldState.current
    val message = stringResource(viewState.error.localizedMessage())
    val retryLabel = stringResource(R.string.error_retry)

    // show snackbar if there's an error to show
    LaunchedEffect(viewState.error) {
        viewState.error?.let {
            when (scaffoldState.snackbarHostState.showSnackbar(message, retryLabel, SnackbarDuration.Long)) {
                SnackbarResult.ActionPerformed -> refreshPagers()
                SnackbarResult.Dismissed -> viewModel.submitAction(SearchAction.ClearError)
            }
        }
    }

    // add snackbar error if there's an error state in any of the active pagers (except empty result errors)
    // and some of the pagers is not empty (in which case full screen error will be shown)
    remember(refreshErrorState, pagersAreEmpty) {
        if (refreshErrorState is LoadState.Error && !pagersAreEmpty) {
            // we don't wanna show empty results error snackbar when there's multiple pagers and one of the pagers gets empty result error (but we have some results if we are here)
            val emptyResultsButHasMultiplePagers = refreshErrorState.error is EmptyResultException && hasMultiplePagers
            if (emptyResultsButHasMultiplePagers)
                return@remember
            viewModel.submitAction(SearchAction.AddError(refreshErrorState.error))
        }
    }

    // scroll to top when any of active pagers refresh state change
    LaunchedEffect(*pagerRefreshStates) {
        listState.animateScrollToItem(0)
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(
            isRefreshing = pagersAreEmpty && hasLoadingPager
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
    LogCompositions(tag = "SearchListContent")
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
internal fun ArtistList(pagingItems: LazyPagingItems<Artist>, imageSize: Dp = 60.dp) {
    LogCompositions(tag = "ArtistList")
    if (pagingItems.itemCount > 0)
        SearchListLabel(stringResource(R.string.search_artists), pagingItems.loadState)

    LazyRow(Modifier.fillMaxWidth()) {
        items(pagingItems, key = { _, item -> item.id }) {
            val artist = it ?: return@items

            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { }
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.specs.paddingTiny)
            ) {
                val image = rememberCoilPainter(artist.photo, fadeIn = true)
                ImageWithPlaceholder(
                    painter = image,
                    icon = rememberVectorPainter(Icons.Default.Person),
                    shape = CircleShape,
                    size = imageSize
                ) { modifier ->
                    Image(
                        painter = image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = modifier
                    )
                }
                Text(
                    artist.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(100.dp)
                )
            }
        }

        loadingMoreRow(pagingItems, height = imageSize)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun AlbumList(pagingItems: LazyPagingItems<Album>, itemSize: Dp = 150.dp, iconPadding: Dp = 48.dp) {
    LogCompositions(tag = "AlbumList")
    if (pagingItems.itemCount > 0)
        SearchListLabel(stringResource(R.string.search_albums), pagingItems.loadState)

    LazyRow(Modifier.fillMaxWidth()) {
        items(pagingItems, key = { _, item -> item.id }) {
            val album = it ?: return@items

            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall),
                modifier = Modifier
                    .clickable { }
                    .fillMaxWidth()
                    .padding(AppTheme.specs.padding)
            ) {
                val image = rememberCoilPainter(album.photo.mediumUrl, fadeIn = true)
                ImageWithPlaceholder(image, size = itemSize, iconPadding = iconPadding) { modifier ->
                    Image(
                        painter = image,
                        contentDescription = null,
                        modifier = modifier
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingTiny),
                    modifier = Modifier.width(itemSize)
                ) {

                    Text(album.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(album.artists.first().name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(album.year.toString())
                    }
                }
            }
        }

        loadingMoreRow(pagingItems, height = itemSize + 32.dp) // additional height is to account for the vertical padding [loadingMore] adds
    }
}

internal fun LazyListScope.audioList(pagingItems: LazyPagingItems<Audio>) {
    if (pagingItems.itemCount > 0)
        item {
            SearchListLabel(stringResource(R.string.search_audios), pagingItems.loadState)
        }

    items(pagingItems, key = { _, item -> item.id }) {
        val audio = it ?: return@items
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppTheme.specs.padding),
            modifier = Modifier
                .clickable { }
                .fillMaxWidth()
                .padding(AppTheme.specs.inputPaddings)
        ) {
            val image = rememberCoilPainter(audio.coverUrlSmall, fadeIn = true)
            ImageWithPlaceholder(image) { modifier ->
                Image(
                    painter = image,
                    contentDescription = null,
                    modifier = modifier
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall)) {
                Text(audio.title, style = MaterialTheme.typography.body1)
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(audio.artist, style = MaterialTheme.typography.body2)
                }
            }
        }
    }

    loadingMore(pagingItems)
}

private fun <T : Any> LazyListScope.loadingMoreRow(pagingItems: LazyPagingItems<T>, height: Dp = 100.dp, modifier: Modifier = Modifier) {
    loadingMore(pagingItems, modifier.height(height))
}

private fun <T : Any> LazyListScope.loadingMore(pagingItems: LazyPagingItems<T>, modifier: Modifier = Modifier) {
    item {
        val isLoading = remember(pagingItems.loadState) { pagingItems.loadState.source.append == LoadState.Loading || pagingItems.loadState.mediator?.append == LoadState.Loading }
        if (isLoading)
            Box(
                modifier
                    .fillMaxWidth()
                    .padding(AppTheme.specs.padding)
            ) {
                ProgressIndicator(Modifier.align(Alignment.Center))
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
            .padding(horizontal = AppTheme.specs.padding, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label, style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
        )

        AnimatedVisibility(
            visible = loadState.source.refresh == LoadState.Loading || loadState.mediator?.refresh == LoadState.Loading,
            enter = expandIn(Alignment.Center),
            exit = shrinkOut(Alignment.Center)
        ) {
            ProgressIndicatorSmall()
        }
    }
}
