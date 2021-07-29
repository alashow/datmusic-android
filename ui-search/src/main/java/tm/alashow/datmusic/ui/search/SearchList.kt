/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import tm.alashow.base.util.extensions.localizedMessage
import tm.alashow.base.util.extensions.localizedTitle
import tm.alashow.common.compose.LocalScaffoldState
import tm.alashow.common.compose.LogCompositions
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.data.repos.search.DatmusicSearchParams.BackendType
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.ui.albums.AlbumColumn
import tm.alashow.datmusic.ui.albums.AlbumsDefaults
import tm.alashow.datmusic.ui.artists.ArtistColumn
import tm.alashow.datmusic.ui.artists.ArtistsDefaults
import tm.alashow.datmusic.ui.audios.AudioRow
import tm.alashow.domain.models.errors.EmptyResultException
import tm.alashow.navigation.LeafScreen
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.ui.Delayed
import tm.alashow.ui.components.ErrorBox
import tm.alashow.ui.components.ProgressIndicator
import tm.alashow.ui.components.ProgressIndicatorSmall
import tm.alashow.ui.items
import tm.alashow.ui.theme.AppTheme

@Composable
internal fun SearchList(viewModel: SearchViewModel, listState: LazyListState, padding: PaddingValues) {
    SearchList(
        viewModel = viewModel,
        audiosLazyPagingItems = rememberFlowWithLifecycle(viewModel.pagedAudioList).collectAsLazyPagingItems(),
        minervaLazyPagingItems = rememberFlowWithLifecycle(viewModel.pagedMinervaList).collectAsLazyPagingItems(),
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
    minervaLazyPagingItems: LazyPagingItems<Audio>,
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
                BackendType.AUDIOS -> audiosLazyPagingItems
                BackendType.ARTISTS -> artistsLazyPagingItems
                BackendType.ALBUMS -> albumsLazyPagingItems
                BackendType.MINERVA -> minervaLazyPagingItems
            }
        }.toSet()
        else -> setOf(audiosLazyPagingItems, artistsLazyPagingItems, albumsLazyPagingItems)
    }

    val pagerRefreshStates = pagers.map { it.loadState.refresh }.toTypedArray()
    val pagersAreEmpty = pagers.all { it.itemCount == 0 }
    val refreshPagers = { pagers.forEach { it.refresh() } }
    val retryPagers = { pagers.forEach { it.retry() } }
    val refreshErrorState = pagerRefreshStates.firstOrNull { it is LoadState.Error }

    val hasMultiplePagers = pagers.size > 1

    LaunchedEffect(*pagerRefreshStates) {
        listState.animateScrollToItem(0)
    }

    SearchListErrors(viewModel, viewState, refreshPagers, refreshErrorState, pagersAreEmpty, hasMultiplePagers)

    SwipeRefresh(
        state = rememberSwipeRefreshState(
            isRefreshing = false
        ),
        onRefresh = { refreshPagers() },
        indicatorPadding = padding,
        indicator = { state, trigger ->
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = trigger,
                scale = true
            )
        },
    ) {
        SearchListContent(
            audiosLazyPagingItems,
            minervaLazyPagingItems,
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
private fun SearchListErrors(
    viewModel: SearchViewModel,
    viewState: SearchViewState,
    refreshPagers: () -> Unit,
    refreshErrorState: LoadState?,
    pagersAreEmpty: Boolean,
    hasMultiplePagers: Boolean,
    scaffoldState: ScaffoldState = LocalScaffoldState.current,
) {
    val captchaError = viewState.captchaError
    var captchaErrorShown by remember(captchaError) { mutableStateOf(true) }
    if (captchaError != null) {
        CaptchaErrorDialog(captchaErrorShown, { captchaErrorShown = it }, captchaError) { solution ->
            viewModel.submitAction(SearchAction.SubmitCaptcha(captchaError, solution))
        }
    }

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
    LaunchedEffect(refreshErrorState, pagersAreEmpty) {
        if (refreshErrorState is LoadState.Error && !pagersAreEmpty) {
            // we don't wanna show empty results error snackbar when there's multiple pagers and one of the pagers gets empty result error (but we have some results if we are here)
            val emptyResultsButHasMultiplePagers = refreshErrorState.error is EmptyResultException && hasMultiplePagers
            if (emptyResultsButHasMultiplePagers)
                return@LaunchedEffect
            viewModel.submitAction(SearchAction.AddError(refreshErrorState.error))
        }
    }
}

@Composable
private fun SearchListContent(
    audiosLazyPagingItems: LazyPagingItems<Audio>,
    minervaAudiosLazyPagingItems: LazyPagingItems<Audio>,
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
            if (refreshErrorState is LoadState.Error && pagersAreEmpty) {
                item {
                    Delayed {
                        ErrorBox(
                            title = stringResource(refreshErrorState.error.localizedTitle()),
                            message = stringResource(refreshErrorState.error.localizedMessage()),
                            onRetryClick = { retryPagers() },
                            maxHeight = this@BoxWithConstraints.maxHeight
                        )
                    }
                }
            }
            item("artists_albums") {
                // TODO: separate these into diff [item]s after swiperefresh bug fix
                if (searchFilter.hasArtists)
                    ArtistList(artistsLazyPagingItems)
                if (searchFilter.hasAlbums)
                    AlbumList(albumsLazyPagingItems)
            }

            if (searchFilter.hasAudios)
                audioList(audiosLazyPagingItems)

            if (searchFilter.hasMinerva)
                audioList(minervaAudiosLazyPagingItems)
        }
    }
}

