/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.library

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.ui.LocalScaffoldPadding
import com.google.accompanist.insets.ui.Scaffold
import tm.alashow.base.util.extensions.Callback
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.entities.Album
import tm.alashow.datmusic.domain.entities.LibraryItems
import tm.alashow.datmusic.domain.entities.Playlist
import tm.alashow.datmusic.ui.library.items.LibraryItemRow
import tm.alashow.datmusic.ui.library.playlists.PlaylistRow
import tm.alashow.domain.models.Success
import tm.alashow.domain.models.Uninitialized
import tm.alashow.navigation.LeafScreen
import tm.alashow.navigation.LocalNavigator
import tm.alashow.navigation.Navigator
import tm.alashow.ui.components.AppTopBar
import tm.alashow.ui.components.EmptyErrorBox
import tm.alashow.ui.components.IconButton
import tm.alashow.ui.components.fullScreenLoading

@Composable
fun Library() {
    Library(viewModel = hiltViewModel())
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Library(
    viewModel: LibraryViewModel,
    navigator: Navigator = LocalNavigator.current
) {
    val listState = rememberLazyListState()
    val asyncLibraryItems by rememberFlowWithLifecycle(viewModel.libraryItems).collectAsState(Uninitialized)

    Scaffold(
        topBar = {
            LibraryTopBar(
                onCreatePlaylist = {
                    navigator.navigate(LeafScreen.CreatePlaylist().createRoute())
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = LocalScaffoldPadding.current,
            state = listState
        ) {
            when (val items = asyncLibraryItems) {
                is Success -> libraryList(
                    items = items(),
                )
                else -> fullScreenLoading()
            }
        }
    }
}

@Composable
private fun LibraryTopBar(onCreatePlaylist: Callback = {}) {
    AppTopBar(
        title = stringResource(R.string.library_title),
        actionsContentAlpha = ContentAlpha.medium,
        actions = {
            IconButton(
                onClick = onCreatePlaylist,
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.library_createPlaylist),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    )
}

fun LazyListScope.libraryList(
    items: LibraryItems = listOf(),
) {
    val downloadsEmpty = items.isEmpty()
    if (downloadsEmpty) {
        item {
            EmptyErrorBox(
                message = stringResource(R.string.library_empty),
                retryVisible = false,
                modifier = Modifier.fillParentMaxHeight()
            )
        }
    }

    items(items) {
        when (it) {
            is Playlist -> PlaylistRow(playlist = it)
            is Album -> LibraryItemRow(libraryItem = it, typeRes = R.string.albums_detail_title)
            else -> LibraryItemRow(libraryItem = it, typeRes = R.string.unknown)
        }
    }
}
