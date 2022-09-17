/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import tm.alashow.base.util.localizedMessage
import tm.alashow.base.util.localizedTitle
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.Artist
import tm.alashow.datmusic.domain.entities.Audio
import tm.alashow.datmusic.domain.models.errors.EmptyResultException
import tm.alashow.datmusic.ui.albums.AlbumColumn
import tm.alashow.datmusic.ui.albums.AlbumsDefaults
import tm.alashow.datmusic.ui.artists.ArtistColumn
import tm.alashow.datmusic.ui.artists.ArtistsDefaults
import tm.alashow.datmusic.ui.audios.AudioRow
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.navigation.screens.LeafScreen
import tm.alashow.ui.Delayed
import tm.alashow.ui.components.ErrorBox
import tm.alashow.ui.components.FullScreenLoading
import tm.alashow.ui.components.ProgressIndicator
import tm.alashow.ui.components.ProgressIndicatorSmall
import tm.alashow.ui.items
import tm.alashow.ui.scaffoldPadding
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.Theme

fun <T : Any> LazyPagingItems<T>.isLoading() = loadState.refresh == LoadState.Loading
fun <T : Any> LazyPagingItems<T>.isRefreshing() = loadState.mediator?.refresh == LoadState.Loading

@Composable
internal fun SearchList(
    viewState: SearchViewState,
    listState: LazyListState,
    artistsListState: LazyListState,
    albumsListState: LazyListState,
    onSearchAction: (SearchAction) -> Unit,
    searchLazyPagers: SearchLazyPagers,
    modifier: Modifier = Modifier,
) {
    val searchFilter = viewState.filter
    val pagers = searchFilter.backends.map { searchLazyPagers[it] }.toSet()
    val pagerRefreshStates = pagers.map { it.loadState.refresh }.toTypedArray()
    val pagersAreEmpty = pagers.all { it.itemCount == 0 }
    val pagersAreLoading = pagers.all { it.isRefreshing() }
    val refreshPagers = { pagers.forEach { it.refresh() } }
    val retryPagers = { pagers.forEach { it.retry() } }
    val refreshErrorState = pagerRefreshStates.firstOrNull { it is LoadState.Error }

    val hasMultiplePagers = pagers.size > 1

    if (pagersAreEmpty && !pagersAreLoading && refreshErrorState == null) {
        FullScreenLoading(delayMillis = 100)
        return
    }

    SearchListErrors(
        viewState = viewState,
        retryPagers = retryPagers,
        refreshErrorState = refreshErrorState,
        pagersAreEmpty = pagersAreEmpty,
        hasMultiplePagers = hasMultiplePagers,
        onSearchAction = onSearchAction,
    )

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = false),
        indicator = { state, trigger -> SwipeRefreshIndicator(state, trigger, scale = true) },
        indicatorPadding = scaffoldPadding(),
        onRefresh = refreshPagers,
        modifier = modifier,
    ) {
        SearchListContent(
            listState = listState,
            artistsListState = artistsListState,
            albumsListState = albumsListState,
            searchFilter = searchFilter,
            searchLazyPagers = searchLazyPagers,
            pagersAreEmpty = pagersAreEmpty,
            hasActiveSearchQuery = viewState.hasActiveSearchQuery,
            retryPagers = retryPagers,
            refreshErrorState = refreshErrorState,
            onPlayAudio = { onSearchAction(SearchAction.PlayAudio(it)) },
        )
    }
}

@Composable
private fun SearchListContent(
    listState: LazyListState,
    artistsListState: LazyListState,
    albumsListState: LazyListState,
    searchFilter: SearchFilter,
    searchLazyPagers: SearchLazyPagers,
    hasActiveSearchQuery: Boolean,
    pagersAreEmpty: Boolean,
    retryPagers: () -> Unit,
    refreshErrorState: LoadState?,
    onPlayAudio: (Audio) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        contentPadding = scaffoldPadding(),
        modifier = modifier.fillMaxSize()
    ) {
        if (refreshErrorState is LoadState.Error && pagersAreEmpty) {
            item {
                Delayed {
                    ErrorBox(
                        title = stringResource(refreshErrorState.error.localizedTitle()),
                        message = stringResource(refreshErrorState.error.localizedMessage()),
                        onRetryClick = { retryPagers() },
                        modifier = Modifier.fillParentMaxHeight()
                    )
                }
            }
        }

        if (hasActiveSearchQuery && searchFilter.hasArtists) {
            item("artists") {
                ArtistList(searchLazyPagers.artists, artistsListState)
            }
        }

        if (hasActiveSearchQuery && searchFilter.hasAlbums) {
            item("albums") {
                AlbumList(searchLazyPagers.albums, albumsListState)
            }
        }

        if (searchFilter.hasAudios) {
            audioList(searchLazyPagers.audios, onPlayAudio)
        }

        if (searchFilter.hasMinerva) {
            audioList(searchLazyPagers.minerva, onPlayAudio)
        }

        if (searchFilter.hasFlacs) {
            audioList(searchLazyPagers.flacs, onPlayAudio)
        }
    }
}