@Composable
internal fun ArtistList(
    pagingItems: LazyPagingItems<Artist>,
    imageSize: Dp = ArtistsDefaults.imageSize,
    navigator: Navigator = LocalNavigator.current
) {
    LogCompositions(tag = "ArtistList")

    val isLoading = pagingItems.loadState.refresh == LoadState.Loading
    val hasItems = pagingItems.itemCount > 0
    if (hasItems || isLoading)
        SearchListLabel(stringResource(R.string.search_artists), hasItems, pagingItems.loadState)

    LazyRow(Modifier.fillMaxWidth()) {
        if (!hasItems && isLoading) {
            val placeholders = (1..5).map { Artist() }
            items(placeholders) { placeholder ->
                ArtistColumn(placeholder, imageSize, isPlaceholder = true)
            }
        }

        items(pagingItems, key = { _, item -> item.id }) { it ->
            val artist = it ?: return@items

            ArtistColumn(artist, imageSize) {
                navigator.navigate(LeafScreen.ArtistDetails.buildRoute(it.id))
            }
        }

        loadingMoreRow(pagingItems, height = imageSize)
    }
}

@Composable
internal fun AlbumList(
    pagingItems: LazyPagingItems<Album>,
    itemSize: Dp = AlbumsDefaults.imageSize,
    iconPadding: Dp = AlbumsDefaults.iconPadding,
    navigator: Navigator = LocalNavigator.current
) {
    LogCompositions(tag = "AlbumList")
    val isLoading = pagingItems.loadState.refresh == LoadState.Loading
    val hasItems = pagingItems.itemCount > 0
    if (hasItems || isLoading)
        SearchListLabel(stringResource(R.string.search_albums), hasItems, pagingItems.loadState)

    LazyRow(Modifier.fillMaxWidth()) {
        if (!hasItems && isLoading) {
            val placeholders = (1..5).map { Album() }
            items(placeholders) { placeholder ->
                AlbumColumn(placeholder, imageSize = itemSize, iconPadding = iconPadding, isPlaceholder = true)
            }
        }

        items(pagingItems, key = { _, item -> item.id }) {
            val album = it ?: Album()

            AlbumColumn(album, imageSize = itemSize, iconPadding = iconPadding, isPlaceholder = it == null) { clickedAlbum ->
                navigator.navigate(LeafScreen.AlbumDetails.buildRoute(clickedAlbum))
            }
        }

        loadingMoreRow(pagingItems, height = itemSize + 32.dp) // additional height is to account for the vertical padding [loadingMore] adds
    }
}

internal fun LazyListScope.audioList(pagingItems: LazyPagingItems<Audio>) {
    val isLoading = pagingItems.loadState.refresh == LoadState.Loading
    val hasItems = pagingItems.itemCount > 0
    if (hasItems || isLoading)
        item {
            SearchListLabel(stringResource(R.string.search_audios), hasItems, pagingItems.loadState)
        }

    if (!hasItems && isLoading) {
        val placeholders = (1..20).map { Audio() }
        items(placeholders) {
            AudioRow(it, isPlaceholder = true)
        }
    }

    items(pagingItems, key = { _, item -> item.id }) {
        val audio = it ?: Audio()
        AudioRow(audio, isPlaceholder = it == null)
    }

    loadingMore(pagingItems)
}

private fun <T : Any> LazyListScope.loadingMoreRow(pagingItems: LazyPagingItems<T>, height: Dp = 100.dp, modifier: Modifier = Modifier) {
    loadingMore(pagingItems, modifier.height(height))
}

private fun <T : Any> LazyListScope.loadingMore(pagingItems: LazyPagingItems<T>, modifier: Modifier = Modifier) {
    item {
        val isLoading = remember(pagingItems.loadState) {
            pagingItems.loadState.mediator?.append == LoadState.Loading || pagingItems.loadState.append == LoadState.Loading
        }
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
private fun SearchListLabel(label: String, hasItems: Boolean, loadState: CombinedLoadStates) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.specs.padding, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold))

        AnimatedVisibility(
            visible = (hasItems && loadState.mediator?.refresh == LoadState.Loading),
            enter = expandIn(Alignment.Center),
            exit = shrinkOut(Alignment.Center)
        ) {
            ProgressIndicatorSmall()
        }
    }
}
