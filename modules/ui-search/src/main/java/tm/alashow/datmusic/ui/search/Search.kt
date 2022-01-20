/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.WindowInsets
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.Scaffold
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import tm.alashow.common.compose.collectEvent
import tm.alashow.common.compose.getNavArgument
import tm.alashow.common.compose.rememberFlowWithLifecycle
import tm.alashow.datmusic.data.DatmusicSearchParams.BackendType
import tm.alashow.navigation.screens.QUERY_KEY
import tm.alashow.ui.components.ChipsRow
import tm.alashow.ui.components.SearchTextField
import tm.alashow.ui.theme.AppTheme
import tm.alashow.ui.theme.topAppBarTitleStyle
import tm.alashow.ui.theme.translucentSurface

@Composable
fun Search() {
    Search(viewModel = hiltViewModel())
}

@Composable
internal fun Search(
    viewModel: SearchViewModel = hiltViewModel(),
) {
    Search(viewModel) { action ->
        viewModel.submitAction(action)
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
internal fun Search(
    viewModel: SearchViewModel,
    actioner: (SearchAction) -> Unit
) {
    val viewState by rememberFlowWithLifecycle(viewModel.state)
    val listState = rememberLazyListState()

    Search(
        viewState = viewState,
        viewModel = viewModel,
        listState = listState,
        actioner = actioner
    )
}

@Composable
private fun Search(
    viewState: SearchViewState,
    viewModel: SearchViewModel,
    listState: LazyListState,
    actioner: (SearchAction) -> Unit,
) {
    val searchBarHideThreshold = 3
    val searchBarHeight = 200.dp
    val searchBarVisibility = remember { Animatable(0f) }

    // hide search bar when scrolling after some scroll
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .debounce(60)
            .distinctUntilChanged()
            .map { if (listState.firstVisibleItemIndex > searchBarHideThreshold) it else false }
            .map { if (it) 1f else 0f }
            .collectLatest { searchBarVisibility.animateTo(it) }
    }

    // scroll up when new search event is fired
    collectEvent(viewModel.onSearchEvent) {
        listState.scrollToItem(0)
    }

    Scaffold(
        topBar = {
            SearchAppBar(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = 1 - searchBarVisibility.value
                        translationY = searchBarHeight.value * (-searchBarVisibility.value)
                    },
                state = viewState,
                onQueryChange = { actioner(SearchAction.QueryChange(it)) },
                onSearch = { actioner(SearchAction.Search) },
                onBackendTypeSelect = { actioner(it) }
            )
        }
    ) {
        SearchList(
            viewModel = viewModel,
            listState = listState,
        )
    }
}

@Composable
@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
private fun SearchAppBar(
    state: SearchViewState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    onBackendTypeSelect: (SearchAction.SelectBackendType) -> Unit = {},
    focusManager: FocusManager = LocalFocusManager.current,
    windowInfo: WindowInfo = LocalWindowInfo.current,
    windowInsets: WindowInsets = LocalWindowInsets.current,
) {
    val initialQuery = (getNavArgument(QUERY_KEY) ?: "").toString()
    Box(
        modifier = modifier
            .translucentSurface()
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(bottom = AppTheme.specs.paddingTiny)
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val hasWindowFocus = windowInfo.isWindowFocused
        val keyboardVisible = windowInsets.ime.isVisible

        var focused by remember { mutableStateOf(false) }
        val searchActive = focused && hasWindowFocus && keyboardVisible

        val triggerSearch = {
            onSearch()
            keyboardController?.hide()
            focusManager.clearFocus()
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.specs.paddingSmall)
        ) {
            Text(
                text = stringResource(R.string.search_title),
                style = topAppBarTitleStyle(),
                modifier = titleModifier.padding(start = AppTheme.specs.padding, top = AppTheme.specs.padding),
            )
            var query by rememberSaveable { mutableStateOf(initialQuery) }
            SearchTextField(
                value = query,
                onValueChange = { value ->
                    query = value
                    onQueryChange(value)
                },
                onSearch = { triggerSearch() },
                hint = if (!searchActive) stringResource(R.string.search_hint) else stringResource(R.string.search_hint_query),
                analyticsPrefix = "search",
                modifier = Modifier
                    .padding(horizontal = AppTheme.specs.padding)
                    .onFocusChanged {
                        focused = it.isFocused
                    }
            )

            var backends = state.filter.backends
            // this applies until default selections mean everything is chosen
            if (backends == SearchFilter.DefaultBackends)
                backends = emptySet()

            val filterVisible = searchActive || query.isNotBlank() || backends.isNotEmpty()
            SearchFilterPanel(visible = filterVisible, backends) { selectAction ->
                onBackendTypeSelect(selectAction)
                triggerSearch()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ColumnScope.SearchFilterPanel(
    visible: Boolean,
    selectedItems: Set<BackendType>,
    onBackendTypeSelect: (SearchAction.SelectBackendType) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandIn(expandFrom = Alignment.TopCenter) + fadeIn(),
        exit = shrinkOut(shrinkTowards = Alignment.BottomCenter) + fadeOut()
    ) {
        ChipsRow(
            items = BackendType.values().toList(),
            selectedItems = selectedItems,
            onItemSelect = { selected, item ->
                onBackendTypeSelect(SearchAction.SelectBackendType(selected, item))
            },
            labelMapper = {
                stringResource(
                    when (it) {
                        BackendType.AUDIOS -> R.string.search_audios
                        BackendType.ARTISTS -> R.string.search_artists
                        BackendType.ALBUMS -> R.string.search_albums
                        BackendType.MINERVA -> R.string.search_minerva
                        BackendType.FLACS -> R.string.search_flacs
                    }
                )
            }
        )
    }
}
