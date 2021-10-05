/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.ui.Scaffold
import kotlinx.coroutines.launch
import tm.alashow.base.util.extensions.Callback
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.domain.entities.LibraryItems
import tm.alashow.datmusic.ui.library.R
import tm.alashow.domain.models.Success
import tm.alashow.domain.models.Uninitialized
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
private fun Library(viewModel: LibraryViewModel) {
    val listState = rememberLazyListState()
    val coroutine = rememberCoroutineScope()
    val createPlaylistState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        confirmStateChange = { it != ModalBottomSheetValue.HalfExpanded } // disallow halfexpanded for now
    )
    val asyncLibraryItems by rememberFlowWithLifecycle(viewModel.libraryItems).collectAsState(Uninitialized)
    CreatePlaylist(createPlaylistState) {
        Scaffold(
            topBar = {
                LibraryTopBar(
                    onCreatePlaylist = {
                        coroutine.launch { createPlaylistState.animateTo(ModalBottomSheetValue.Expanded) }
                    }
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            LazyColumn(
                contentPadding = padding,
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

    itemsIndexed(items) { index, it ->
        if (index != 0) Divider()
        Text(it.getLabel())
    }
}