@Composable
private fun ArtistList(
    pagingItems: LazyPagingItems<Artist>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    imageSize: Dp = ArtistsDefaults.imageSize,
    navigator: Navigator = LocalNavigator.current
) {
    Column(modifier) {
        val isLoading = pagingItems.isLoading()
        val hasItems = pagingItems.itemCount > 0
        if (hasItems || isLoading) {
            SearchListLabel(stringResource(R.string.search_artists), hasItems, pagingItems.loadState)
        }

        if (!hasItems && isLoading) {
            LazyRow(Modifier.fillMaxWidth()) {
                val placeholders = (1..5).map { Artist() }
                items(placeholders) { placeholder ->
                    ArtistColumn(placeholder, imageSize, isPlaceholder = true)
                }
            }
        }

        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(pagingItems, key = { _, item -> item.id }) {
                val artist = it ?: return@items

                ArtistColumn(artist, imageSize) {
                    navigator.navigate(LeafScreen.ArtistDetails.buildRoute(artist.id))
                }
            }
            loadingMoreRow(pagingItems, height = imageSize)
        }
    }
}

@Composable
private fun AlbumList(
    pagingItems: LazyPagingItems<Album>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    itemSize: Dp = AlbumsDefaults.imageSize,
    navigator: Navigator = LocalNavigator.current
) {
    Column(modifier) {
        val isLoading = pagingItems.isLoading()
        val hasItems = pagingItems.itemCount > 0

        if (hasItems || isLoading) {
            SearchListLabel(stringResource(R.string.search_albums), hasItems, pagingItems.loadState)
        }

        if (!hasItems && isLoading) {
            LazyRow(Modifier.fillMaxWidth()) {
                val placeholders = (1..5).map { Album() }
                items(placeholders) { placeholder ->
                    AlbumColumn(placeholder, imageSize = itemSize, isPlaceholder = true)
                }
            }
        }

        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(pagingItems, key = { _, item -> item.id }) {
                val album = it ?: Album()
                AlbumColumn(
                    album = album,
                    isPlaceholder = it == null,
                    imageSize = itemSize,
                ) {
                    navigator.navigate(LeafScreen.AlbumDetails.buildRoute(album))
                }
            }
            // additional height is to account for the vertical padding [loadingMore] adds
            loadingMoreRow(pagingItems, height = itemSize + 32.dp)
        }
    }
}

private fun LazyListScope.audioList(pagingItems: LazyPagingItems<Audio>, onPlayAudio: (Audio) -> Unit) {
    val isLoading = pagingItems.isLoading()
    val hasItems = pagingItems.itemCount > 0

    if (hasItems || isLoading) {
        item {
            SearchListLabel(stringResource(R.string.search_audios), hasItems, pagingItems.loadState)
        }
    }

    if (!hasItems && isLoading) {
        val placeholders = (1..20).map { Audio() }
        items(placeholders) { audio ->
            AudioRow(
                audio = audio,
                isPlaceholder = true
            )
        }
    }

    items(pagingItems, key = { _, audio -> audio.id }) { audio ->
        AudioRow(
            audio = audio ?: Audio(),
            isPlaceholder = audio == null,
            playOnClick = false,
            onPlayAudio = onPlayAudio,
        )
    }

    loadingMore(pagingItems)
}

private fun <T : Any> LazyListScope.loadingMoreRow(pagingItems: LazyPagingItems<T>, height: Dp = 100.dp, modifier: Modifier = Modifier) {
    loadingMore(pagingItems, modifier.height(height))
}

private fun <T : Any> LazyListScope.loadingMore(pagingItems: LazyPagingItems<T>, modifier: Modifier = Modifier) {
    val isLoading = pagingItems.loadState.mediator?.append == LoadState.Loading || pagingItems.loadState.append == LoadState.Loading
    if (isLoading)
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

@Composable
private fun SearchListLabel(
    label: String,
    hasItems: Boolean,
    loadState: CombinedLoadStates,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Theme.specs.padding, vertical = Theme.specs.paddingSmall),
    ) {
        Text(text = label, style = Theme.typography.h6)

        AnimatedVisibility(
            visible = (hasItems && loadState.mediator?.refresh == LoadState.Loading),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ProgressIndicatorSmall()
        }
    }
}

@Composable
private fun SearchListErrors(
    viewState: SearchViewState,
    retryPagers: () -> Unit,
    refreshErrorState: LoadState?,
    pagersAreEmpty: Boolean,
    hasMultiplePagers: Boolean,
    onSearchAction: (SearchAction) -> Unit,
) {
    val captchaError = viewState.captchaError
    var captchaErrorShown by remember(captchaError) { mutableStateOf(true) }
    if (captchaError != null) {
        CaptchaErrorDialog(
            captchaErrorShown, { captchaErrorShown = it }, captchaError,
            onCaptchaSubmit = { solution ->
                onSearchAction(SearchAction.SubmitCaptcha(captchaError, solution))
            }
        )
    }

    // add snackbar error if there's an error state in any of the active pagers (except empty result errors)
    // and some of the pagers is not empty (in which case full screen error will be shown)
    LaunchedEffect(refreshErrorState, pagersAreEmpty) {
        if (refreshErrorState is LoadState.Error && !pagersAreEmpty) {
            // we don't wanna show empty results error snackbar when there's multiple pagers and one of the pagers gets empty result error (but we have some results if we are here)
            val emptyResultsButHasMultiplePagers = refreshErrorState.error is EmptyResultException && hasMultiplePagers
            if (emptyResultsButHasMultiplePagers)
                return@LaunchedEffect
            onSearchAction(SearchAction.AddError(refreshErrorState.error, retryPagers))
        }
    }
}
