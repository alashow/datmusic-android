/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import tm.alashow.domain.models.PaginatedEntity

@Composable
fun <T : PaginatedEntity> EntityList(
    lazyPagingItems: LazyPagingItems<out T>,
    modifier: Modifier = Modifier,
    padding: PaddingValues,
    itemContent: @Composable LazyListScope.(T?) -> Unit
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(
            isRefreshing = lazyPagingItems.loadState.refresh == LoadState.Loading
        ),
        onRefresh = { lazyPagingItems.refresh() },
        indicatorPadding = padding,
        indicator = { state, trigger ->
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = trigger,
                scale = true
            )
        }
    ) {
        LazyColumn(
            contentPadding = padding,
            modifier = modifier.fillMaxSize()
        ) {
            items(lazyPagingItems = lazyPagingItems) {
                this@LazyColumn.itemContent(it)
            }

            if (lazyPagingItems.loadState.append == LoadState.Loading) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
            }

            when (val refreshState = lazyPagingItems.loadState.refresh) {
                is LoadState.Error -> {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text("Error: ${refreshState.error}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun <T : PaginatedEntity> EntityListRow(
    lazyPagingItems: LazyPagingItems<out T>,
    modifier: Modifier = Modifier,
    padding: PaddingValues,
    itemContent: @Composable LazyListScope.(T?) -> Unit
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(
            isRefreshing = lazyPagingItems.loadState.refresh == LoadState.Loading
        ),
        onRefresh = { lazyPagingItems.refresh() },
        indicatorPadding = padding,
        indicator = { state, trigger ->
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = trigger,
                scale = true
            )
        }
    ) {
        LazyRow(
            contentPadding = padding,
            modifier = modifier.fillMaxSize()
        ) {
            items(lazyPagingItems = lazyPagingItems) {
                this@LazyRow.itemContent(it)
            }

            if (lazyPagingItems.loadState.append == LoadState.Loading) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
            }

            when (val refreshState = lazyPagingItems.loadState.refresh) {
                is LoadState.Error -> {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text("Error: ${refreshState.error}")
                        }
                    }
                }
            }
        }
    }
}
